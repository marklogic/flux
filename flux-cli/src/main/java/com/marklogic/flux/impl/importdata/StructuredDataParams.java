/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.StructuredDataImporter;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parameters for transforming rows read from structured data sources, such as JDBC, Parquet, CSV, etc.
 * Supports filtering rows via WHERE expressions, grouping rows by column values, aggregating related rows into
 * arrays, and ordering aggregated arrays.
 */
class StructuredDataParams implements StructuredDataImporter.GroupByOptions<StructuredDataParams> {

    private static final String AGGREGATE_DELIMITER = ",";

    @CommandLine.Option(
        names = "--drop",
        description = "Specify one or more column names to drop from the imported data.",
        arity = "1..*"
    )
    private List<String> columnsToDrop = new ArrayList<>();

    @CommandLine.Option(
        names = "--where",
        description = "Filter rows using a SQL-like WHERE expression; e.g. --where \"size < 10\" or --where \"status = 'active'\"."
    )
    private String where;

    @CommandLine.Option(
        names = "--group-by",
        description = "Name of a column to group the rows by before constructing documents. Typically used with at " +
            "least one instance of the --aggregate option.")
    private String groupBy;

    @CommandLine.Option(
        names = "--aggregate",
        description = "Define an aggregation of multiple columns into a new column. Each aggregation must be of the " +
            "form newColumnName=column1,column2,etc. Requires the use of --group-by.",
        converter = Aggregation.class
    )
    private List<Aggregation> aggregations = new ArrayList<>();

    @CommandLine.Option(
        names = "--aggregate-order-by",
        description = "Specify ordering for an an aggregation produced by --aggregate. Must be of the form aggregationName=columnName:asc " +
            "or aggregationName=columnName:desc. The columnName must be one of the columns in the corresponding " +
            "aggregation. Default order is ascending if not specified. Can be specified multiple times to order " +
            "multiple aggregations.",
        converter = AggregationOrdering.class
    )
    private List<AggregationOrdering> aggregationOrderings = new ArrayList<>();

    public static class Aggregation implements CommandLine.ITypeConverter<Aggregation> {
        private String newColumnName;
        private List<String> columnNamesToGroup;

        public Aggregation() {
            // Needed so that picocli can instantiate in order to call convert().
        }

        public Aggregation(String newColumnName, List<String> columnNamesToGroup) {
            this.newColumnName = newColumnName;
            this.columnNamesToGroup = columnNamesToGroup;
        }

        @Override
        public Aggregation convert(String value) {
            String[] parts = value.split("=");
            if (parts.length != 2) {
                throw new FluxException(String.format("Invalid aggregation: %s; must be of " +
                    "the form newColumnName=columnToGroup1,columnToGroup2,etc.", value));
            }
            final String newColumnName = parts[0];
            String[] columnNamesToAggregate = parts[1].split(AGGREGATE_DELIMITER);
            return new Aggregation(newColumnName, Arrays.asList(columnNamesToAggregate));
        }
    }

    public static class AggregationOrdering implements CommandLine.ITypeConverter<AggregationOrdering> {
        private String aggregationName;
        private String columnName;
        private boolean ascending = true;

        public AggregationOrdering() {
            // Needed so that picocli can instantiate in order to call convert().
        }

        public AggregationOrdering(String aggregationName, String columnName, boolean ascending) {
            this.aggregationName = aggregationName;
            this.columnName = columnName;
            this.ascending = ascending;
        }

        @Override
        public AggregationOrdering convert(String value) {
            String[] parts = value.split("=");
            if (parts.length != 2) {
                throw new FluxException(String.format("Invalid aggregate order-by: %s; must be of the form " +
                    "aggregationName=columnName[:asc|desc]", value));
            }

            String aggregationName = parts[0];
            String columnAndDirection = parts[1];

            String columnName;
            boolean ascending = true;

            if (columnAndDirection.endsWith(":asc")) {
                columnName = columnAndDirection.substring(0, columnAndDirection.length() - 4);
                ascending = true;
            } else if (columnAndDirection.endsWith(":desc")) {
                columnName = columnAndDirection.substring(0, columnAndDirection.length() - 5);
                ascending = false;
            } else {
                // No direction specified, default to ascending
                columnName = columnAndDirection;
            }

            return new AggregationOrdering(aggregationName, columnName, ascending);
        }

        public String getAggregationName() {
            return aggregationName;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isAscending() {
            return ascending;
        }
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public StructuredDataParams drop(String... columns) {
        this.columnsToDrop.addAll(Arrays.asList(columns));
        return this;
    }

    public StructuredDataParams where(String expression) {
        this.where = expression;
        return this;
    }

    @Override
    public StructuredDataParams orderAggregation(String aggregationName, String columnName, boolean ascending) {
        if (this.aggregationOrderings == null) {
            this.aggregationOrderings = new ArrayList<>();
        }
        this.aggregationOrderings.add(new AggregationOrdering(aggregationName, columnName, ascending));
        return this;
    }

    @Override
    public StructuredDataParams aggregateColumns(String newColumnName, String... columns) {
        if (this.aggregations == null) {
            this.aggregations = new ArrayList<>();
        }
        this.aggregations.add(new Aggregation(newColumnName, Arrays.asList(columns)));
        return this;
    }

    public Dataset<Row> applyTransformations(Dataset<Row> dataset) {
        // Apply where filter first, before groupBy. This allows the user to filter out rows that would otherwise be
        // included in the groupBy and thus end up in the aggregated arrays.
        if (where != null && !where.trim().isEmpty()) {
            try {
                dataset = dataset.where(where);
            } catch (Exception ex) {
                throw new FluxException("Unable to apply 'where' clause: '%s'; cause: %s"
                    .formatted(where, ex.getMessage()), ex);
            }
        }

        if (groupBy == null || groupBy.trim().isEmpty()) {
            return applyDrop(dataset);
        }

        final RelationalGroupedDataset groupedDataset = dataset.groupBy(this.groupBy);

        List<Column> columns = getColumnsNotInAggregation(dataset);
        List<Column> aggregationColumns = makeAggregationColumns();
        columns.addAll(aggregationColumns);
        final Column aliasColumn = columns.get(0);
        final Column[] columnsToGroup = columns.subList(1, columns.size()).toArray(new Column[]{});
        Dataset<Row> result;
        try {
            result = groupedDataset.agg(aliasColumn, columnsToGroup);
        } catch (Exception e) {
            String columnNames = aggregations.stream().map(agg -> agg.columnNamesToGroup.toString()).collect(Collectors.joining(", "));
            throw new FluxException(String.format("Unable to aggregate columns: %s; please ensure that each column " +
                "name will be present in the data read from the data source.", columnNames), e);
        }

        Dataset<Row> sorted = aggregationOrderings != null && !aggregationOrderings.isEmpty() ?
            applySortToAggregatedArrays(result) :
            result;

        return applyDrop(sorted);
    }

    /**
     * @param dataset
     * @return a list of columns reflecting each column that is not referenced in an aggregation and is also not the
     * "groupBy" column. These columns are assumed to have the same value in every row, and thus only the first value
     * is needed for each column.
     */
    private List<Column> getColumnsNotInAggregation(Dataset<Row> dataset) {
        Set<String> aggregatedColumnNames = new HashSet<>();
        aggregations.forEach(agg -> aggregatedColumnNames.addAll(agg.columnNamesToGroup));

        List<Column> columns = new ArrayList<>();
        for (String name : dataset.schema().names()) {
            if (!aggregatedColumnNames.contains(name) && !groupBy.equals(name)) {
                columns.add(functions.first(name).alias(name));
            }
        }
        return columns;
    }

    /**
     * @return a list of columns, one per aggregation.
     */
    private List<Column> makeAggregationColumns() {
        List<Column> columns = new ArrayList<>();
        aggregations.forEach(aggregation -> {
            final List<String> columnNames = aggregation.columnNamesToGroup;
            Column resultColumn;

            if (columnNames.size() == 1) {
                Column column = new Column(columnNames.get(0));
                resultColumn = functions.collect_list(functions.concat(column));
            } else {
                Column[] structColumns = columnNames.stream().map(functions::col).toArray(Column[]::new);
                Column arrayColumn = functions.collect_list(functions.struct(structColumns));
                // array_distinct removes duplicate objects that can result from 2+ joins existing in the query.
                // See https://www.sparkreference.com/reference/array_distinct/ for performance considerations.
                resultColumn = functions.array_distinct(arrayColumn);
            }

            // Validate aggregate-order-by if specified for this aggregation
            if (aggregationOrderings != null) {
                aggregationOrderings.stream()
                    .filter(orderBy -> orderBy.getAggregationName().equals(aggregation.newColumnName))
                    .forEach(orderBy -> validateAggregateOrderBy(aggregation, columnNames, orderBy));
            }

            columns.add(resultColumn.alias(aggregation.newColumnName));
        });
        return columns;
    }

    private void validateAggregateOrderBy(Aggregation aggregation, List<String> columnNames, AggregationOrdering orderBy) {
        if (!columnNames.contains(orderBy.getColumnName())) {
            throw new FluxException(String.format(
                "Invalid aggregate order-by for '%s': column '%s' is not in the aggregation. " +
                    "Available columns: %s",
                aggregation.newColumnName,
                orderBy.getColumnName(),
                columnNames));
        }
    }

    private Dataset<Row> applySortToAggregatedArrays(Dataset<Row> dataset) {
        Dataset<Row> result = dataset;
        for (AggregationOrdering orderBy : aggregationOrderings) {
            result = applySortToSingleAggregation(result, orderBy);
        }
        return result;
    }

    private Dataset<Row> applySortToSingleAggregation(Dataset<Row> dataset, AggregationOrdering orderBy) {
        final String aggregationName = orderBy.getAggregationName();
        final String columnToSortBy = orderBy.getColumnName();

        // Find the aggregation to determine if it's single or multi-column
        Aggregation aggregation = aggregations.stream()
            .filter(a -> a.newColumnName.equals(aggregationName))
            .findFirst()
            .orElseThrow(() -> new FluxException(String.format(
                "Aggregate order-by references unknown aggregation '%s'", aggregationName)));

        Column sortedAggregationColumn = aggregation.columnNamesToGroup.size() == 1 ?
            functions.sort_array(functions.col(aggregationName), !orderBy.isAscending()) :
            sortArrayColumn(aggregationName, columnToSortBy, orderBy.isAscending());

        // withColumn replaces the existing aggregation column with the just-sorted aggregation column.
        return dataset.withColumn(aggregationName, sortedAggregationColumn);
    }

    private Column sortArrayColumn(String aggregationName, String columnToSortBy, boolean ascending) {
        // Generated by Copilot.
        // For struct arrays, use array_sort with SQL expression
        // In array_sort lambda: return -1 if left should come before right, 1 if after, 0 if equal
        int whenLessThan = ascending ? -1 : 1;
        int whenGreaterThan = ascending ? 1 : -1;

        return functions.expr(String.format(
            "array_sort(%s, (left, right) -> " +
                "case when left.%s < right.%s then %d " +
                "when left.%s > right.%s then %d else 0 end)",
            aggregationName, columnToSortBy, columnToSortBy, whenLessThan,
            columnToSortBy, columnToSortBy, whenGreaterThan
        ));
    }

    private Dataset<Row> applyDrop(Dataset<Row> dataset) {
        if (columnsToDrop != null && !columnsToDrop.isEmpty()) {
            dataset = dataset.drop(columnsToDrop.toArray(new String[]{}));
        }
        return dataset;
    }
}

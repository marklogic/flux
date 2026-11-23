/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ColumnNameStrategy;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.StructuredDataImporter;
import com.marklogic.flux.impl.OptionsUtil;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parameters for transforming rows read from structured data sources, such as JDBC, Parquet, CSV, etc.
 * Supports filtering rows via WHERE expressions, grouping rows by column values, aggregating related rows into
 * arrays, ordering aggregated arrays, and sanitizing top-level column names.
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

    @CommandLine.Option(
        names = "--column-name-strategy",
        description = "Strategy for sanitizing top-level column names before constructing documents. " +
            OptionsUtil.VALID_VALUES_DESCRIPTION
    )
    private ColumnNameStrategy columnNameStrategy = ColumnNameStrategy.NONE;

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

    public StructuredDataParams columnNameStrategy(ColumnNameStrategy strategy) {
        this.columnNameStrategy = strategy != null ? strategy : ColumnNameStrategy.NONE;
        return this;
    }

    /**
     * Package-private, intended solely for tests that verify how {@code --column-name-strategy} is parsed.
     */
    ColumnNameStrategy getColumnNameStrategy() {
        return columnNameStrategy;
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
        // Sanitize column names first so that every other transformation can refer to the sanitized names, and so
        // that the fields in an aggregation - which are named after the columns they are built from - are sanitized.
        final String[] originalColumnNames = dataset.schema().names();
        dataset = sanitizeColumnNames(dataset);
        final boolean caseSensitive = Boolean.parseBoolean(dataset.sparkSession().conf().get("spark.sql.caseSensitive", "false"));
        final ColumnReferences references = new ColumnReferences(dataset.schema().names(), originalColumnNames, caseSensitive);

        // Apply where filter before groupBy. This allows the user to filter out rows that would otherwise be
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
            return applyDrop(dataset, references);
        }

        final Column groupByColumn = functions.col(references.toExpression(this.groupBy));
        try {
            // Spark does not check the column to group by until the aggregation is performed, at which point a problem
            // with it cannot be told apart from a problem with an aggregated column. So check it now.
            dataset.select(groupByColumn);
        } catch (Exception e) {
            throw new FluxException("Unable to group by '%s'; cause: %s".formatted(groupBy, getErrorMessage(e)), e);
        }
        final RelationalGroupedDataset groupedDataset = dataset.groupBy(groupByColumn);

        List<Column> columns = getColumnsNotInAggregation(dataset, references);
        List<Column> aggregationColumns = makeAggregationColumns(references);
        columns.addAll(aggregationColumns);
        final Column aliasColumn = columns.get(0);
        final Column[] columnsToGroup = columns.subList(1, columns.size()).toArray(new Column[]{});
        Dataset<Row> result;
        try {
            result = groupedDataset.agg(aliasColumn, columnsToGroup);
        } catch (Exception e) {
            String columnNames = aggregations.stream().map(agg -> agg.columnNamesToGroup.toString()).collect(Collectors.joining(", "));
            throw new FluxException(String.format("Unable to aggregate columns: %s; please ensure that each column " +
                "name will be present in the data read from the data source. Cause: %s", columnNames, getErrorMessage(e)), e);
        }

        Dataset<Row> sorted = aggregationOrderings != null && !aggregationOrderings.isEmpty() ?
            applySortToAggregatedArrays(result, references) :
            result;

        return applyDrop(sorted, references);
    }

    /**
     * @param dataset
     * @return a list of columns reflecting each column that is not referenced in an aggregation and is also not the
     * "groupBy" column. These columns are assumed to have the same value in every row, and thus only the first value
     * is needed for each column.
     */
    private List<Column> getColumnsNotInAggregation(Dataset<Row> dataset, ColumnReferences references) {
        Set<String> excludedColumnNames = new HashSet<>();
        excludedColumnNames.add(references.resolve(groupBy));
        aggregations.forEach(agg -> agg.columnNamesToGroup.forEach(name -> excludedColumnNames.add(references.resolve(name))));

        List<Column> columns = new ArrayList<>();
        for (String name : dataset.schema().names()) {
            if (!excludedColumnNames.contains(name)) {
                // The name must be quoted so that Spark does not parse e.g. a period in it as a path to a nested field.
                columns.add(functions.first(functions.col(ColumnReferences.quote(name))).alias(name));
            }
        }
        return columns;
    }

    /**
     * @return a list of columns, one per aggregation.
     */
    private List<Column> makeAggregationColumns(ColumnReferences references) {
        List<Column> columns = new ArrayList<>();
        aggregations.forEach(aggregation -> {
            final List<String> columnNames = aggregation.columnNamesToGroup;
            Column resultColumn;

            if (columnNames.size() == 1) {
                Column column = functions.col(references.toExpression(columnNames.get(0)));
                resultColumn = functions.collect_list(functions.concat(column));
            } else {
                Column[] structColumns = columnNames.stream()
                    .map(name -> functions.col(references.toExpression(name)))
                    .toArray(Column[]::new);
                Column arrayColumn = functions.collect_list(functions.struct(structColumns));
                // array_distinct removes duplicate objects that can result from 2+ joins existing in the query.
                // See https://www.sparkreference.com/reference/array_distinct/ for performance considerations.
                resultColumn = functions.array_distinct(arrayColumn);
            }

            // Validate aggregate-order-by if specified for this aggregation
            if (aggregationOrderings != null) {
                aggregationOrderings.stream()
                    .filter(orderBy -> orderBy.getAggregationName().equals(aggregation.newColumnName))
                    .forEach(orderBy -> validateAggregateOrderBy(aggregation, columnNames, orderBy, references));
            }

            columns.add(resultColumn.alias(aggregation.newColumnName));
        });
        return columns;
    }

    private void validateAggregateOrderBy(Aggregation aggregation, List<String> columnNames, AggregationOrdering orderBy,
                                          ColumnReferences references) {
        final String columnToSortBy = references.resolve(orderBy.getColumnName());
        if (columnNames.stream().map(references::resolve).noneMatch(name -> Objects.equals(name, columnToSortBy))) {
            throw new FluxException(String.format(
                "Invalid aggregate order-by for '%s': column '%s' is not in the aggregation. " +
                    "Available columns: %s",
                aggregation.newColumnName,
                orderBy.getColumnName(),
                columnNames));
        }
    }

    private Dataset<Row> applySortToAggregatedArrays(Dataset<Row> dataset, ColumnReferences references) {
        Dataset<Row> result = dataset;
        for (AggregationOrdering orderBy : aggregationOrderings) {
            result = applySortToSingleAggregation(result, orderBy, references);
        }
        return result;
    }

    private Dataset<Row> applySortToSingleAggregation(Dataset<Row> dataset, AggregationOrdering orderBy, ColumnReferences references) {
        final String aggregationName = orderBy.getAggregationName();
        // Each field in an aggregated struct is named after the column it was built from.
        final String fieldToSortBy = references.toExpression(orderBy.getColumnName());

        // Find the aggregation to determine if it's single or multi-column
        Aggregation aggregation = aggregations.stream()
            .filter(a -> a.newColumnName.equals(aggregationName))
            .findFirst()
            .orElseThrow(() -> new FluxException(String.format(
                "Aggregate order-by references unknown aggregation '%s'", aggregationName)));

        Column sortedAggregationColumn = aggregation.columnNamesToGroup.size() == 1 ?
            functions.sort_array(functions.col(ColumnReferences.quote(aggregationName)), !orderBy.isAscending()) :
            sortArrayColumn(ColumnReferences.quote(aggregationName), fieldToSortBy, orderBy.isAscending());

        // withColumn replaces the existing aggregation column with the just-sorted aggregation column.
        return dataset.withColumn(aggregationName, sortedAggregationColumn);
    }

    private Column sortArrayColumn(String aggregationName, String columnToSortBy, boolean ascending) {
        // Generated by Copilot.
        // For struct arrays, use array_sort with SQL expression. Both names are inserted into that expression as-is,
        // so the caller must have already quoted them where necessary.
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

    /**
     * @return the message of the given exception, without the query plan that Spark appends to an analysis error.
     */
    private String getErrorMessage(Exception e) {
        return e instanceof AnalysisException analysisException ? analysisException.getSimpleMessage() : e.getMessage();
    }

    private Dataset<Row> applyDrop(Dataset<Row> dataset, ColumnReferences references) {
        if (columnsToDrop != null && !columnsToDrop.isEmpty()) {
            // The references are those of the columns read from the data source, even when this is called after
            // grouping. That is intentional: it is what allows a column to be dropped via its original name. Any
            // other name, such as that of an aggregation, is not a known reference and is passed to Spark as-is.
            dataset = dataset.drop(columnsToDrop.stream().map(references::resolve).toArray(String[]::new));
        }
        return dataset;
    }

    private Dataset<Row> sanitizeColumnNames(Dataset<Row> dataset) {
        ColumnNameSanitizer sanitizer = ColumnNameSanitizer.forStrategy(columnNameStrategy);
        return sanitizer != null ? dataset.toDF(sanitizer.sanitize(dataset.schema().names())) : dataset;
    }
}

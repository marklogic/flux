/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.FluxException;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.*;
import java.util.stream.Collectors;

class AggregationParams implements CommandLine.ITypeConverter<AggregationParams.Aggregation> {

    // Have not found a way to make this configurable in the CLI yet. The StringConverter interface is invoked first
    // by JCommander, which means the logic for converting the aggregation expression doesn't know what value a user
    // would have chosen for overriding this. Could hack something into the Main class to peek at the delimiter value
    // provided by a user, but for now assuming that it will be very unlikely for a Spark column name to have a comma
    // in it.
    private static final String AGGREGATE_DELIMITER = ",";

    @CommandLine.Option(names = "--group-by", description = "Name of a column to group the rows by before constructing documents.")
    private String groupBy;

    @CommandLine.Option(
        names = "--aggregate",
        description = "Define an aggregation of multiple columns into a new column. Each aggregation must be of the " +
            "the form newColumnName=column1,column2,etc. Requires the use of --group-by.",
        converter = AggregationParams.class
    )
    private List<Aggregation> aggregations = new ArrayList<>();

    public static class Aggregation {
        private String newColumnName;
        private List<String> columnNamesToGroup;

        public Aggregation(String newColumnName, List<String> columnNamesToGroup) {
            this.newColumnName = newColumnName;
            this.columnNamesToGroup = columnNamesToGroup;
        }
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

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public void addAggregationExpression(String newColumnName, String... columns) {
        if (this.aggregations == null) {
            this.aggregations = new ArrayList<>();
        }
        this.aggregations.add(new Aggregation(newColumnName, Arrays.asList(columns)));
    }

    public Dataset<Row> applyGroupBy(Dataset<Row> dataset) {
        if (groupBy == null || groupBy.trim().isEmpty()) {
            return dataset;
        }

        final RelationalGroupedDataset groupedDataset = dataset.groupBy(this.groupBy);

        List<Column> columns = getColumnsNotInAggregation(dataset);
        List<Column> aggregationColumns = makeAggregationColumns();
        columns.addAll(aggregationColumns);
        final Column aliasColumn = columns.get(0);
        final Column[] columnsToGroup = columns.subList(1, columns.size()).toArray(new Column[]{});
        try {
            return groupedDataset.agg(aliasColumn, columnsToGroup);
        } catch (Exception e) {
            String columnNames = aggregations.stream().map(agg -> agg.columnNamesToGroup.toString()).collect(Collectors.joining(", "));
            throw new FluxException(String.format("Unable to aggregate columns: %s; please ensure that each column " +
                "name will be present in the data read from the data source.", columnNames), e);
        }
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
            if (columnNames.size() == 1) {
                Column column = new Column(columnNames.get(0));
                Column listOfValuesColumn = functions.collect_list(functions.concat(column));
                columns.add(listOfValuesColumn.alias(aggregation.newColumnName));
            } else {
                Column[] structColumns = columnNames.stream().map(functions::col).toArray(Column[]::new);
                Column arrayColumn = functions.collect_list(functions.struct(structColumns));
                // array_distinct removes duplicate objects that can result from 2+ joins existing in the query.
                // See https://www.sparkreference.com/reference/array_distinct/ for performance considerations.
                columns.add(functions.array_distinct(arrayColumn).alias(aggregation.newColumnName));
            }
        });
        return columns;
    }
}

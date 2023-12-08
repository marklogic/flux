package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

import java.util.*;

public class ImportJdbcCommand extends AbstractImportCommand {

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Parameter(names = "--group-by", description = "the column to group by")
    private String groupBy;

    @Parameter(
        names = "--aggregate",
        description = "Each value is expected to be of the form alias=column1,column2,etc; requires the use of --group-by"
    )
    private List<String> aggregationExpressions = new ArrayList<>();

    @Parameter(names = "--drop", variableArity = true)
    private List<String> dropColumnNames = new ArrayList<>();

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        Dataset<Row> dataset = session.read()
            .jdbc(jdbcParams.getUrl(), jdbcParams.getTable(), jdbcParams.toProperties());

        if (groupBy != null) {
            Map<String, List<String>> aggregationMap = new LinkedHashMap<>();
            if (aggregationExpressions != null) {
                aggregationExpressions.forEach(expr -> {
                    String[] parts = expr.split("=");
                    aggregationMap.put(parts[0], Arrays.asList(parts[1].split(";")));
                });
            }
            List<Column> columns = new ArrayList<>();

            Set<String> aggregatedColumnNames = new HashSet<>();
            aggregationMap.values().forEach(columnNames -> aggregatedColumnNames.addAll(columnNames));

            // Add all the columns that aren't in any aggregations. The assumption is that every row will have the
            // same value for each of these columns, so we only need the first value.
            for (String name : dataset.schema().names()) {
                if (!aggregatedColumnNames.contains(name) && !groupBy.equals(name)) {
                    columns.add(functions.first(name).alias(name));
                }
            }

            for (String alias : aggregationMap.keySet()) {
                List<String> columnNames = aggregationMap.get(alias);
                if (columnNames.size() == 1) {
                    columns.add(functions.collect_list(functions.concat(new Column(columnNames.get(0)))).alias(alias));
                } else {
                    Column[] cols = columnNames.stream().map(name -> functions.col(name)).toArray(Column[]::new);
                    // array_distinct removes duplicate objects that can result from 2+ joins existing in the query.
                    // See https://www.sparkreference.com/reference/array_distinct/ for performance considerations.
                    columns.add(functions.array_distinct(functions.collect_list(functions.struct(cols))).alias(alias));
                }
            }

            dataset = dataset.groupBy(groupBy).agg(
                columns.get(0),
                columns.subList(1, columns.size()).toArray(new Column[]{})
            );
        }

        if (dropColumnNames != null && !dropColumnNames.isEmpty()) {
            dataset = dataset.drop(dropColumnNames.toArray(new String[]{}));
        }

        Dataset<Row> d2 = dataset;
        return write(() -> d2);
    }

    public void setJdbcParams(JdbcParams jdbcParams) {
        this.jdbcParams = jdbcParams;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
}

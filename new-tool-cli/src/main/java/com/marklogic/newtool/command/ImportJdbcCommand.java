package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;

import java.util.*;

@Parameters(commandDescription = "Read rows via JDBC and write documents in MarkLogic.")
public class ImportJdbcCommand extends AbstractCommand {

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Parameter(names = "--groupBy", description = "the column to group by")
    private String groupBy;

    @Parameter(
        names = "--aggregate",
        description = "Each value is expected to be of the form alias=column1,column2,etc; requires the use of --groupBy"
    )
    private List<String> aggregationExpressions = new ArrayList<>();

    @Parameter(names = "--drop", variableArity = true)
    private List<String> dropColumnNames = new ArrayList<>();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        Dataset<Row> dataset = session.read()
            .jdbc(jdbcParams.getUrl(), jdbcParams.getTable(), jdbcParams.toProperties());

        if (groupBy != null) {
            Map<String, List<String>> aggregationMap = makeAggregationMap();
            List<Column> columns = new ArrayList<>();
            addColumnsNotInAggregation(dataset, columns, aggregationMap);
            addAggregationColumns(columns, aggregationMap);
            dataset = dataset.groupBy(groupBy).agg(
                columns.get(0),
                columns.subList(1, columns.size()).toArray(new Column[]{})
            );
        }

        if (dropColumnNames != null && !dropColumnNames.isEmpty()) {
            dataset = dataset.drop(dropColumnNames.toArray(new String[]{}));
        }

        return dataset;
    }

    private Map<String, List<String>> makeAggregationMap() {
        Map<String, List<String>> aggregationMap = new LinkedHashMap<>();
        if (aggregationExpressions != null) {
            aggregationExpressions.forEach(expr -> {
                String[] parts = expr.split("=");
                aggregationMap.put(parts[0], Arrays.asList(parts[1].split(";")));
            });
        }
        return aggregationMap;
    }

    private void addColumnsNotInAggregation(Dataset<Row> dataset, List<Column> columns, Map<String, List<String>> aggregationMap) {
        Set<String> aggregatedColumnNames = new HashSet<>();
        aggregationMap.values().forEach(aggregatedColumnNames::addAll);

        // Add all the columns that aren't in any aggregations. The assumption is that every row will have the
        // same value for each of these columns, so we only need the first value.
        for (String name : dataset.schema().names()) {
            if (!aggregatedColumnNames.contains(name) && !groupBy.equals(name)) {
                columns.add(functions.first(name).alias(name));
            }
        }
    }

    private void addAggregationColumns(List<Column> columns, Map<String, List<String>> aggregationMap) {
        for (Map.Entry<String, List<String>> entry : aggregationMap.entrySet()) {
            final String alias = entry.getKey();
            final List<String> columnNames = entry.getValue();
            if (columnNames.size() == 1) {
                columns.add(functions.collect_list(functions.concat(new Column(columnNames.get(0)))).alias(alias));
            } else {
                Column[] cols = columnNames.stream().map(functions::col).toArray(Column[]::new);
                // array_distinct removes duplicate objects that can result from 2+ joins existing in the query.
                // See https://www.sparkreference.com/reference/array_distinct/ for performance considerations.
                columns.add(functions.array_distinct(functions.collect_list(functions.struct(cols))).alias(alias));
            }
        }
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeDocumentParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }
}

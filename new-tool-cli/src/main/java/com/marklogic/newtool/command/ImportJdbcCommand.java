package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.*;

@Parameters(commandDescription = "Read rows via JDBC and write JSON documents to MarkLogic.")
public class ImportJdbcCommand extends AbstractCommand {

    @ParametersDelegate
    private WriteDocumentWithTemplateParams writeDocumentParams = new WriteDocumentWithTemplateParams();

    @ParametersDelegate
    private JdbcParams jdbcParams = new JdbcParams();

    @Parameter(names = "--query", required = true,
        description = "A SQL query used to read data from the JDBC data source.")
    private String query;

    @Parameter(names = "--groupBy", description = "Name of a column to group the rows by before constructing documents.")
    private String groupBy;

    @Parameter(
        names = "--aggregate",
        description = "Define an aggregation of multiple columns into a new column. Each aggregation must be of the " +
            "the form newColumnName=column1;column2;etc. Requires the user of --groupBy."
    )
    private List<String> aggregationExpressions = new ArrayList<>();

    @Parameter(
        names = "--jsonRootName",
        description = "Name of a root field to add to each JSON document."
    )
    private String jsonRootName;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        Dataset<Row> dataset = session.read().format("jdbc")
            .options(jdbcParams.makeOptions())
            .option("query", query)
            .load();

        return groupBy != null && !groupBy.trim().isEmpty() ? applyGroupBy(dataset) : dataset;
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeDocumentParams.makeOptions())
            .options(OptionsUtil.makeOptions(Options.WRITE_JSON_ROOT_NAME, jsonRootName))
            .mode(SaveMode.Append)
            .save();
    }

    private Dataset<Row> applyGroupBy(Dataset<Row> dataset) {
        Map<String, List<String>> aggregationMap = makeAggregationMap();

        List<Column> columns = getColumnsNotInAggregation(dataset, aggregationMap);
        List<Column> aggregationColumns = makeAggregationColumns(aggregationMap);
        columns.addAll(aggregationColumns);
        return dataset.groupBy(this.groupBy).agg(
            columns.get(0),
            columns.subList(1, columns.size()).toArray(new Column[]{})
        );
    }

    /**
     * Parses the value of each "--aggregation" parameter and returns a map, where each key is a new column name
     * containing an aggregation, and each value is a list of the column names to be aggregated together.
     *
     * @return
     */
    private Map<String, List<String>> makeAggregationMap() {
        Map<String, List<String>> aggregationMap = new LinkedHashMap<>();
        if (aggregationExpressions != null) {
            aggregationExpressions.forEach(expr -> {
                String[] parts = expr.split("=");
                String[] columnNames = parts[1].split(";");
                aggregationMap.put(parts[0], Arrays.asList(columnNames));
            });
        }
        return aggregationMap;
    }

    /**
     * @param dataset
     * @param aggregationMap
     * @return a list of columns reflecting each column that is not referenced in an aggregation and is also not the
     * "groupBy" column. These columns are assumed to have the same value in every row, and thus only the first value
     * is needed for each column.
     */
    private List<Column> getColumnsNotInAggregation(Dataset<Row> dataset, Map<String, List<String>> aggregationMap) {
        Set<String> aggregatedColumnNames = new HashSet<>();
        aggregationMap.values().forEach(aggregatedColumnNames::addAll);

        List<Column> columns = new ArrayList<>();
        for (String name : dataset.schema().names()) {
            if (!aggregatedColumnNames.contains(name) && !groupBy.equals(name)) {
                columns.add(functions.first(name).alias(name));
            }
        }
        return columns;
    }

    /**
     * @param aggregationMap
     * @return a list of columns, one per aggregation in the map.
     */
    private List<Column> makeAggregationColumns(Map<String, List<String>> aggregationMap) {
        List<Column> columns = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : aggregationMap.entrySet()) {
            final String alias = entry.getKey();
            final List<String> columnNames = entry.getValue();
            if (columnNames.size() == 1) {
                Column column = new Column(columnNames.get(0));
                Column listOfValuesColumn = functions.collect_list(functions.concat(column));
                columns.add(listOfValuesColumn.alias(alias));
            } else {
                Column[] structColumns = columnNames.stream().map(functions::col).toArray(Column[]::new);
                Column arrayColumn = functions.collect_list(functions.struct(structColumns));
                // array_distinct removes duplicate objects that can result from 2+ joins existing in the query.
                // See https://www.sparkreference.com/reference/array_distinct/ for performance considerations.
                columns.add(functions.array_distinct(arrayColumn).alias(alias));
            }
        }
        return columns;
    }
}

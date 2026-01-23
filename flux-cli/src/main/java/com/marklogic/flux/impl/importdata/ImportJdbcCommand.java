/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.JdbcImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.*;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-jdbc",
    description = "Read rows via JDBC using Spark's support defined at " +
        "%nhttps://spark.apache.org/docs/latest/sql-data-sources-jdbc.html, and write JSON or XML documents " +
        "to MarkLogic."
)
public class ImportJdbcCommand extends AbstractCommand<JdbcImporter> implements JdbcImporter {

    @CommandLine.Mixin
    private ReadJdbcParams readParams = new ReadJdbcParams();

    @CommandLine.Mixin
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected void validateDuringApiUsage() {
        Map<String, String> options = readParams.makeOptions();
        OptionsUtil.validateRequiredOptions(options, "url", "Must specify a JDBC URL");

        if (options.get("query") == null && options.get("dbtable") == null) {
            throw new FluxException("Must specify either a query or a table to read from");
        }
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) throws Exception {
        DataFrameReader readerToLoad = session.read().format("jdbc").options(readParams.makeOptions());
        try {
            return readerToLoad.load();
        } catch (Exception ex) {
            throw JdbcUtil.massageException(ex);
        }
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        dataset = readParams.aggregationParams.applyGroupBy(dataset);

        TdeHelper.Result result = writeParams.newTdeHelper().logOrLoadTemplate(dataset.schema(), getConnectionParams());
        if (TdeHelper.Result.TEMPLATE_LOGGED.equals(result)) {
            return null;
        }

        return dataset;
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    @Override
    public JdbcImporter from(Consumer<ReadJdbcOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public JdbcImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    public static class ReadJdbcParams extends JdbcParams<JdbcImporter.ReadJdbcOptions> implements JdbcImporter.ReadJdbcOptions {

        static class QueryOptions {
            @CommandLine.Option(names = "--query", required = true,
                description = "The SQL query to execute to read data via the JDBC data source.")
            private String query;

            @CommandLine.Option(names = "--table", required = true,
                description = "The table to read data from via the JDBC data source.")
            private String table;
        }

        @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
        private QueryOptions queryOptions = new QueryOptions();

        @CommandLine.Mixin
        private AggregationParams aggregationParams = new AggregationParams();

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                "query", queryOptions.query,
                "dbtable", queryOptions.table
            );
        }

        @Override
        public JdbcImporter.ReadJdbcOptions query(String query) {
            this.queryOptions.query = query;
            return this;
        }

        @Override
        public ReadJdbcOptions table(String table) {
            this.queryOptions.table = table;
            return this;
        }

        @Override
        public JdbcImporter.ReadJdbcOptions groupBy(String groupBy) {
            this.aggregationParams.setGroupBy(groupBy);
            return this;
        }

        @Override
        public ReadJdbcOptions aggregateColumns(String aggregationName, String... columns) {
            this.aggregationParams.addAggregationExpression(aggregationName, columns);
            return this;
        }

        @Override
        public ReadJdbcOptions orderAggregation(String aggregationName, String columnName, boolean ascending) {
            this.aggregationParams.addAggregationOrdering(aggregationName, columnName, ascending);
            return this;
        }
    }
}

/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.JdbcExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.impl.*;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "export-jdbc",
    description = "Read rows via Optic from MarkLogic and write them to a table using Spark's support defined at" +
        "%nhttps://spark.apache.org/docs/latest/sql-data-sources-jdbc.html ."
)
public class ExportJdbcCommand extends AbstractCommand<JdbcExporter> implements JdbcExporter {

    @CommandLine.Mixin
    private ReadRowsParams readParams = new ReadRowsParams();

    @CommandLine.Mixin
    private WriteJdbcParams writeParams = new WriteJdbcParams();

    @Override
    protected void validateDuringApiUsage() {
        OptionsUtil.validateRequiredOptions(writeParams.makeOptions(),
            "url", "Must specify a JDBC URL",
            "dbtable", "Must specify a table"
        );
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) throws Exception {
        writer = writer.format("jdbc")
            .options(writeParams.makeOptions())
            .mode(SparkUtil.toSparkSaveMode(writeParams.saveMode));

        try {
            writer.save();
        } catch (Exception e) {
            throw JdbcUtil.massageException(e);
        }
    }

    public static class WriteJdbcParams extends JdbcParams<WriteRowsOptions> implements WriteRowsOptions {

        @CommandLine.Option(names = "--table", required = true, description = "The JDBC table that should be written to.")
        private String table;

        @CommandLine.Option(names = "--mode",
            description = "Specifies how data is written to a table if the table already exists. " +
                "See %nhttps://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information. "
                + OptionsUtil.VALID_VALUES_DESCRIPTION)
        private SaveMode saveMode = SaveMode.ERRORIFEXISTS;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                "dbtable", table
            );
        }

        @Override
        public WriteRowsOptions table(String table) {
            this.table = table;
            return this;
        }

        @Override
        public WriteRowsOptions saveMode(SaveMode saveMode) {
            this.saveMode = saveMode;
            return this;
        }
    }

    @Override
    public JdbcExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public JdbcExporter from(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public JdbcExporter to(Consumer<WriteRowsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.JdbcExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.JdbcParams;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.SparkUtil;
import org.apache.spark.sql.*;

import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read rows via Optic from MarkLogic and write them to a table via JDBC.")
public class ExportJdbcCommand extends AbstractCommand<JdbcExporter> implements JdbcExporter {

    @ParametersDelegate
    private ReadRowsParams readParams = new ReadRowsParams();

    @ParametersDelegate
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
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format("jdbc")
            .options(writeParams.makeOptions())
            .mode(SparkUtil.toSparkSaveMode(writeParams.saveMode))
            .save();
    }

    public static class WriteJdbcParams extends JdbcParams<WriteRowsOptions> implements WriteRowsOptions {

        @Parameter(names = "--table", required = true, description = "The JDBC table that should be written to.")
        private String table;

        @Parameter(names = "--mode",
            description = "Specifies how data is written to a table if the table already exists. " +
                "See https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information.")
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

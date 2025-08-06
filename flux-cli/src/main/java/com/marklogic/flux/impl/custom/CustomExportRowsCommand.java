/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.api.CustomExportWriteOptions;
import com.marklogic.flux.api.CustomRowsExporter;
import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.flux.impl.export.ReadRowsParams;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(
    name = "custom-export-rows",
    description = "Read rows from MarkLogic and write them using a custom Spark connector or data source."
)
public class CustomExportRowsCommand extends AbstractCustomExportCommand<CustomRowsExporter> implements CustomRowsExporter {

    @CommandLine.Mixin
    private ReadRowsParams readParams = new ReadRowsParams();

    @Override
    protected String getCustomSparkMasterUrl() {
        return readParams.getPartitions() > 0 ? SparkUtil.makeSparkMasterUrl(readParams.getPartitions()) : null;
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(AbstractCommand.MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    public CustomRowsExporter from(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public CustomRowsExporter from(String opticQuery) {
        readParams.opticQuery(opticQuery);
        return this;
    }

    @Override
    public CustomRowsExporter to(Consumer<CustomExportWriteOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

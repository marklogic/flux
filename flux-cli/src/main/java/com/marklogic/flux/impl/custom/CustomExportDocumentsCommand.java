/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.api.CustomDocumentsExporter;
import com.marklogic.flux.api.CustomExportWriteOptions;
import com.marklogic.flux.api.ReadDocumentsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.export.ReadDocumentParams;
import com.marklogic.flux.impl.export.ReadDocumentParamsImpl;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.function.Consumer;

@CommandLine.Command(
    name = "custom-export-documents",
    description = "Read documents from MarkLogic and write them using a custom Spark connector or data source."
)
public class CustomExportDocumentsCommand extends AbstractCustomExportCommand<CustomDocumentsExporter> implements CustomDocumentsExporter {

    @CommandLine.Mixin
    private ReadDocumentParamsImpl readParams = new ReadDocumentParamsImpl();

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        super.validateCommandLineOptions(parseResult);
        OptionsUtil.verifyHasAtLeastOneOption(parseResult, ReadDocumentParams.REQUIRED_QUERY_OPTIONS);
    }

    @Override
    protected void validateDuringApiUsage() {
        readParams.verifyAtLeastOneQueryOptionIsSet("export");
    }

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    public CustomDocumentsExporter from(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public CustomDocumentsExporter to(Consumer<CustomExportWriteOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

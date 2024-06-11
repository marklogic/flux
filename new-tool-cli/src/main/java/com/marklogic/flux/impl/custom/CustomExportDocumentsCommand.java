package com.marklogic.flux.impl.custom;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.ReadDocumentsOptions;
import com.marklogic.flux.impl.export.ReadDocumentParamsImpl;
import com.marklogic.flux.api.CustomDocumentsExporter;
import com.marklogic.flux.api.CustomExportWriteOptions;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.function.Consumer;

@Parameters(commandDescription = "Read documents from MarkLogic and write them using a custom Spark connector or data source.")
public class CustomExportDocumentsCommand extends AbstractCustomExportCommand<CustomDocumentsExporter> implements CustomDocumentsExporter {

    @ParametersDelegate
    private ReadDocumentParamsImpl readParams = new ReadDocumentParamsImpl();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    public CustomDocumentsExporter readDocuments(Consumer<ReadDocumentsOptions<? extends ReadDocumentsOptions>> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public CustomDocumentsExporter writeDocuments(Consumer<CustomExportWriteOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

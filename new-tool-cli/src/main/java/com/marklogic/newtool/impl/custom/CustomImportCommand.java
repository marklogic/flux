package com.marklogic.newtool.impl.custom;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.CustomImporter;
import com.marklogic.newtool.api.WriteStructuredDocumentsOptions;
import com.marklogic.newtool.impl.AbstractCommand;
import com.marklogic.newtool.impl.S3Params;
import com.marklogic.newtool.impl.importdata.WriteStructuredDocumentParams;
import org.apache.spark.sql.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read data via a custom Spark connector or data source and write JSON or XML documents to MarkLogic.")
public class CustomImportCommand extends AbstractCommand<CustomImporter> implements CustomImporter {

    @ParametersDelegate
    private CustomReadParams readParams = new CustomReadParams();

    @ParametersDelegate
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        readParams.s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format(readParams.source)
            .options(readParams.additionalOptions)
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    public static class CustomReadParams implements CustomReadOptions {

        @Parameter(names = "--source", description = "Identifier for the Spark connector that is the source of data to import.")
        private String source;

        @DynamicParameter(
            names = "-P",
            description = "Specify any number of options to be passed to the connector identified by '--source'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @ParametersDelegate
        private S3Params s3Params = new S3Params();

        @Override
        public CustomReadOptions source(String source) {
            this.source = source;
            return this;
        }

        @Override
        public CustomReadOptions additionalOptions(Map<String, String> additionalOptions) {
            this.additionalOptions = additionalOptions;
            return this;
        }

        public CustomReadOptions s3AddCredentials() {
            this.s3Params.setAddCredentials(true);
            return this;
        }

        @Override
        public CustomReadOptions s3Endpoint(String endpoint) {
            this.s3Params.setEndpoint(endpoint);
            return this;
        }
    }

    @Override
    public CustomImporter readData(Consumer<CustomReadOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public CustomImporter writeData(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

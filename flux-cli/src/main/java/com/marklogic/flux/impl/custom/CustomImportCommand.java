/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.api.CustomImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.S3Params;
import com.marklogic.flux.impl.importdata.WriteStructuredDocumentParams;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "custom-import",
    abbreviateSynopsis = true,
    description = "Read data via a custom Spark connector or data source and write JSON or XML documents to MarkLogic."
)
public class CustomImportCommand extends AbstractCommand<CustomImporter> implements CustomImporter {

    @CommandLine.Mixin
    private CustomReadParams readParams = new CustomReadParams();

    @CommandLine.Mixin
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

        @CommandLine.Option(names = "--source", description = "Identifier for the Spark connector that is the source of data to import.")
        private String source;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any number of options to be passed to the connector identified by '--source'."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.ArgGroup(exclusive = false)
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
        public CustomReadOptions s3AccessKeyId(String accessKeyId) {
            this.s3Params.setAccessKeyId(accessKeyId);
            return this;
        }

        @Override
        public CustomReadOptions s3SecretAccessKey(String secretAccessKey) {
            this.s3Params.setSecretAccessKey(secretAccessKey);
            return this;
        }

        @Override
        public CustomReadOptions s3Endpoint(String endpoint) {
            this.s3Params.setEndpoint(endpoint);
            return this;
        }
    }

    @Override
    public CustomImporter from(Consumer<CustomReadOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public CustomImporter to(Consumer<WriteStructuredDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

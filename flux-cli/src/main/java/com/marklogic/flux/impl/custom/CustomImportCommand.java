/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.api.CustomImporter;
import com.marklogic.flux.api.WriteStructuredDocumentsOptions;
import com.marklogic.flux.impl.*;
import com.marklogic.flux.impl.importdata.WriteStructuredDocumentParams;
import org.apache.spark.sql.*;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "custom-import",
    description = "Read data via a custom Spark connector or data source and write JSON or XML documents to MarkLogic."
)
public class CustomImportCommand extends AbstractCommand<CustomImporter> implements CustomImporter {

    @CommandLine.Mixin
    private CustomReadParams readParams = new CustomReadParams();

    @CommandLine.Mixin
    private WriteStructuredDocumentParams writeParams = new WriteStructuredDocumentParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        applyCloudStorageParams(session.sparkContext().hadoopConfiguration(), readParams);
        return reader.format(readParams.source)
            .options(readParams.additionalOptions)
            .load();
    }

    @Override
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        TdeHelper.Result result = writeParams.newTdeHelper().logOrLoadTemplate(dataset.schema(), getConnectionParams());
        if (TdeHelper.Result.TEMPLATE_LOGGED.equals(result)) {
            return null;
        }

        return super.afterDatasetLoaded(dataset);
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(writeParams.makeOptions())
            .mode(SaveMode.Append)
            .save();
    }

    public static class CustomReadParams implements CustomReadOptions, CloudStorageParams {

        @CommandLine.Option(
            names = "--source",
            description = "Identifier for the Spark connector or data source that is used to read data.",
            required = true
        )
        private String source;

        @CommandLine.Option(
            names = "--spark-prop",
            description = "Specify any number of options to be passed to the connector identified by '--source' - e.g. --spark-prop myOption=someValue."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.Mixin
        private S3Params s3Params = new S3Params();

        @CommandLine.Mixin
        private AzureStorageParams azureStorageParams = new AzureStorageParams();

        @Override
        public S3Params getS3Params() {
            return s3Params;
        }

        @Override
        public AzureStorageParams getAzureStorageParams() {
            return azureStorageParams;
        }

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

        @Override
        public CustomReadOptions azureStorageAccount(String storageAccount) {
            this.azureStorageParams.storageAccount(storageAccount);
            return this;
        }

        @Override
        public CustomReadOptions azureStorageType(com.marklogic.flux.api.AzureStorageType storageType) {
            this.azureStorageParams.storageType(storageType);
            return this;
        }

        @Override
        public CustomReadOptions azureAccessKey(String accessKey) {
            this.azureStorageParams.accessKey(accessKey);
            return this;
        }

        @Override
        public CustomReadOptions azureSasToken(String sasToken) {
            this.azureStorageParams.sasToken(sasToken);
            return this;
        }

        @Override
        public CustomReadOptions azureSharedKey(String sharedKey) {
            this.azureStorageParams.sharedKey(sharedKey);
            return this;
        }

        @Override
        public CustomReadOptions azureContainerName(String containerName) {
            this.azureStorageParams.containerName(containerName);
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

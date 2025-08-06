/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.custom;

import com.marklogic.flux.api.CustomExportWriteOptions;
import com.marklogic.flux.api.Executor;
import com.marklogic.flux.api.SaveMode;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.flux.impl.S3Params;
import com.marklogic.flux.impl.SparkUtil;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractCustomExportCommand<T extends Executor> extends AbstractCommand<T> {

    @CommandLine.Mixin
    protected final CustomWriteParams writeParams = new CustomWriteParams();

    public static class CustomWriteParams implements CustomExportWriteOptions {

        @CommandLine.Mixin
        private S3Params s3Params = new S3Params();

        @CommandLine.Option(
            names = "--target",
            description = "Identifier for the Spark connector that is the target of data to export.",
            required = true
        )
        private String target;

        @CommandLine.Option(
            names = "-P",
            description = "Specify any number of options to be passed to the connector identified by '--target' - e.g. -PmyOption=someValue."
        )
        private Map<String, String> additionalOptions = new HashMap<>();

        @CommandLine.Option(names = "--mode",
            description = "Specifies how data is written if the path already exists. " +
                "See %nhttps://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/SaveMode.html for more information. "
                + OptionsUtil.VALID_VALUES_DESCRIPTION)
        private SaveMode saveMode = SaveMode.APPEND;

        @Override
        public CustomExportWriteOptions target(String target) {
            this.target = target;
            return this;
        }

        @Override
        public CustomExportWriteOptions additionalOptions(Map<String, String> additionalOptions) {
            this.additionalOptions = additionalOptions;
            return this;
        }

        @Override
        public CustomExportWriteOptions saveMode(SaveMode saveMode) {
            this.saveMode = saveMode;
            return this;
        }

        public CustomExportWriteOptions s3AddCredentials() {
            this.s3Params.setAddCredentials(true);
            return this;
        }

        @Override
        public CustomExportWriteOptions s3AccessKeyId(String accessKeyId) {
            this.s3Params.setAccessKeyId(accessKeyId);
            return this;
        }

        @Override
        public CustomExportWriteOptions s3SecretAccessKey(String secretAccessKey) {
            this.s3Params.setSecretAccessKey(secretAccessKey);
            return this;
        }

        @Override
        public CustomExportWriteOptions s3Endpoint(String endpoint) {
            this.s3Params.setEndpoint(endpoint);
            return this;
        }
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writeParams.s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(writeParams.target)
            .options(writeParams.additionalOptions)
            .mode(SparkUtil.toSparkSaveMode(writeParams.saveMode))
            .save();
    }
}

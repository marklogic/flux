/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

public interface CustomExportWriteOptions {

    CustomExportWriteOptions target(String target);

    CustomExportWriteOptions additionalOptions(Map<String, String> additionalOptions);

    CustomExportWriteOptions saveMode(SaveMode saveMode);

    CustomExportWriteOptions s3AddCredentials();

    CustomExportWriteOptions s3AccessKeyId(String accessKeyId);

    CustomExportWriteOptions s3SecretAccessKey(String secretAccessKey);

    CustomExportWriteOptions s3Endpoint(String endpoint);
}

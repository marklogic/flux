package com.marklogic.newtool.api;

import java.util.Map;

public interface CustomExportWriteOptions {

    CustomExportWriteOptions target(String target);

    CustomExportWriteOptions additionalOptions(Map<String, String> additionalOptions);

    CustomExportWriteOptions saveMode(SaveMode saveMode);

    CustomExportWriteOptions s3AddCredentials();

    CustomExportWriteOptions s3Endpoint(String endpoint);
}

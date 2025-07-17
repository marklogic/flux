/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.impl.AzureStorageParams;
import com.marklogic.flux.impl.S3Params;

/**
 * Interface for writer options that support cloud storage configuration.
 * This allows export commands to access cloud storage parameters in a consistent way.
 */
public interface CloudStorageWriteOptions {
    S3Params getS3Params();

    AzureStorageParams getAzureStorageParams();

    String getPath();
}

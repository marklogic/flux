/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

public interface CloudStorageParams {

    S3Params getS3Params();

    AzureStorageParams getAzureStorageParams();

}

/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface ReadRowsOptions {

    ReadRowsOptions opticQuery(String opticQuery);

    ReadRowsOptions disableAggregationPushDown(boolean value);

    ReadRowsOptions batchSize(int batchSize);

    ReadRowsOptions partitions(int partitions);

    ReadRowsOptions logProgress(int interval);
}

/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

public interface ReadRowsOptions {

    ReadRowsOptions opticQuery(String opticQuery);

    ReadRowsOptions disableAggregationPushDown(boolean value);

    ReadRowsOptions batchSize(int batchSize);

    ReadRowsOptions partitions(int partitions);

    ReadRowsOptions logProgress(int interval);
}

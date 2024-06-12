package com.marklogic.flux.api;

public interface ReadRowsOptions {

    ReadRowsOptions opticQuery(String opticQuery);

    ReadRowsOptions disableAggregationPushDown(Boolean value);

    ReadRowsOptions batchSize(Integer batchSize);

    ReadRowsOptions partitions(Integer partitions);
}

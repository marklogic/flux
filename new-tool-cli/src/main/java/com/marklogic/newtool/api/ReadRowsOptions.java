package com.marklogic.newtool.api;

public interface ReadRowsOptions {

    ReadRowsOptions opticQuery(String opticQuery);

    ReadRowsOptions disableAggregationPushDown(Boolean value);

    ReadRowsOptions batchSize(Integer batchSize);

    ReadRowsOptions partitions(Integer partitions);
}

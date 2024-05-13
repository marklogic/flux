package com.marklogic.newtool.impl.export;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.api.ReadRowsOptions;
import com.marklogic.newtool.impl.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * Defines parameters for reading rows via an Optic query.
 */
public class ReadRowsParams implements ReadRowsOptions {

    @Parameter(names = "--query", description = "The Optic DSL query for retrieving rows; must use op.fromView as an accessor.")
    private String query;

    @Parameter(names = "--batchSize", description = "Approximate number of rows to retrieve in each call to MarkLogic; defaults to 100000.")
    private Integer batchSize;

    // Not yet showing this in usage as it is confusing for a typical user to understand and would only need to be
    // set if push down aggregation is producing incorrect results. See the MarkLogic Spark connector documentation
    // for more information.
    @Parameter(names = "--disableAggregationPushDown", hidden = true)
    private Boolean disableAggregationPushDown;

    @Parameter(names = "--partitions", description = "Number of partitions to create when reading rows from MarkLogic. " +
        "Increasing this may improve performance as the number of rows to read increases.")
    private Integer partitions;

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.READ_OPTIC_QUERY, query,
            Options.READ_BATCH_SIZE, batchSize != null ? batchSize.toString() : null,
            Options.READ_PUSH_DOWN_AGGREGATES, disableAggregationPushDown != null ? Boolean.toString(!disableAggregationPushDown) : null,
            Options.READ_NUM_PARTITIONS, partitions != null ? partitions.toString() : null
        );
    }

    @Override
    public ReadRowsOptions opticQuery(String opticQuery) {
        this.query = opticQuery;
        return this;
    }

    @Override
    public ReadRowsOptions disableAggregationPushDown(Boolean value) {
        this.disableAggregationPushDown = value;
        return this;
    }

    @Override
    public ReadRowsOptions batchSize(Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public ReadRowsOptions partitions(Integer partitions) {
        this.partitions = partitions;
        return this;
    }
}

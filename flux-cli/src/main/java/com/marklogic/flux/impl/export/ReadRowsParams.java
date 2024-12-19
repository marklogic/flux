/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.ReadRowsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;

/**
 * Defines parameters for reading rows via an Optic query.
 */
public class ReadRowsParams implements ReadRowsOptions {

    @CommandLine.Option(
        names = "--query",
        description = "The Optic DSL query for retrieving rows; must use op.fromView as an accessor.",
        required = true
    )
    private String query;

    @CommandLine.Option(names = "--batch-size", description = "Approximate number of rows to retrieve in each call to MarkLogic; defaults to 100000.")
    private int batchSize;

    // Not yet showing this in usage as it is confusing for a typical user to understand and would only need to be
    // set if push down aggregation is producing incorrect results. See the MarkLogic Spark connector documentation
    // for more information.
    @CommandLine.Option(names = "--disable-aggregation-push-down", hidden = true)
    private boolean disableAggregationPushDown;

    // As of 1.2.0, this now affects the Spark master URL. This ensures that if the user e.g. creates 16 partitions for
    // reading rows, then the Spark master URL will specify 16 worker threads. The user may still reconfigure the
    // number of partitions during the writer phase via "--repartition".
    @CommandLine.Option(names = "--partitions", description = "Number of partitions to create when reading rows from MarkLogic. " +
        "Increasing this may improve performance as the number of rows to read increases.")
    private int partitions;

    @CommandLine.Option(
        names = "--log-progress",
        description = "Log a count of total rows read every time this many rows are read."
    )
    private int progressInterval = 100000;

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.READ_OPTIC_QUERY, query,
            Options.READ_BATCH_SIZE, OptionsUtil.intOption(batchSize),
            Options.READ_PUSH_DOWN_AGGREGATES, disableAggregationPushDown ? "true" : null,
            Options.READ_NUM_PARTITIONS, OptionsUtil.intOption(partitions),
            Options.READ_LOG_PROGRESS, OptionsUtil.intOption(progressInterval)
        );
    }

    @Override
    public ReadRowsOptions opticQuery(String opticQuery) {
        this.query = opticQuery;
        return this;
    }

    @Override
    public ReadRowsOptions disableAggregationPushDown(boolean value) {
        this.disableAggregationPushDown = value;
        return this;
    }

    @Override
    public ReadRowsOptions batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    @Override
    public ReadRowsOptions partitions(int partitions) {
        this.partitions = partitions;
        return this;
    }

    @Override
    public ReadRowsOptions logProgress(int interval) {
        this.progressInterval = interval;
        return this;
    }

    public int getPartitions() {
        return partitions;
    }
}

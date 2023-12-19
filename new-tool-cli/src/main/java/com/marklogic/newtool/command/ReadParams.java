package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.HashMap;
import java.util.Map;

/**
 * The idea here is that we can define all the params for reading via Optic and via custom code.
 * This would then be a delegate on every Import class. We'd have the same with WriteParams for every
 * Export class. That allows a user to choose whether to use Optic/custom-code while reading and whether
 * to write documents or process with custom code while writing.
 */
public class ReadParams {

    @Parameter(names = "--query", description = "the Optic DSL query")
    private String query;

    @Parameter(names = "--batchSize")
    private int batchSize;

    @Parameter(names = "--pushDownAggregates")
    private boolean pushDownAggregates = true;

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        options.put(Options.READ_OPTIC_QUERY, query);
        options.put(Options.READ_BATCH_SIZE, batchSize + "");
        options.put(Options.READ_PUSH_DOWN_AGGREGATES, pushDownAggregates + "");
        return options;
    }
}

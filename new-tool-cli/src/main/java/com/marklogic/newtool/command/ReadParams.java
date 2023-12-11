package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
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

    @Parameter(names = "--partitions", description = "Number of partitions to create when executing the Optic query")
    private int partitions = 1;

    @Parameter(names = "--batchSize")
    private int batchSize;

    @Parameter(names = "--pushDownAggregates")
    private boolean pushDownAggregates = true;

    @Parameter(names = "--readInvoke")
    private String readInvoke;

    @Parameter(names = "--readJavascript")
    private String readJavascript;

    @Parameter(names = "--readXquery")
    private String readXquery;

    @Parameter(names = "--readPartitionsInvoke")
    private String readPartitionsInvoke;

    @Parameter(names = "--readPartitionsJavascript")
    private String readPartitionsJavascript;

    @Parameter(names = "--readPartitionsXquery")
    private String readPartitionsXquery;

    @DynamicParameter(names = "-RV:", description = "Variables to pass to custom code for reading rows")
    private Map<String, String> readExternalVariables = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        options.put(Options.READ_OPTIC_QUERY, query);
        options.put(Options.READ_NUM_PARTITIONS, partitions + "");
        options.put(Options.READ_BATCH_SIZE, batchSize + "");
        options.put(Options.READ_PUSH_DOWN_AGGREGATES, pushDownAggregates + "");
        options.put(Options.READ_INVOKE, readInvoke);
        options.put(Options.READ_JAVASCRIPT, readJavascript);
        options.put(Options.READ_XQUERY, readXquery);
        options.put(Options.READ_PARTITIONS_INVOKE, readPartitionsInvoke);
        options.put(Options.READ_PARTITIONS_JAVASCRIPT, readPartitionsJavascript);
        options.put(Options.READ_PARTITIONS_XQUERY, readPartitionsXquery);
        readExternalVariables.forEach((key, value) -> options.put(Options.READ_VARS_PREFIX + key, value));
        return options;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getPartitions() {
        return partitions;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isPushDownAggregates() {
        return pushDownAggregates;
    }

    public void setPushDownAggregates(boolean pushDownAggregates) {
        this.pushDownAggregates = pushDownAggregates;
    }

    public String getReadInvoke() {
        return readInvoke;
    }

    public void setReadInvoke(String readInvoke) {
        this.readInvoke = readInvoke;
    }

    public String getReadJavascript() {
        return readJavascript;
    }

    public void setReadJavascript(String readJavascript) {
        this.readJavascript = readJavascript;
    }

    public String getReadXquery() {
        return readXquery;
    }

    public void setReadXquery(String readXquery) {
        this.readXquery = readXquery;
    }

    public String getReadPartitionsInvoke() {
        return readPartitionsInvoke;
    }

    public void setReadPartitionsInvoke(String readPartitionsInvoke) {
        this.readPartitionsInvoke = readPartitionsInvoke;
    }

    public String getReadPartitionsJavascript() {
        return readPartitionsJavascript;
    }

    public void setReadPartitionsJavascript(String readPartitionsJavascript) {
        this.readPartitionsJavascript = readPartitionsJavascript;
    }

    public String getReadPartitionsXquery() {
        return readPartitionsXquery;
    }

    public void setReadPartitionsXquery(String readPartitionsXquery) {
        this.readPartitionsXquery = readPartitionsXquery;
    }

    public Map<String, String> getReadExternalVariables() {
        return readExternalVariables;
    }

    public void setReadExternalVariables(Map<String, String> readExternalVariables) {
        this.readExternalVariables = readExternalVariables;
    }
}

package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Map;

/**
 * Idea is that "export" = read from MarkLogic using Optic and then do something with the results.
 */
public abstract class AbstractExportCommand extends AbstractCommand {

    @Parameter(names = "--query", description = "the Optic DSL query")
    private String query;

    @Parameter(names = "--partitions", description = "Number of partitions to create when executing the Optic query")
    private int partitions = 1;

    @Parameter(names = "--batch-size")
    private int batchSize;

    @Parameter(names = "--push-down-aggregates")
    private boolean pushDownAggregates = true;

    protected final Map<String, String> makeExportOptions() {
        return super.makeReadOptions(
            Options.READ_OPTIC_QUERY, query,
            Options.READ_NUM_PARTITIONS, partitions + "",
            Options.READ_BATCH_SIZE, batchSize + "",
            Options.READ_PUSH_DOWN_AGGREGATES, pushDownAggregates + ""
        );
    }

    protected final Dataset<Row> read(SparkSession session) {
        return session.read()
            .format(MARKLOGIC_CONNECTOR)
            .options(makeExportOptions())
            .load();
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setPartitions(int partitions) {
        this.partitions = partitions;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setPushDownAggregates(boolean pushDownAggregates) {
        this.pushDownAggregates = pushDownAggregates;
    }
}

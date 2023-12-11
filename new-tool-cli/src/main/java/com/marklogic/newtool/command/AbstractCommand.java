package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Could provide a way to configure hadoopConfiguration for things like this:
 * session.sparkContext().hadoopConfiguration().set("fs.s3a.access.key", creds.getAWSAccessKeyId());
 * session.sparkContext().hadoopConfiguration().set("fs.s3a.secret.key", creds.getAWSSecretKey());
 * <p>
 * Could do something like -HC:key=value, though that wouldn't work for the above which uses a programmatic solution.
 * Though a user could figure out a way to pass in the keys themselves.
 * <p>
 * TODO Should support an options file, where any other options override what's in the file.
 */
public abstract class AbstractCommand implements Command {

    protected final static String MARKLOGIC_CONNECTOR = "com.marklogic.spark";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @DynamicParameter(names = "-R:", description = "Custom options to pass to the reader", hidden = true, order = Integer.MAX_VALUE - 1)
    private Map<String, String> customReadOptions = new HashMap<>();

    @DynamicParameter(names = "-W:", description = "Custom options to pass to the writer", hidden = true, order = Integer.MAX_VALUE)
    private Map<String, String> customWriteOptions = new HashMap<>();

    @Parameter(names = "--partitions", description = "number of Spark partitions")
    private Integer partitions;

    @Parameter(names = "--optionsFile", description = "File path for a file containing newline-delimited options.", hidden = true)
    private String optionsFile;

    @Parameter(names = "--preview", description = "Set to true to log the dataset instead of writing it.")
    private boolean preview;

    // TODO Apply optionsFile here.
    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        long start = System.currentTimeMillis();
        try {
            DataFrameReader reader = session.read()
                .options(makeCommonReadOptions());
            Dataset<Row> dataset = loadDataset(session, reader);
            if (partitions != null) {
                dataset = dataset.repartition(partitions);
            }
            if (preview) {
                // TODO Log N rows?
                return Optional.of(dataset.collectAsList());
            }
            DataFrameWriter<Row> writer = dataset.write()
                .options(makeCommonWriteOptions());
            writeDataset(session, writer);
            return Optional.empty();
        } finally {
            if (logger.isInfoEnabled()) {
                logger.info("Completed, duration in ms: " + (System.currentTimeMillis() - start));
            }
        }
    }

    protected abstract Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader);

    // TODO Keeping this void for now until there's a use case for returning something.
    // Only use case right now is for preview, which is handled before this is invoked.
    // Not certain about this name yet - perhaps applyWriter would be better?
    protected abstract void writeDataset(SparkSession session, DataFrameWriter<Row> writer);

    private Map<String, String> makeCommonReadOptions() {
        Map<String, String> options = connectionParams.makeOptions();
        options.putAll(customReadOptions);
        return options;
    }

    private Map<String, String> makeCommonWriteOptions() {
        Map<String, String> options = connectionParams.makeOptions();
        options.putAll(customWriteOptions);
        return options;
    }

    public ConnectionParams getConnectionParams() {
        return connectionParams;
    }

    public void setConnectionParams(ConnectionParams connectionParams) {
        this.connectionParams = connectionParams;
    }

    public void setCustomReadOptions(Map<String, String> customReadOptions) {
        this.customReadOptions = customReadOptions;
    }

    public void setCustomWriteOptions(Map<String, String> customWriteOptions) {
        this.customWriteOptions = customWriteOptions;
    }

    public Map<String, String> getCustomReadOptions() {
        return customReadOptions;
    }

    public Map<String, String> getCustomWriteOptions() {
        return customWriteOptions;
    }

}

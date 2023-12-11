package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.ParametersDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO Currently defaults to the test app, which won't be the case in the real world.
 * <p>
 * Could provide a way to configure hadoopConfiguration for things like this:
 * session.sparkContext().hadoopConfiguration().set("fs.s3a.access.key", creds.getAWSAccessKeyId());
 * session.sparkContext().hadoopConfiguration().set("fs.s3a.secret.key", creds.getAWSSecretKey());
 * <p>
 * TODO Should support an options file, where any other options override what's in the file.
 */
abstract class AbstractCommand implements Command {

    protected final static String MARKLOGIC_CONNECTOR = "com.marklogic.spark";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @ParametersDelegate
    private ConnectionParams connectionParams = new ConnectionParams();

    @DynamicParameter(names = "-R:", description = "Custom options to pass to the reader", hidden = true, order = Integer.MAX_VALUE - 1)
    private Map<String, String> customReadOptions = new HashMap<>();

    @DynamicParameter(names = "-W:", description = "Custom options to pass to the writer", hidden = true, order = Integer.MAX_VALUE)
    private Map<String, String> customWriteOptions = new HashMap<>();

    protected final Map<String, String> makeWriteOptions() {
        Map<String, String> options = connectionParams.makeOptions();
        customWriteOptions.forEach((key, value) -> options.put(key, value));
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

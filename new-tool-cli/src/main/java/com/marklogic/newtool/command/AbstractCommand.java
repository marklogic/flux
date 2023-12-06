package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO Currently defaults to the test app, which won't be the case in the real world.
 *
 * Could provide a way to configure hadoopConfiguration for things like this:
 * session.sparkContext().hadoopConfiguration().set("fs.s3a.access.key", creds.getAWSAccessKeyId());
 * session.sparkContext().hadoopConfiguration().set("fs.s3a.secret.key", creds.getAWSSecretKey());
 */
abstract class AbstractCommand implements Command {

    protected final static String MARKLOGIC_CONNECTOR = "com.marklogic.spark";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Parameter(names = {"-h", "--host"}, description = "The MarkLogic host to connect to", hidden = true)
    private String host = "localhost";

    @Parameter(names = "--port", description = "Port of a MarkLogic app server to connect to", hidden = true)
    private Integer port = 8003;

    @Parameter(names = "--username", description = "Username when using 'digest' or 'basic' authentication", hidden = true)
    private String username = "new-tool-user";

    @Parameter(names = "--password", description = "Password when using 'digest' or 'basic' authentication", hidden = true)
    private String password = "password";

    @DynamicParameter(names = "-R:", description = "Custom options to pass to the reader", hidden = true)
    private Map<String, String> customReadOptions = new HashMap<>();

    @DynamicParameter(names = "-W:", description = "Custom options to pass to the writer", hidden = true)
    private Map<String, String> customWriteOptions = new HashMap<>();

    protected final Map<String, String> makeConnectionOptions() {
        return new HashMap() {{
            put("spark.marklogic.client.host", getHost());
            put("spark.marklogic.client.port", getPort() + "");
            put("spark.marklogic.client.username", getUsername());
            put("spark.marklogic.client.password", getPassword());
        }};
    }

    protected final Map<String, String> makeReadOptions(String... namesAndValues) {
        Map<String, String> options = makeConnectionOptions();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            String value = namesAndValues[i + 1];
            if (value != null && value.trim().length() > 0) {
                options.put(namesAndValues[i], value.trim());
            }
        }
        customReadOptions.forEach((key, value) -> options.put(key, value));
        return options;
    }

    protected final Map<String, String> makeWriteOptions(String... namesAndValues) {
        Map<String, String> options = makeConnectionOptions();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            String value = namesAndValues[i + 1];
            if (value != null && value.trim().length() > 0) {
                options.put(namesAndValues[i], value.trim());
            }
        }
        customWriteOptions.forEach((key, value) -> options.put(key, value));
        return options;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, String> getCustomReadOptions() {
        return customReadOptions;
    }

    public Map<String, String> getCustomWriteOptions() {
        return customWriteOptions;
    }
}

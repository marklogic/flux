package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.HashMap;
import java.util.Map;

public class ConnectionParams {

    @Parameter(names = {"--clientUri"},
        hidden = true,
        description = "Defines a connection string as user:password@host:port; only usable when using digest or basic authentication.",
        order = 0)
    private String clientUri;

    @Parameter(names = {"-h", "--host"}, description = "The MarkLogic host to connect to", hidden = true, order = 1)
    private String host = "localhost";

    @Parameter(names = "--port", description = "Port of a MarkLogic REST API app server to connect to", hidden = true, order = 2)
    private Integer port;

    @Parameter(names = "--username", description = "Username when using 'digest' or 'basic' authentication",
        hidden = true, order = 3)
    private String username;

    @Parameter(names = "--password", description = "Password when using 'digest' or 'basic' authentication",
        hidden = true, password = true, order = 4)
    private String password;

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        if (clientUri != null && !clientUri.isEmpty()) {
            options.put(Options.CLIENT_URI, clientUri);
        } else {
            options.put(Options.CLIENT_HOST, host);
            if (port != null) {
                options.put(Options.CLIENT_PORT, port.toString());
            }
            options.put(Options.CLIENT_USERNAME, username);
            options.put(Options.CLIENT_PASSWORD, password);
        }
        return options;
    }
}

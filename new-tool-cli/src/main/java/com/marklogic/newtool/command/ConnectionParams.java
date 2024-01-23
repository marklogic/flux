package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;

import java.util.Map;

public class ConnectionParams {

    @Parameter(names = {"--clientUri"},
        description = "Defines a connection string as user:password@host:port; only usable when using digest or basic authentication."
    )
    private String clientUri;

    @Parameter(names = {"-h", "--host"}, description = "The MarkLogic host to connect to.")
    private String host = "localhost";

    @Parameter(names = "--port", description = "Port of a MarkLogic REST API app server to connect to.")
    private Integer port;

    @Parameter(names = "--username", description = "Username when using 'digest' or 'basic' authentication.")
    private String username;

    @Parameter(names = "--password", description = "Password when using 'digest' or 'basic' authentication.", password = true)
    private String password;

    public Map<String, String> makeOptions() {
        if (clientUri != null && !clientUri.isEmpty()) {
            return OptionsUtil.makeOptions(Options.CLIENT_URI, clientUri);
        }
        return OptionsUtil.makeOptions(
            Options.CLIENT_HOST, host,
            Options.CLIENT_PORT, port != null ? port.toString() : null,
            Options.CLIENT_USERNAME, username,
            Options.CLIENT_PASSWORD, password
        );
    }
}

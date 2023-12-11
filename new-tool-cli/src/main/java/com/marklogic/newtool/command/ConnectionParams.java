package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;

import java.util.HashMap;
import java.util.Map;

public class ConnectionParams {

    @Parameter(names = {"-h", "--host"}, description = "The MarkLogic host to connect to", hidden = true, order = 0)
    private String host = "localhost";

    @Parameter(names = "--port", description = "Port of a MarkLogic app server to connect to", hidden = true, order = 1)
    private Integer port = 8003;

    @Parameter(names = "--username", description = "Username when using 'digest' or 'basic' authentication",
        hidden = true, order = 2)
    private String username = "new-tool-user";

    @Parameter(names = "--password", description = "Password when using 'digest' or 'basic' authentication",
        hidden = true, password = true, order = 3)
    private String password = "password";

    public Map<String, String> makeOptions() {
        Map<String, String> options = new HashMap<>();
        options.put("spark.marklogic.client.host", getHost());
        options.put("spark.marklogic.client.port", getPort() + "");
        options.put("spark.marklogic.client.username", getUsername());
        options.put("spark.marklogic.client.password", getPassword());
        return options;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

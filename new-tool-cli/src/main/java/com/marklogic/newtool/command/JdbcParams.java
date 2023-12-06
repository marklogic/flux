package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;

import java.util.Properties;

/**
 * TODO Additional inputs are possible when calling read().jdbc(...).
 */
public class JdbcParams {

    @Parameter(names = "--jdbc-url", required = true)
    private String url;

    @Parameter(names = "--jdbc-table", required = true)
    private String table;

    @Parameter(names = "--jdbc-driver", required = true)
    private String driver;

    @Parameter(names = "--jdbc-user")
    private String user;

    @Parameter(names = "--jdbc-password")
    private String password = "";

    public Properties toProperties() {
        Properties props = new Properties();
        props.setProperty("driver", driver);
        props.setProperty("user", user);
        props.setProperty("password", password);
        return props;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getTable() {
        return table;
    }

    public String getDriver() {
        return driver;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}

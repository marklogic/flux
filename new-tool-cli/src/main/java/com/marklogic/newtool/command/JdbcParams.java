package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;

import java.util.Properties;

/**
 * TODO Additional inputs are possible when calling read().jdbc(...).
 */
public class JdbcParams {

    @Parameter(names = "--jdbcUrl", required = true)
    private String url;

    @Parameter(names = "--jdbcTable", required = true)
    private String table;

    @Parameter(names = "--jdbcDriver", required = true)
    private String driver;

    @Parameter(names = "--jdbcUser")
    private String user;

    @Parameter(names = "--jdbcPassword")
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

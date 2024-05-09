package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.marklogic.newtool.api.JdbcOptions;

import java.util.HashMap;
import java.util.Map;

public class JdbcParams<T extends JdbcOptions> implements JdbcOptions<T> {

    @Parameter(names = "--jdbcUrl", required = true, description = "The JDBC URL to connect to.")
    private String url;

    @Parameter(names = "--jdbcDriver", required = true, description = "The class name of the JDBC driver to use.")
    private String driver;

    @Parameter(names = "--jdbcUser", description = "The user to authenticate as, if not specified in the JDBC URL.")
    private String user;

    @Parameter(names = "--jdbcPassword", description = "The password to user for authentication, if not specified in the JDBC URL.")
    private String password;

    @DynamicParameter(
        names = "-P",
        description = "Specify any Spark JDBC option defined at " +
            "https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html; e.g. -Pfetchsize=100."
    )
    private Map<String, String> additionalOptions = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = OptionsUtil.makeOptions(
            "url", url,
            "driver", driver,
            "user", user,
            "password", password
        );
        options.putAll(additionalOptions);
        return options;
    }

    @Override
    public T url(String url) {
        this.url = url;
        return (T) this;
    }

    @Override
    public T driver(String driver) {
        this.driver = driver;
        return (T) this;
    }

    @Override
    public T user(String user) {
        this.user = user;
        return (T) this;
    }

    @Override
    public T password(String password) {
        this.password = password;
        return (T) this;
    }

    @Override
    public T additionalOptions(Map<String, String> additionalOptions) {
        this.additionalOptions = additionalOptions;
        return (T) this;
    }
}

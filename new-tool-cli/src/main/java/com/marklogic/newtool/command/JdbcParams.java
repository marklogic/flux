package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;

import java.util.HashMap;
import java.util.Map;

public class JdbcParams {

    @Parameter(names = "--jdbcUrl", required = true, description = "The JDBC URL to connect to.")
    private String url;

    @Parameter(names = "--jdbcDriver", required = true, description = "The class name of the JDBC driver to use.")
    private String driver;

    @Parameter(names = "--jdbcUser", description = "The user to authenticate as, if not specified in the JDBC URL.")
    private String user;

    @Parameter(names = "--jdbcPassword", description = "The password to user for authentication, if not specified in the JDBC URL.")
    private String password;

    @DynamicParameter(names = "-P", description = "Specify any Spark JDBC option defined at " +
        "https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html as -PoptionName=optionValue; e.g. -Pfetchsize=100.")
    private Map<String, String> dynamicParams = new HashMap<>();

    public Map<String, String> makeOptions() {
        Map<String, String> options = OptionsUtil.makeOptions(
            "url", url,
            "driver", driver,
            "user", user,
            "password", password
        );
        options.putAll(dynamicParams);
        return options;
    }
}

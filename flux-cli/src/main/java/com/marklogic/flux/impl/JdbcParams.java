/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.JdbcOptions;
import picocli.CommandLine;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class JdbcParams<T extends JdbcOptions> implements JdbcOptions<T> {

    @CommandLine.Option(names = "--jdbc-url", required = true, description = "The JDBC URL to connect to.")
    private String url;

    @CommandLine.Option(names = "--jdbc-driver", description = "The fully qualified Java class name of the JDBC driver to use.")
    private String driver;

    @CommandLine.Option(names = "--jdbc-user", description = "The user to authenticate as, if not specified in the JDBC URL.")
    private String user;

    @CommandLine.Option(
        names = "--jdbc-password",
        description = "The password to use for authentication, if not specified in the JDBC URL.",
        interactive = true,
        arity = "0..1"
    )
    private String password;

    @CommandLine.Option(
        names = "--spark-prop",
        description = "Specify any Spark JDBC option defined at " +
            "%nhttps://spark.apache.org/docs/latest/sql-data-sources-jdbc.html; e.g. --spark-prop fetchsize=100 ."
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

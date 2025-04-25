/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

public abstract class PostgresUtil {

    public static final String URL = "jdbc:postgresql://localhost/dvdrental";
    public static final String DRIVER = "org.postgresql.Driver";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";
    public static final String URL_WITH_AUTH = String.format("%s?user=%s&password=%s", URL, USER, PASSWORD);

    private PostgresUtil() {
    }
}

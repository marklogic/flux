package com.marklogic.newtool.command;

public interface PostgresUtil {

    String URL = "jdbc:postgresql://localhost/dvdrental";
    String DRIVER = "org.postgresql.Driver";
    String USER = "postgres";
    String PASSWORD = "postgres";
    String URL_WITH_AUTH = String.format("%s?user=%s&password=%s", URL, USER, PASSWORD);
}

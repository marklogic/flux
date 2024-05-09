package com.marklogic.newtool.api;

import java.util.Map;

public interface JdbcOptions<T extends JdbcOptions> {

    T url(String url);

    T driver(String driver);

    T user(String user);

    T password(String password);

    T additionalOptions(Map<String, String> additionalOptions);
}

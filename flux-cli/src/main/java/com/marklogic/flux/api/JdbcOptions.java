/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

public interface JdbcOptions<T extends JdbcOptions> {

    T url(String url);

    T driver(String driver);

    T user(String user);

    T password(String password);

    T additionalOptions(Map<String, String> additionalOptions);
}

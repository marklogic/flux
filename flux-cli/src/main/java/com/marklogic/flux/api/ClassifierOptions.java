/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

/**
 * @since 1.3.0
 */
public interface ClassifierOptions {

    ClassifierOptions host(String host);

    ClassifierOptions http();

    ClassifierOptions port(int port);

    ClassifierOptions path(String endpoint);

    ClassifierOptions apiKey(String apiKey);

    ClassifierOptions tokenPath(String tokenEndpoint);
}

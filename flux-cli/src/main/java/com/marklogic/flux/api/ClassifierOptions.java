/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

import java.util.Map;

/**
 * @since 1.3.0
 */
public interface ClassifierOptions {

    /**
     * @param host Hostname of the classifier service.
     */
    ClassifierOptions host(String host);

    /**
     * Specifies the use of HTTP instead of HTTPS for the classifier service.
     */
    ClassifierOptions http();

    /**
     * @param port Port number of the classifier service.
     */
    ClassifierOptions port(int port);

    /**
     * @param path Path of the classifier service.
     */
    ClassifierOptions path(String path);

    /**
     * @param apiKey API key granting access to the classifier service when hosted in Progress Data Cloud.
     */
    ClassifierOptions apiKey(String apiKey);

    /**
     * @param tokenPath Path of the token generator for the classifier service when hosted in Progress Data Cloud.
     */
    ClassifierOptions tokenPath(String tokenPath);

    /**
     * @param batchSize Number of documents and/or chunks of text to send to the classifier service in a single request.
     */
    ClassifierOptions batchSize(int batchSize);

    /**
     * @param additionalOptions Additional options for configuring the behavior of the classifier service.
     */
    ClassifierOptions additionalOptions(Map<String, String> additionalOptions);
}

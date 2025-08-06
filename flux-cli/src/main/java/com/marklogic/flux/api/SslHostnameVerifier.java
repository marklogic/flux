/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.api;

// Need this as the Java Client's SSLHostnameVerifier isn't an enum.
public enum SslHostnameVerifier {
    ANY,
    COMMON,
    STRICT
}

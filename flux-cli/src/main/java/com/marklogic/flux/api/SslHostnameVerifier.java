/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

// Need this as the Java Client's SSLHostnameVerifier isn't an enum.
public enum SslHostnameVerifier {
    ANY,
    COMMON,
    STRICT
}

package com.marklogic.flux.api;

// Need this as the Java Client's SSLHostnameVerifier isn't an enum.
public enum SslHostnameVerifier {
    ANY,
    COMMON,
    STRICT
}

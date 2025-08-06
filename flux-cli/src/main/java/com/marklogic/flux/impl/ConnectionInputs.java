/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.AuthenticationType;
import com.marklogic.flux.api.FluxException;
import com.marklogic.flux.api.SslHostnameVerifier;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;

/**
 * Defines all inputs for creating a connection to a REST API app server. Subclasses are expected to add setters
 * with parameter annotations.
 */
public abstract class ConnectionInputs {

    public enum ConnectionType {DIRECT, GATEWAY}

    public static class ConnectionStringValidator implements CommandLine.ITypeConverter<String> {

        @Override
        public String convert(String value) {
            try {
                new ConnectionString(value, "connection string");
            } catch (IllegalArgumentException e) {
                // See https://picocli.info/#_handling_invalid_input .
                throw new CommandLine.TypeConversionException(e.getMessage());
            }
            return value;
        }
    }

    protected String connectionString;
    protected String host;
    protected int port;
    protected String basePath;
    protected String database;
    protected ConnectionType connectionType;
    protected boolean disableGzippedResponses;
    protected AuthenticationType authType;
    protected String username;
    protected String password;
    protected String certificateFile;
    protected String certificatePassword;
    protected String cloudApiKey;
    protected String kerberosPrincipal;
    protected String samlToken;
    protected String oauthToken;
    protected String sslProtocol;
    protected SslHostnameVerifier sslHostnameVerifier;
    protected String keyStorePath;
    protected String keyStorePassword;
    protected String keyStoreType;
    protected String keyStoreAlgorithm;
    protected String trustStorePath;
    protected String trustStorePassword;
    protected String trustStoreType;
    protected String trustStoreAlgorithm;

    public String getSelectedHost() {
        if (connectionString != null) {
            return new ConnectionString(connectionString, "--connection-string").getHost();
        }
        return host;
    }

    /**
     * Used by the API to eagerly fail on an invalid connection string and provide a meaningful error message.
     */
    public void validateConnectionString(String inputNameForErrorMessage) {
        if (connectionString != null && !connectionString.trim().isEmpty()) {
            try {
                new ConnectionString(connectionString, inputNameForErrorMessage);
            } catch (IllegalArgumentException e) {
                throw new FluxException(e.getMessage());
            }
        }
    }

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.CLIENT_URI, connectionString,
            Options.CLIENT_HOST, host,
            Options.CLIENT_PORT, OptionsUtil.intOption(port),
            "spark.marklogic.client.disableGzippedResponses", disableGzippedResponses ? "true" : null,
            "spark.marklogic.client.basePath", basePath,
            Options.CLIENT_DATABASE, database,
            Options.CLIENT_CONNECTION_TYPE, connectionType != null ? connectionType.name() : null,
            Options.CLIENT_AUTH_TYPE, authType != null ? authType.name().toLowerCase() : null,
            Options.CLIENT_USERNAME, username,
            Options.CLIENT_PASSWORD, password,
            "spark.marklogic.client.certificate.file", certificateFile,
            "spark.marklogic.client.certificate.password", certificatePassword,
            "spark.marklogic.client.cloud.apiKey", cloudApiKey,
            "spark.marklogic.client.kerberos.principal", kerberosPrincipal,
            "spark.marklogic.client.saml.token", samlToken,
            "spark.marklogic.client.oauth.token", oauthToken,
            "spark.marklogic.client.sslProtocol", sslProtocol,
            "spark.marklogic.client.sslHostnameVerifier", sslHostnameVerifier != null ? sslHostnameVerifier.name() : null,
            "spark.marklogic.client.ssl.keystore.path", keyStorePath,
            "spark.marklogic.client.ssl.keystore.password", keyStorePassword,
            "spark.marklogic.client.ssl.keystore.type", keyStoreType,
            "spark.marklogic.client.ssl.keystore.algorithm", keyStoreAlgorithm,
            "spark.marklogic.client.ssl.truststore.path", trustStorePath,
            "spark.marklogic.client.ssl.truststore.password", trustStorePassword,
            "spark.marklogic.client.ssl.truststore.type", trustStoreType,
            "spark.marklogic.client.ssl.truststore.algorithm", trustStoreAlgorithm
        );
    }
}

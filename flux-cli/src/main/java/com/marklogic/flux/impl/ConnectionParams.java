/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.api.AuthenticationType;
import com.marklogic.flux.api.ConnectionOptions;
import com.marklogic.flux.api.SslHostnameVerifier;
import picocli.CommandLine;

public class ConnectionParams extends ConnectionInputs implements ConnectionOptions {

    @Override
    @CommandLine.Option(
        names = "--connection-string",
        converter = ConnectionStringValidator.class,
        description = "Defines a connection string as user:password@host:port/optionalDatabaseName; only usable when using 'DIGEST' or 'BASIC' authentication."
    )
    public ConnectionOptions connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = {"--host"},
        description = "The MarkLogic host to connect to."
    )
    public ConnectionOptions host(String host) {
        this.host = host;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--port",
        description = "Port of a MarkLogic REST API app server to connect to."
    )
    public ConnectionOptions port(int port) {
        this.port = port;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--base-path",
        description = "Path to prepend to each call to a MarkLogic REST API app server."
    )
    public ConnectionOptions basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--database",
        description = "Name of a database to connect to if it differs from the one associated with the app server identified by '--port'."
    )
    public ConnectionOptions database(String database) {
        this.database = database;
        return this;
    }

    @CommandLine.Option(
        names = "--connection-type",
        description = "Set to 'DIRECT' if connections can be made directly to each host in the MarkLogic cluster. Defaults to 'GATEWAY'. " + OptionsUtil.VALID_VALUES_DESCRIPTION
    )
    public ConnectionOptions connectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
        return this;
    }

    @Override
    public ConnectionOptions connectionType(String connectionType) {
        return connectionType(ConnectionType.valueOf(connectionType));
    }

    @Override
    @CommandLine.Option(
        names = "--disable-gzipped-responses",
        description = "If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small."
    )
    public ConnectionOptions disableGzippedResponses(boolean disableGzippedResponses) {
        this.disableGzippedResponses = disableGzippedResponses;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--auth-type",
        description = "Type of authentication to use. " + OptionsUtil.VALID_VALUES_DESCRIPTION
    )
    public ConnectionOptions authenticationType(AuthenticationType authType) {
        this.authType = authType;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--username",
        description = "Username when using 'DIGEST' or 'BASIC' authentication."
    )
    public ConnectionOptions username(String username) {
        this.username = username;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--password",
        description = "Password when using 'DIGEST' or 'BASIC' authentication.",
        interactive = true,
        arity = "0..1"
    )
    public ConnectionOptions password(String password) {
        this.password = password;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--certificate-file",
        description = "File path for a keystore to be used for 'CERTIFICATE' authentication."
    )
    public ConnectionOptions certificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--certificate-password",
        description = "Password for the keystore referenced by '--certificate-file'.",
        interactive = true,
        arity = "0..1"
    )
    public ConnectionOptions certificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--cloud-api-key",
        description = "API key for authenticating with a MarkLogic Cloud cluster when using 'CLOUD' authentication."
    )
    public ConnectionOptions cloudApiKey(String cloudApiKey) {
        this.cloudApiKey = cloudApiKey;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--kerberos-principal",
        description = "Principal to be used with 'KERBEROS' authentication."
    )
    public ConnectionOptions kerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--saml-token",
        description = "Token to be used with 'SAML' authentication."
    )
    public ConnectionOptions samlToken(String samlToken) {
        this.samlToken = samlToken;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--oauth-token",
        description = "Token to be used with 'OAUTH' authentication."
    )
    public ConnectionOptions oauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--ssl-protocol",
        description = "SSL protocol to use when the MarkLogic app server requires an SSL connection. If a keystore " +
            "or truststore is configured, defaults to 'TLSv1.2'."
    )
    public ConnectionOptions sslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--ssl-hostname-verifier",
        description = "Hostname verification strategy when connecting via SSL. " + OptionsUtil.VALID_VALUES_DESCRIPTION
    )
    public ConnectionOptions sslHostnameVerifier(SslHostnameVerifier sslHostnameVerifier) {
        this.sslHostnameVerifier = sslHostnameVerifier;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--keystore-path",
        description = "File path for a keystore for two-way SSL connections."
    )
    public ConnectionOptions keyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--keystore-password",
        description = "Password for the keystore identified by '--keystore-path'.",
        interactive = true,
        arity = "0..1"
    )
    public ConnectionOptions keyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--keystore-type",
        description = "Type of the keystore identified by '--keystore-path'; defaults to 'JKS'."
    )
    public ConnectionOptions keyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--keystore-algorithm",
        description = "Algorithm of the keystore identified by '--keystore-path'; defaults to 'SunX509'."
    )
    public ConnectionOptions keyStoreAlgorithm(String keyStoreAlgorithm) {
        this.keyStoreAlgorithm = keyStoreAlgorithm;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--truststore-path",
        description = "File path for a truststore for establishing trust with the certificate used by the MarkLogic app server."
    )
    public ConnectionOptions trustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--truststore-password",
        description = "Password for the truststore identified by '--truststore-path'.",
        interactive = true,
        arity = "0..1"
    )
    public ConnectionOptions trustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--truststore-type",
        description = "Type of the truststore identified by '--truststore-path'; defaults to 'JKS'."
    )
    public ConnectionOptions trustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
        return this;
    }

    @Override
    @CommandLine.Option(
        names = "--truststore-algorithm",
        description = "Algorithm of the truststore identified by '--truststore-path'; defaults to 'SunX509'."
    )
    public ConnectionOptions trustStoreAlgorithm(String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
        return this;
    }
}

package com.marklogic.newtool.impl;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.client.DatabaseClient;
import com.marklogic.newtool.api.AuthenticationType;
import com.marklogic.newtool.api.ConnectionOptions;
import com.marklogic.newtool.api.SslHostnameVerifier;

@Parameters(parametersValidators = ConnectionParamsValidator.class)
public class ConnectionParams extends ConnectionInputs implements ConnectionOptions {

    @Override
    @Parameter(
        names = {"--connection-string"},
        description = "Defines a connection string as user:password@host:port; only usable when using 'DIGEST' or 'BASIC' authentication.",
        validateWith = ConnectionStringValidator.class
    )
    public ConnectionOptions connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    @Override
    @Parameter(
        names = {"--host"},
        description = "The MarkLogic host to connect to."
    )
    public ConnectionOptions host(String host) {
        this.host = host;
        return this;
    }

    @Override
    @Parameter(
        names = "--port",
        description = "Port of a MarkLogic REST API app server to connect to."
    )
    public ConnectionOptions port(int port) {
        this.port = port;
        return this;
    }

    @Override
    @Parameter(
        names = "--base-path",
        description = "Path to prepend to each call to a MarkLogic REST API app server."
    )
    public ConnectionOptions basePath(String basePath) {
        this.basePath = basePath;
        return this;
    }

    @Override
    @Parameter(
        names = "--database",
        description = "Name of a database to connect if it differs from the one associated with the app server identified by '--port'."
    )
    public ConnectionOptions database(String database) {
        this.database = database;
        return this;
    }

    @Parameter(
        names = "--connection-type",
        description = "Defines whether connections can be made directly to each host in the MarkLogic cluster."
    )
    public ConnectionOptions connectionType(DatabaseClient.ConnectionType connectionType) {
        this.connectionType = connectionType;
        return this;
    }

    @Override
    public ConnectionOptions connectionType(String connectionType) {
        return connectionType(DatabaseClient.ConnectionType.valueOf(connectionType));
    }

    @Override
    @Parameter(
        names = "--disable-gzipped-responses",
        description = "If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small."
    )
    public ConnectionOptions disableGzippedResponses(Boolean disableGzippedResponses) {
        this.disableGzippedResponses = disableGzippedResponses;
        return this;
    }

    @Override
    @Parameter(
        names = "--auth-type",
        description = "Type of authentication to use."
    )
    public ConnectionOptions authenticationType(AuthenticationType authType) {
        this.authType = authType;
        return this;
    }

    @Override
    @Parameter(
        names = "--username",
        description = "Username when using 'DIGEST' or 'BASIC' authentication."
    )
    public ConnectionOptions username(String username) {
        this.username = username;
        return this;
    }

    @Override
    @Parameter(
        names = "--password",
        description = "Password when using 'DIGEST' or 'BASIC' authentication.",
        password = true
    )
    public ConnectionOptions password(String password) {
        this.password = password;
        return this;
    }

    @Override
    @Parameter(
        names = "--certificate-file",
        description = "File path for a keystore to be used for 'CERTIFICATE' authentication."
    )
    public ConnectionOptions certificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
        return this;
    }

    @Override
    @Parameter(
        names = "--certificate-password",
        description = "Password for the keystore referenced by '--certificate-file'."
    )
    public ConnectionOptions certificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
        return this;
    }

    @Override
    @Parameter(
        names = "--cloud-api-key",
        description = "API key for authenticating with a MarkLogic Cloud cluster."
    )
    public ConnectionOptions cloudApiKey(String cloudApiKey) {
        this.cloudApiKey = cloudApiKey;
        return this;
    }

    @Override
    @Parameter(
        names = "--kerberos-principal",
        description = "Principal to be used with 'KERBEROS' authentication."
    )
    public ConnectionOptions kerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
        return this;
    }

    @Override
    @Parameter(
        names = "--saml-token",
        description = "Token to be used with 'SAML' authentication."
    )
    public ConnectionOptions samlToken(String samlToken) {
        this.samlToken = samlToken;
        return this;
    }

    @Override
    @Parameter(
        names = "--ssl-protocol",
        description = "SSL protocol to use when the MarkLogic app server requires an SSL connection. If a keystore " +
            "or truststore is configured, defaults to 'TLSv1.2'."
    )
    public ConnectionOptions sslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
        return this;
    }

    @Override
    @Parameter(
        names = "--ssl-hostname-verifier",
        description = "Hostname verification strategy when connecting via SSL."
    )
    public ConnectionOptions sslHostnameVerifier(SslHostnameVerifier sslHostnameVerifier) {
        this.sslHostnameVerifier = sslHostnameVerifier;
        return this;
    }

    @Override
    @Parameter(
        names = "--keystore-path",
        description = "File path for a keystore for two-way SSL connections."
    )
    public ConnectionOptions keyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
        return this;
    }

    @Override
    @Parameter(
        names = "--keystore-password",
        description = "Password for the keystore identified by '--keystore-path'."
    )
    public ConnectionOptions keyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    @Override
    @Parameter(
        names = "--keystore-type",
        description = "Type of the keystore identified by '--keystore-path'; defaults to 'JKS'."
    )
    public ConnectionOptions keyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
        return this;
    }

    @Override
    @Parameter(
        names = "--keystore-algorithm",
        description = "Algorithm of the keystore identified by '--keystore-path'; defaults to 'SunX509'."
    )
    public ConnectionOptions keyStoreAlgorithm(String keyStoreAlgorithm) {
        this.keyStoreAlgorithm = keyStoreAlgorithm;
        return this;
    }

    @Override
    @Parameter(
        names = "--truststore-path",
        description = "File path for a truststore for establishing trust with the certificate used by the MarkLogic app server."
    )
    public ConnectionOptions trustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
        return this;
    }

    @Override
    @Parameter(
        names = "--truststore-password",
        description = "Password for the truststore identified by '--truststore-path'."
    )
    public ConnectionOptions trustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    @Override
    @Parameter(
        names = "--truststore-type",
        description = "Type of the truststore identified by '--truststore-path'; defaults to 'JKS'."
    )
    public ConnectionOptions trustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
        return this;
    }

    @Override
    @Parameter(
        names = "--truststore-algorithm",
        description = "Algorithm of the truststore identified by '--truststore-path'; defaults to 'SunX509'."
    )
    public ConnectionOptions trustStoreAlgorithm(String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
        return this;
    }
}

package com.marklogic.newtool.command.copy;

import com.beust.jcommander.Parameter;
import com.marklogic.client.DatabaseClient;
import com.marklogic.newtool.command.ConnectionInputs;

/**
 * Defines all inputs with a "--output" prefix so it can be used in {@code CopyCommand} without conflicting with
 * all of the args in {@code ConnectionParams}.
 */
public class OutputConnectionParams extends ConnectionInputs {

    @Parameter(
        names = {"--outputConnectionString"},
        description = "Defines a connection string as user:password@host:port; only usable when using 'DIGEST' or 'BASIC' authentication."
    )
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Parameter(
        names = {"--outputHost"},
        description = "The MarkLogic host to connect to."
    )
    public void setHost(String host) {
        this.host = host;
    }

    @Parameter(
        names = "--outputPort",
        description = "Port of a MarkLogic REST API app server to connect to."
    )
    public void setPort(Integer port) {
        this.port = port;
    }

    @Parameter(
        names = "--outputBasePath",
        description = "Path to prepend to each call to a MarkLogic REST API app server."
    )
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Parameter(
        names = "--outputDatabase",
        description = "Name of a database to connect if it differs from the one associated with the app server identified by 'port'."
    )
    public void setDatabase(String database) {
        this.database = database;
    }

    @Parameter(
        names = "--outputConnectionType",
        description = "Defines whether connections can be made directly to each host in the MarkLogic cluster."
    )
    public void setConnectionType(DatabaseClient.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    // disableGzippedResponses doesn't apply for the output connection for a COPY procedure.

    @Parameter(
        names = "--outputAuthType",
        description = "Type of authentication to use."
    )
    public void setAuthType(AuthenticationType authType) {
        this.authType = authType;
    }

    @Parameter(
        names = "--outputUsername",
        description = "Username when using 'DIGEST' or 'BASIC' authentication."
    )
    public void setUsername(String username) {
        this.username = username;
    }

    @Parameter(
        names = "--outputPassword",
        description = "Password when using 'DIGEST' or 'BASIC' authentication.",
        password = true
    )
    public void setPassword(String password) {
        this.password = password;
    }

    @Parameter(
        names = "--outputCertificateFile",
        description = "File path for a key store to be used for 'CERTIFICATE' authentication."
    )
    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    @Parameter(
        names = "--outputCertificatePassword",
        description = "Password for the key store referenced by '--certificateFile'."
    )
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    @Parameter(
        names = "--outputCloudApiKey",
        description = "API key for authenticating with a MarkLogic Cloud cluster."
    )
    public void setCloudApiKey(String cloudApiKey) {
        this.cloudApiKey = cloudApiKey;
    }

    @Parameter(
        names = "--outputKerberosPrincipal",
        description = "Principal to be used with 'KERBEROS' authentication."
    )
    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }

    @Parameter(
        names = "--outputSamlToken",
        description = "Token to be used with 'SAML' authentication."
    )
    public void setSamlToken(String samlToken) {
        this.samlToken = samlToken;
    }

    @Parameter(
        names = "--outputSslProtocol",
        description = "SSL protocol to use when the MarkLogic app server requires an SSL connection. If a key store " +
            "or trust store is configured, defaults to 'TLSv1.2'."
    )
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    @Parameter(
        names = "--outputSslHostnameVerifier",
        description = "Hostname verification strategy when connecting via SSL."
    )
    public void setSslHostnameVerifier(SslHostnameVerifier sslHostnameVerifier) {
        this.sslHostnameVerifier = sslHostnameVerifier;
    }

    @Parameter(
        names = "--outputKeyStorePath",
        description = "File path for a key store for two-way SSL connections."
    )
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    @Parameter(
        names = "--outputKeyStorePassword",
        description = "Password for the key store identified by '--keyStorePath'."
    )
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @Parameter(
        names = "--outputKeyStoreType",
        description = "Type of the key store identified by '--keyStorePath'; defaults to 'JKS'."
    )
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    @Parameter(
        names = "--outputKeyStoreAlgorithm",
        description = "Algorithm of the key store identified by '--keyStorePath'; defaults to 'SunX509'."
    )
    public void setKeyStoreAlgorithm(String keyStoreAlgorithm) {
        this.keyStoreAlgorithm = keyStoreAlgorithm;
    }

    @Parameter(
        names = "--outputTrustStorePath",
        description = "File path for a trust store for establishing trust with the certificate used by the MarkLogic app server."
    )
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    @Parameter(
        names = "--outputTrustStorePassword",
        description = "Password for the trust store identified by '--trustStorePath'."
    )
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @Parameter(
        names = "--outputTrustStoreType",
        description = "Type of the trust store identified by '--trustStorePath'; defaults to 'JKS'."
    )
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    @Parameter(
        names = "--outputTrustStoreAlgorithm",
        description = "Algorithm of the trust store identified by '--trustStorePath'; defaults to 'SunX509'."
    )
    public void setTrustStoreAlgorithm(String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
    }
}

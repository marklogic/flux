package com.marklogic.newtool.impl;

import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.marklogic.client.DatabaseClient;
import com.marklogic.newtool.api.AuthenticationType;
import com.marklogic.newtool.api.ConnectionOptions;
import com.marklogic.newtool.api.SslHostnameVerifier;

import java.util.Map;

@Parameters(parametersValidators = ConnectionParams.class)
public class ConnectionParams extends ConnectionInputs implements IParametersValidator, ConnectionOptions {

    @Override
    public void validate(Map<String, Object> parameters) throws ParameterException {
        if (parameters.get("--connectionString") == null && parameters.get("--preview") == null) {
            if (parameters.get("--host") == null) {
                throw new ParameterException("Must specify a MarkLogic host via --host or --connectionString.");
            }
            if (parameters.get("--port") == null) {
                throw new ParameterException("Must specify a MarkLogic app server port via --port or --connectionString.");
            }

            String authType = (String) parameters.get("--authType");
            boolean isDigestOrBasicAuth = authType == null || ("digest".equalsIgnoreCase(authType) || "basic".equalsIgnoreCase(authType));
            if (isDigestOrBasicAuth && parameters.get("--username") == null) {
                throw new ParameterException("Must specify a MarkLogic user via --username when using 'BASIC' or 'DIGEST' authentication.");
            }
        }
    }

    @Override
    @Parameter(
        names = {"--connectionString"},
        description = "Defines a connection string as user:password@host:port; only usable when using 'DIGEST' or 'BASIC' authentication."
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
        names = "--basePath",
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
        names = "--connectionType",
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
        names = "--disableGzippedResponses",
        description = "If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small."
    )
    public ConnectionOptions disableGzippedResponses(Boolean disableGzippedResponses) {
        this.disableGzippedResponses = disableGzippedResponses;
        return this;
    }

    @Override
    @Parameter(
        names = "--authType",
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
        names = "--certificateFile",
        description = "File path for a key store to be used for 'CERTIFICATE' authentication."
    )
    public ConnectionOptions certificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
        return this;
    }

    @Override
    @Parameter(
        names = "--certificatePassword",
        description = "Password for the key store referenced by '--certificateFile'."
    )
    public ConnectionOptions certificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
        return this;
    }

    @Override
    @Parameter(
        names = "--cloudApiKey",
        description = "API key for authenticating with a MarkLogic Cloud cluster."
    )
    public ConnectionOptions cloudApiKey(String cloudApiKey) {
        this.cloudApiKey = cloudApiKey;
        return this;
    }

    @Override
    @Parameter(
        names = "--kerberosPrincipal",
        description = "Principal to be used with 'KERBEROS' authentication."
    )
    public ConnectionOptions kerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
        return this;
    }

    @Override
    @Parameter(
        names = "--samlToken",
        description = "Token to be used with 'SAML' authentication."
    )
    public ConnectionOptions samlToken(String samlToken) {
        this.samlToken = samlToken;
        return this;
    }

    @Override
    @Parameter(
        names = "--sslProtocol",
        description = "SSL protocol to use when the MarkLogic app server requires an SSL connection. If a key store " +
            "or trust store is configured, defaults to 'TLSv1.2'."
    )
    public ConnectionOptions sslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
        return this;
    }

    @Override
    @Parameter(
        names = "--sslHostnameVerifier",
        description = "Hostname verification strategy when connecting via SSL."
    )
    public ConnectionOptions sslHostnameVerifier(SslHostnameVerifier sslHostnameVerifier) {
        this.sslHostnameVerifier = sslHostnameVerifier;
        return this;
    }

    @Override
    @Parameter(
        names = "--keyStorePath",
        description = "File path for a key store for two-way SSL connections."
    )
    public ConnectionOptions keyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
        return this;
    }

    @Override
    @Parameter(
        names = "--keyStorePassword",
        description = "Password for the key store identified by '--keyStorePath'."
    )
    public ConnectionOptions keyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    @Override
    @Parameter(
        names = "--keyStoreType",
        description = "Type of the key store identified by '--keyStorePath'; defaults to 'JKS'."
    )
    public ConnectionOptions keyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
        return this;
    }

    @Override
    @Parameter(
        names = "--keyStoreAlgorithm",
        description = "Algorithm of the key store identified by '--keyStorePath'; defaults to 'SunX509'."
    )
    public ConnectionOptions keyStoreAlgorithm(String keyStoreAlgorithm) {
        this.keyStoreAlgorithm = keyStoreAlgorithm;
        return this;
    }

    @Override
    @Parameter(
        names = "--trustStorePath",
        description = "File path for a trust store for establishing trust with the certificate used by the MarkLogic app server."
    )
    public ConnectionOptions trustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
        return this;
    }

    @Override
    @Parameter(
        names = "--trustStorePassword",
        description = "Password for the trust store identified by '--trustStorePath'."
    )
    public ConnectionOptions trustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    @Override
    @Parameter(
        names = "--trustStoreType",
        description = "Type of the trust store identified by '--trustStorePath'; defaults to 'JKS'."
    )
    public ConnectionOptions trustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
        return this;
    }

    @Override
    @Parameter(
        names = "--trustStoreAlgorithm",
        description = "Algorithm of the trust store identified by '--trustStorePath'; defaults to 'SunX509'."
    )
    public ConnectionOptions trustStoreAlgorithm(String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
        return this;
    }
}

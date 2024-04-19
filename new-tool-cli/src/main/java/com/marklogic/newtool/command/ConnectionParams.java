package com.marklogic.newtool.command;

import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.marklogic.client.DatabaseClient;

import java.util.Map;

@Parameters(parametersValidators = ConnectionParams.class)
public class ConnectionParams extends ConnectionInputs implements IParametersValidator {

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

    @Parameter(
        names = {"--connectionString"},
        description = "Defines a connection string as user:password@host:port; only usable when using 'DIGEST' or 'BASIC' authentication."
    )
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Parameter(
        names = {"--host"},
        description = "The MarkLogic host to connect to."
    )
    public void setHost(String host) {
        this.host = host;
    }

    @Parameter(
        names = "--port",
        description = "Port of a MarkLogic REST API app server to connect to."
    )
    public void setPort(Integer port) {
        this.port = port;
    }

    @Parameter(
        names = "--basePath",
        description = "Path to prepend to each call to a MarkLogic REST API app server."
    )
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Parameter(
        names = "--database",
        description = "Name of a database to connect if it differs from the one associated with the app server identified by '--port'."
    )
    public void setDatabase(String database) {
        this.database = database;
    }

    @Parameter(
        names = "--connectionType",
        description = "Defines whether connections can be made directly to each host in the MarkLogic cluster."
    )
    public void setConnectionType(DatabaseClient.ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    @Parameter(
        names = "--disableGzippedResponses",
        description = "If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small."
    )
    public void setDisableGzippedResponses(Boolean disableGzippedResponses) {
        this.disableGzippedResponses = disableGzippedResponses;
    }

    @Parameter(
        names = "--authType",
        description = "Type of authentication to use."
    )
    public void setAuthType(AuthenticationType authType) {
        this.authType = authType;
    }

    @Parameter(
        names = "--username",
        description = "Username when using 'DIGEST' or 'BASIC' authentication."
    )
    public void setUsername(String username) {
        this.username = username;
    }

    @Parameter(
        names = "--password",
        description = "Password when using 'DIGEST' or 'BASIC' authentication.",
        password = true
    )
    public void setPassword(String password) {
        this.password = password;
    }

    @Parameter(
        names = "--certificateFile",
        description = "File path for a key store to be used for 'CERTIFICATE' authentication."
    )
    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    @Parameter(
        names = "--certificatePassword",
        description = "Password for the key store referenced by '--certificateFile'."
    )
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    @Parameter(
        names = "--cloudApiKey",
        description = "API key for authenticating with a MarkLogic Cloud cluster."
    )
    public void setCloudApiKey(String cloudApiKey) {
        this.cloudApiKey = cloudApiKey;
    }

    @Parameter(
        names = "--kerberosPrincipal",
        description = "Principal to be used with 'KERBEROS' authentication."
    )
    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }

    @Parameter(
        names = "--samlToken",
        description = "Token to be used with 'SAML' authentication."
    )
    public void setSamlToken(String samlToken) {
        this.samlToken = samlToken;
    }

    @Parameter(
        names = "--sslProtocol",
        description = "SSL protocol to use when the MarkLogic app server requires an SSL connection. If a key store " +
            "or trust store is configured, defaults to 'TLSv1.2'."
    )
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    @Parameter(
        names = "--sslHostnameVerifier",
        description = "Hostname verification strategy when connecting via SSL."
    )
    public void setSslHostnameVerifier(SslHostnameVerifier sslHostnameVerifier) {
        this.sslHostnameVerifier = sslHostnameVerifier;
    }

    @Parameter(
        names = "--keyStorePath",
        description = "File path for a key store for two-way SSL connections."
    )
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    @Parameter(
        names = "--keyStorePassword",
        description = "Password for the key store identified by '--keyStorePath'."
    )
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @Parameter(
        names = "--keyStoreType",
        description = "Type of the key store identified by '--keyStorePath'; defaults to 'JKS'."
    )
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    @Parameter(
        names = "--keyStoreAlgorithm",
        description = "Algorithm of the key store identified by '--keyStorePath'; defaults to 'SunX509'."
    )
    public void setKeyStoreAlgorithm(String keyStoreAlgorithm) {
        this.keyStoreAlgorithm = keyStoreAlgorithm;
    }

    @Parameter(
        names = "--trustStorePath",
        description = "File path for a trust store for establishing trust with the certificate used by the MarkLogic app server."
    )
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    @Parameter(
        names = "--trustStorePassword",
        description = "Password for the trust store identified by '--trustStorePath'."
    )
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @Parameter(
        names = "--trustStoreType",
        description = "Type of the trust store identified by '--trustStorePath'; defaults to 'JKS'."
    )
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    @Parameter(
        names = "--trustStoreAlgorithm",
        description = "Algorithm of the trust store identified by '--trustStorePath'; defaults to 'SunX509'."
    )
    public void setTrustStoreAlgorithm(String trustStoreAlgorithm) {
        this.trustStoreAlgorithm = trustStoreAlgorithm;
    }
}

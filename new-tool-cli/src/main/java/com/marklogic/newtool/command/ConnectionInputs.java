package com.marklogic.newtool.command;

import com.marklogic.client.DatabaseClient;
import com.marklogic.spark.Options;

import java.util.Map;

/**
 * Defines all inputs for creating a connection to a REST API app server. Subclasses are expected to add setters
 * with parameter annotations.
 */
public abstract class ConnectionInputs {

    public enum AuthenticationType {
        BASIC,
        DIGEST,
        CLOUD,
        KERBEROS,
        CERTIFICATE,
        SAML
    }

    // Need this as the Java Client's SSLHostnameVerifier isn't an enum.
    public enum SslHostnameVerifier {
        ANY,
        COMMON,
        STRICT
    }

    protected String clientUri;
    protected String host;
    protected Integer port;
    protected String basePath;
    protected String database;
    protected DatabaseClient.ConnectionType connectionType;
    protected Boolean disableGzippedResponses;
    protected AuthenticationType authType;
    protected String username;
    protected String password;
    protected String certificateFile;
    protected String certificatePassword;
    protected String cloudApiKey;
    protected String kerberosPrincipal;
    protected String samlToken;
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
        if (clientUri != null) {
            // TBD Ideally reuse this logic from the connector.
            String[] parts = clientUri.split("@");
            return parts[1].split(":")[0];
        }
        return host;
    }

    public Map<String, String> makeOptions() {
        return OptionsUtil.makeOptions(
            Options.CLIENT_URI, clientUri,
            Options.CLIENT_HOST, host,
            Options.CLIENT_PORT, port != null ? port.toString() : null,
            "spark.marklogic.client.disableGzippedResponses", disableGzippedResponses != null ? Boolean.toString(disableGzippedResponses) : null,
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

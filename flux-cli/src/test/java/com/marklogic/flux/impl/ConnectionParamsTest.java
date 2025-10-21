/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.impl.importdata.ImportFilesCommand;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Note for future reference - picocli supports env vars - see https://picocli.info/#_custom_variables - but when
 * using an expression like this: ${env:MARKLOGIC_CLIENT_HOST:-${sys:marklogic.client.host:}}, and neither is populated,
 * picocli will return the literal string "null", which seems like a picocli bug.
 */
class ConnectionParamsTest extends AbstractOptionsTest {

    /**
     * Verifies that system properties are used to provide default values to connection options.
     * This makes Flux much easier to configure via e.g. a Spring Boot application.
     */
    @Test
    void systemProperties() {
        Map<String, String> systemProps = new HashMap<>();
        systemProps.put("marklogic.client.connectionString", "user:password@host:8000");
        systemProps.put("marklogic.client.host", "localhost");
        systemProps.put("marklogic.client.port", "8123");
        systemProps.put("marklogic.client.basePath", "/path");
        systemProps.put("marklogic.client.database", "somedb");
        systemProps.put("marklogic.client.connectionType", "direct");
        systemProps.put("marklogic.client.disableGzippedResponses", "true");
        systemProps.put("marklogic.client.authType", "basic");
        systemProps.put("marklogic.client.username", "jane");
        systemProps.put("marklogic.client.password", "secret");
        systemProps.put("marklogic.client.certificate.file", "/cert/file");
        systemProps.put("marklogic.client.certificate.password", "certword");
        systemProps.put("marklogic.client.cloud.apiKey", "key123");
        systemProps.put("marklogic.client.kerberos.principal", "kerb123");
        systemProps.put("marklogic.client.saml.token", "saml123");
        systemProps.put("marklogic.client.oauth.token", "oauth123");
        systemProps.put("marklogic.client.sslProtocol", "TLSv1.3");
        systemProps.put("marklogic.client.sslHostnameVerifier", "STRICT");
        systemProps.put("marklogic.client.ssl.keystore.path", "key.jks");
        systemProps.put("marklogic.client.ssl.keystore.password", "keypass");
        systemProps.put("marklogic.client.ssl.keystore.type", "SOMEJKS");
        systemProps.put("marklogic.client.ssl.keystore.algorithm", "SomeX509");
        systemProps.put("marklogic.client.ssl.truststore.path", "trust.jks");
        systemProps.put("marklogic.client.ssl.truststore.password", "trustpass");
        systemProps.put("marklogic.client.ssl.truststore.type", "SOMETRUST");
        systemProps.put("marklogic.client.ssl.truststore.algorithm", "SomeX510");

        try {
            systemProps.forEach(System::setProperty);
            ImportFilesCommand command = (ImportFilesCommand) getCommand(
                "import-files",
                "--path", "/doesnt/matter/for-this-test");
            assertOptions(command.getConnectionParams().makeOptions(),
                Options.CLIENT_URI, "user:password@host:8000",
                Options.CLIENT_HOST, "localhost",
                Options.CLIENT_PORT, "8123",
                "spark.marklogic.client.basePath", "/path",
                Options.CLIENT_DATABASE, "somedb",
                Options.CLIENT_CONNECTION_TYPE, "DIRECT",
                "spark.marklogic.client.disableGzippedResponses", "true",
                Options.CLIENT_AUTH_TYPE, "basic",
                Options.CLIENT_USERNAME, "jane",
                Options.CLIENT_PASSWORD, "secret",
                "spark.marklogic.client.certificate.file", "/cert/file",
                "spark.marklogic.client.certificate.password", "certword",
                "spark.marklogic.client.cloud.apiKey", "key123",
                "spark.marklogic.client.kerberos.principal", "kerb123",
                "spark.marklogic.client.saml.token", "saml123",
                "spark.marklogic.client.oauth.token", "oauth123",
                "spark.marklogic.client.sslProtocol", "TLSv1.3",
                "spark.marklogic.client.sslHostnameVerifier", "STRICT",
                "spark.marklogic.client.ssl.keystore.path", "key.jks",
                "spark.marklogic.client.ssl.keystore.password", "keypass",
                "spark.marklogic.client.ssl.keystore.type", "SOMEJKS",
                "spark.marklogic.client.ssl.keystore.algorithm", "SomeX509",
                "spark.marklogic.client.ssl.truststore.path", "trust.jks",
                "spark.marklogic.client.ssl.truststore.password", "trustpass",
                "spark.marklogic.client.ssl.truststore.type", "SOMETRUST",
                "spark.marklogic.client.ssl.truststore.algorithm", "SomeX510"
            );
        } finally {
            systemProps.keySet().forEach(System::clearProperty);
        }
    }

    @Test
    void allConnectionParams() {
        // Any command will work for this test, as all are assumed to extend AbstractCommand
        // and thus have an instance of ConnectionParams.
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import-files",
            "--path", "/doesnt/matter/for-this-test",
            "--connection-string", "user:password@host:8000",
            "--host", "localhost",
            "--port", "8123",
            "--disable-gzipped-responses",
            "--base-path", "/path",
            "--database", "somedb",
            "--connection-type", "direct",
            "--auth-type", "basic",
            "--username", "jane",
            "--password", "secret",
            "--certificate-file", "my.jks",
            "--certificate-password", "pwd123",
            "--cloud-api-key", "key123",
            "--kerberos-principal", "prince123",
            "--saml-token", "my-token",
            "--oauth-token", "my-oauth-token",
            "--ssl-protocol", "TLSv1.3",
            "--ssl-hostname-verifier", "STRICT",
            "--keystore-path", "key.jks",
            "--keystore-password", "keypass",
            "--keystore-type", "JKS",
            "--keystore-algorithm", "SunX509",
            "--truststore-path", "trust.jks",
            "--truststore-password", "trustpass",
            "--truststore-type", "PKCS",
            "--truststore-algorithm", "SunX510"
        );

        assertOptions(command.getConnectionParams().makeOptions(),
            Options.CLIENT_URI, "user:password@host:8000",
            Options.CLIENT_HOST, "localhost",
            Options.CLIENT_PORT, "8123",
            "spark.marklogic.client.disableGzippedResponses", "true",
            "spark.marklogic.client.basePath", "/path",
            Options.CLIENT_DATABASE, "somedb",
            Options.CLIENT_CONNECTION_TYPE, "DIRECT",
            Options.CLIENT_AUTH_TYPE, "basic",
            Options.CLIENT_USERNAME, "jane",
            Options.CLIENT_PASSWORD, "secret",
            "spark.marklogic.client.certificate.file", "my.jks",
            "spark.marklogic.client.certificate.password", "pwd123",
            "spark.marklogic.client.cloud.apiKey", "key123",
            "spark.marklogic.client.kerberos.principal", "prince123",
            "spark.marklogic.client.saml.token", "my-token",
            "spark.marklogic.client.oauth.token", "my-oauth-token",
            "spark.marklogic.client.sslProtocol", "TLSv1.3",
            "spark.marklogic.client.sslHostnameVerifier", "STRICT",
            "spark.marklogic.client.ssl.keystore.path", "key.jks",
            "spark.marklogic.client.ssl.keystore.password", "keypass",
            "spark.marklogic.client.ssl.keystore.type", "JKS",
            "spark.marklogic.client.ssl.keystore.algorithm", "SunX509",
            "spark.marklogic.client.ssl.truststore.path", "trust.jks",
            "spark.marklogic.client.ssl.truststore.password", "trustpass",
            "spark.marklogic.client.ssl.truststore.type", "PKCS",
            "spark.marklogic.client.ssl.truststore.algorithm", "SunX510"
        );
    }
}

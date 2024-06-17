/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl;

import com.marklogic.flux.impl.importdata.ImportFilesCommand;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ConnectionParamsTest extends AbstractOptionsTest {

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

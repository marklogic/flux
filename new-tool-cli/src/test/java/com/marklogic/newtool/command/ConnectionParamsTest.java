package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

class ConnectionParamsTest extends AbstractOptionsTest {

    @Test
    void allConnectionParams() {
        // Any command will work for this test, as all are assumed to extend AbstractCommand
        // and thus have an instance of ConnectionParams.
        ImportFilesCommand command = (ImportFilesCommand) getCommand(
            "import_files",
            "--path", "/doesnt/matter/for-this-test",
            "--clientUri", "user:password@host:8000",
            "--host", "localhost",
            "--port", "8123",
            "--disableGzippedResponses",
            "--basePath", "/path",
            "--database", "somedb",
            "--connectionType", "direct",
            "--authType", "basic",
            "--username", "jane",
            "--password", "secret",
            "--certificateFile", "my.jks",
            "--certificatePassword", "pwd123",
            "--cloudApiKey", "key123",
            "--kerberosPrincipal", "prince123",
            "--samlToken", "my-token",
            "--sslProtocol", "TLSv1.3",
            "--sslHostnameVerifier", "STRICT",
            "--keyStorePath", "key.jks",
            "--keyStorePassword", "keypass",
            "--keyStoreType", "JKS",
            "--keyStoreAlgorithm", "SunX509",
            "--trustStorePath", "trust.jks",
            "--trustStorePassword", "trustpass",
            "--trustStoreType", "PKCS",
            "--trustStoreAlgorithm", "SunX510"
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

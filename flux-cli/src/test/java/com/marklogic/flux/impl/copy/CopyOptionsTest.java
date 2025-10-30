/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.copy;

import com.marklogic.flux.cli.Main;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.flux.impl.importdata.WriteDocumentParams;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CopyOptionsTest extends AbstractOptionsTest {

    @Test
    void useOutputParamsForConnection() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--connection-string", "test:test@test:8000",
            "--output-connection-string", "user:password@host:8000",
            "--collections", "anything"
        );

        assertOptions(
            command.makeOutputConnectionOptions(),
            Options.CLIENT_URI, "user:password@host:8000"
        );
    }

    @Test
    void noSnapshot() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--connection-string", "test:test@test:8000",
            "--output-connection-string", "user:password@host:8000",
            "--collections", "anything",
            "--no-snapshot"
        );

        assertOptions(
            command.readParams.makeOptions(),
            Options.READ_SNAPSHOT, "false"
        );
    }

    @Test
    void useRegularConnectionParamsIfNoOutputConnectionParams() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--connection-string", "test:test@test:8000",
            "--collections", "anything"
        );

        assertOptions(
            command.makeOutputConnectionOptions(),
            Options.CLIENT_URI, "test:test@test:8000"
        );
    }

    @Test
    void allWriteParams() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--connection-string", "someone:word@somehost:7000",
            "--collections", "anything",
            "--output-abort-on-write-failure",
            "--output-batch-size", "123",
            "--output-collections", "c1,c2",
            "--output-failed-documents-path", "/my/failures",
            "--output-permissions", "rest-reader,read,qconsole-user,update",
            "--output-pipeline-batch-size", "13",
            "--output-temporal-collection", "t1",
            "--output-thread-count", "7",
            "--output-transform", "transform1",
            "--output-transform-params", "p1;v1;p2;v2",
            "--output-transform-params-delimiter", ";",
            "--output-uri-prefix", "/prefix/",
            "--output-uri-replace", ".*data,''",
            "--output-uri-suffix", ".xml",
            "--output-uri-template", "/{example}.xml",
            "--splitter-text",
            "--embedder", "doesnt-matter",
            "--output-doc-metadata", "meta1=value1",
            "--output-doc-metadata", "meta2=value2",
            "--output-doc-prop", "prop1=value1",
            "--output-doc-prop", "prop2=value2"
        );

        assertOptions(command.writeParams.makeOptions(),
            Options.WRITE_ABORT_ON_FAILURE, "true",
            Options.WRITE_BATCH_SIZE, "123",
            Options.WRITE_PIPELINE_BATCH_SIZE, "13",
            Options.WRITE_COLLECTIONS, "c1,c2",
            Options.WRITE_ARCHIVE_PATH_FOR_FAILED_DOCUMENTS, "/my/failures",
            Options.WRITE_PERMISSIONS, "rest-reader,read,qconsole-user,update",
            Options.WRITE_TEMPORAL_COLLECTION, "t1",
            Options.WRITE_THREAD_COUNT, "7",
            Options.WRITE_TRANSFORM_NAME, "transform1",
            Options.WRITE_TRANSFORM_PARAMS, "p1;v1;p2;v2",
            Options.WRITE_TRANSFORM_PARAMS_DELIMITER, ";",
            Options.WRITE_URI_PREFIX, "/prefix/",
            Options.WRITE_URI_REPLACE, ".*data,''",
            Options.WRITE_URI_SUFFIX, ".xml",
            Options.WRITE_URI_TEMPLATE, "/{example}.xml",
            Options.WRITE_SPLITTER_TEXT, "true",
            Options.WRITE_EMBEDDER_MODEL_FUNCTION_CLASS_NAME, "doesnt-matter",
            Options.WRITE_METADATA_VALUES_PREFIX + "meta1", "value1",
            Options.WRITE_METADATA_VALUES_PREFIX + "meta2", "value2",
            Options.WRITE_DOCUMENT_PROPERTIES_PREFIX + "prop1", "value1",
            Options.WRITE_DOCUMENT_PROPERTIES_PREFIX + "prop2", "value2"
        );
    }

    @Test
    void allOutputConnectionParams() {
        CopyCommand command = (CopyCommand) getCommand(
            "copy",
            "--connection-string", "someone:word@somehost:7000",
            "--collections", "anything",
            "--output-connection-string", "user:password@host:8000",
            "--output-host", "localhost",
            "--output-port", "8123",
            "--output-base-path", "/path",
            "--output-database", "somedb",
            "--output-connection-type", "direct",
            "--output-auth-type", "basic",
            "--output-username", "jane",
            "--output-password", "secret",
            "--output-certificate-file", "my.jks",
            "--output-certificate-password", "pwd123",
            "--output-cloud-api-key", "key123",
            "--output-kerberos-principal", "prince123",
            "--output-oauth-token", "my-oauth-token",
            "--output-saml-token", "my-token",
            "--output-ssl-protocol", "TLSv1.3",
            "--output-ssl-hostname-verifier", "STRICT",
            "--output-keystore-path", "key.jks",
            "--output-keystore-password", "keypass",
            "--output-keystore-type", "JKS",
            "--output-keystore-algorithm", "SunX509",
            "--output-truststore-path", "trust.jks",
            "--output-truststore-password", "trustpass",
            "--output-truststore-type", "PKCS",
            "--output-truststore-algorithm", "SunX510"
        );

        assertOptions(command.makeOutputConnectionOptions(),
            Options.CLIENT_URI, "user:password@host:8000",
            Options.CLIENT_HOST, "localhost",
            Options.CLIENT_PORT, "8123",
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
            "spark.marklogic.client.oauth.token", "my-oauth-token",
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

    @Test
    void testOutputParameterCount() {
        final int copyCommandCount = getOutputParameterCountInCopyCommand();
        final int writeDocumentParamsCount = getParameterCount(WriteDocumentParams.class);
        assertEquals(copyCommandCount, writeDocumentParamsCount,
            "Expecting the CopyCommand to declare one Parameter field for each Parameter field found in " +
                "WriteDocumentParams, as CopyCommand is expected to duplicate all of those " +
                "parameters and start their names with '--output'. This test is intended to ensure that if we ever " +
                "add a field to WriteDocumentParams, we need to remember to duplicate it in CopyCommand.");
    }

    private int getParameterCount(Class<?> clazz) {
        int count = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(CommandLine.Option.class) != null) {
                count++;
            }
        }
        return count;
    }

    private int getOutputParameterCountInCopyCommand() {
        int count = 0;
        for (Field field : CopyCommand.CopyWriteDocumentsParams.class.getDeclaredFields()) {
            CommandLine.Option option = field.getAnnotation(CommandLine.Option.class);
            if (option != null) {
                String name = option.names()[0];
                if (name.startsWith("--output")) {
                    count++;
                }
            }
        }
        return count;
    }

    @Test
    void getOptionNames() {
        List<String> expectedOptions = List.of(
            "--output-connection-string", "--output-host", "--output-port", "--output-base-path",
            "--output-database", "--output-connection-type", "--output-auth-type", "--output-username",
            "--output-password", "--output-certificate-file", "--output-certificate-password",
            "--output-cloud-api-key", "--output-kerberos-principal", "--output-saml-token",
            "--output-oauth-token", "--output-ssl-protocol", "--output-ssl-hostname-verifier",
            "--output-keystore-path", "--output-keystore-password", "--output-keystore-type",
            "--output-keystore-algorithm", "--output-truststore-path", "--output-truststore-password",
            "--output-truststore-type", "--output-truststore-algorithm"
        );

        List<String> actualOptions = Main.getOutputConnectionOptionNames();

        assertEquals(expectedOptions.size(), actualOptions.size(),
            "Expected " + expectedOptions.size() + " output connection option names");

        assertTrue(actualOptions.containsAll(expectedOptions),
            "Missing options: " + expectedOptions.stream()
                .filter(opt -> !actualOptions.contains(opt))
                .toList());
    }

    @Test
    void systemProperties() {
        Map<String, String> systemProps = new HashMap<>();

        // For a flux-box prototype, only supporting this for now. May add all the output options later.
        systemProps.put("marklogic.client.output.connectionString", "user:password@output:8000");

        try {
            systemProps.forEach(System::setProperty);

            CopyCommand command = (CopyCommand) getCommand("copy",
                "--connection-string", "test:test@host:8000"
            );

            assertOptions(
                command.makeOutputConnectionOptions(),
                Options.CLIENT_URI, "user:password@output:8000"
            );
        } finally {
            systemProps.keySet().forEach(System::clearProperty);
        }
    }
}

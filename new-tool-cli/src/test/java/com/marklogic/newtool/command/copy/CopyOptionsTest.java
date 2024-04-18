package com.marklogic.newtool.command.copy;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.newtool.command.importdata.WriteDocumentParams;
import com.marklogic.newtool.command.importdata.WriteDocumentWithTemplateParams;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyOptionsTest extends AbstractOptionsTest {

    @Test
    void useOutputParamsForConnection() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--clientUri", "test:test@test:8000",
            "--outputClientUri", "user:password@host:8000",
            "--collections", "anything"
        );

        assertOptions(
            command.makeOutputConnectionOptions(),
            Options.CLIENT_URI, "user:password@host:8000"
        );
    }

    @Test
    void useRegularConnectionParamsIfNoOutputConnectionParams() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--clientUri", "test:test@test:8000",
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
            "--clientUri", "someone:word@somehost:7000",
            "--collections", "anything",
            "--outputAbortOnWriteFailure",
            "--outputBatchSize", "123",
            "--outputCollections", "c1,c2",
            "--outputFailedDocumentsPath", "/my/failures",
            "--outputPermissions", "rest-reader,read,qconsole-user,update",
            "--outputTemporalCollection", "t1",
            "--outputThreadCount", "7",
            "--outputTransform", "transform1",
            "--outputTransformParams", "p1;v1;p2;v2",
            "--outputTransformParamsDelimiter", ";",
            "--outputUriPrefix", "/prefix/",
            "--outputUriReplace", ".*data,''",
            "--outputUriSuffix", ".xml",
            "--outputUriTemplate", "/{example}.xml"
        );

        assertOptions(command.makeWriteOptions(),
            Options.WRITE_ABORT_ON_FAILURE, "true",
            Options.WRITE_BATCH_SIZE, "123",
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
            Options.WRITE_URI_TEMPLATE, "/{example}.xml"
        );
    }

    @Test
    void allOutputConnectionParams() {
        CopyCommand command = (CopyCommand) getCommand(
            "copy",
            "--clientUri", "someone:word@somehost:7000",
            "--collections", "anything",
            "--outputClientUri", "user:password@host:8000",
            "--outputHost", "localhost",
            "--outputPort", "8123",
            "--outputBasePath", "/path",
            "--outputDatabase", "somedb",
            "--outputConnectionType", "direct",
            "--outputAuthType", "basic",
            "--outputUsername", "jane",
            "--outputPassword", "secret",
            "--outputCertificateFile", "my.jks",
            "--outputCertificatePassword", "pwd123",
            "--outputCloudApiKey", "key123",
            "--outputKerberosPrincipal", "prince123",
            "--outputSamlToken", "my-token",
            "--outputSslProtocol", "TLSv1.3",
            "--outputSslHostnameVerifier", "STRICT",
            "--outputKeyStorePath", "key.jks",
            "--outputKeyStorePassword", "keypass",
            "--outputKeyStoreType", "JKS",
            "--outputKeyStoreAlgorithm", "SunX509",
            "--outputTrustStorePath", "trust.jks",
            "--outputTrustStorePassword", "trustpass",
            "--outputTrustStoreType", "PKCS",
            "--outputTrustStoreAlgorithm", "SunX510"
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

        // Can't find a way to get the count from the subclass, so gotta get counts from both subclass and parent class.
        final int writeDocumentParamsCount =
            getParameterCount(WriteDocumentWithTemplateParams.class) + getParameterCount(WriteDocumentParams.class);

        assertEquals(copyCommandCount, writeDocumentParamsCount,
            "Expecting the CopyCommand to declare one Parameter field for each Parameter field found in " +
                "WriteDocumentParams, as CopyCommand is expected to duplicate all of those " +
                "parameters and start their names with '--output'. This test is intended to ensure that if we ever " +
                "add a field to WriteDocumentParams, we need to remember to duplicate it in CopyCommand.");
    }

    private int getParameterCount(Class<?> clazz) {
        int count = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(Parameter.class) != null) {
                count++;
            }
        }
        return count;
    }

    private int getOutputParameterCountInCopyCommand() {
        int count = 0;
        for (Field field : CopyCommand.class.getDeclaredFields()) {
            Parameter param = field.getAnnotation(Parameter.class);
            if (param != null && param.names()[0].startsWith("--output")) {
                count++;
            }
        }
        return count;
    }
}

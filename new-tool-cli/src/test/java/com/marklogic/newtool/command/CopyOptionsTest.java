package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CopyOptionsTest extends AbstractOptionsTest {

    @Test
    void useOutputParamsForConnection() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--clientUri", "test:test@test:8000",
            "--outputClientUri", "user:password@host:8000"
        );

        assertOptions(
            command.makeWriteConnectionOptions(),
            Options.CLIENT_URI, "user:password@host:8000"
        );
    }

    @Test
    void useRegularConnectionParamsIfNoOutputConnectionParams() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--clientUri", "test:test@test:8000"
        );

        assertOptions(
            command.makeWriteConnectionOptions(),
            Options.CLIENT_URI, "test:test@test:8000"
        );
    }

    @Test
    void allWriteParams() {
        CopyCommand command = (CopyCommand) getCommand("copy",
            "--outputAbortOnFailure", "false",
            "--outputBatchSize", "123",
            "--outputCollections", "c1,c2",
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
            Options.WRITE_ABORT_ON_FAILURE, "false",
            Options.WRITE_BATCH_SIZE, "123",
            Options.WRITE_COLLECTIONS, "c1,c2",
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
    void testOutputParameterCount() {
        int copyCommandCount = getOutputParameterCountInCopyCommand();
        int writeDocumentParamsCount = getParameterCount(WriteDocumentParams.class);
        int connectionParamsCount = getParameterCount(ConnectionParams.class);

        assertEquals(copyCommandCount, writeDocumentParamsCount + connectionParamsCount,
            "Expecting the CopyCommand to declare one Parameter field for each Parameter field found in " +
                "WriteDocumentParams and ConnectionParams, as CopyCommand is expected to duplicate all of those " +
                "parameters and start their names with '--output'. This test is intended to ensure that if we ever " +
                "add a field to WriteDocumentParams or ConnectionParams, we need to remember to duplicate it in " +
                "CopyCommand.");
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

/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.api.Flux;
import com.marklogic.flux.api.GenericFilesExporter;
import com.marklogic.flux.impl.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExportFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void prettyPrint() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--pretty-print",
            "--compression", "gzip"
        );

        Map<String, String> options = command.writeParams.get();
        assertEquals("true", options.get(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("GZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }

    @Test
    void dontPrettyPrint() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--compression", "zip"
        );

        Map<String, String> options = command.writeParams.get();
        assertFalse(options.containsKey(Options.WRITE_FILES_PRETTY_PRINT));
        assertEquals("ZIP", options.get(Options.WRITE_FILES_COMPRESSION));
    }

    @Test
    void encoding() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--encoding", "ISO-8859-1"
        );

        Map<String, String> options = command.writeParams.get();
        assertEquals("ISO-8859-1", options.get(Options.WRITE_FILES_ENCODING));
    }

    @Test
    void streaming() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--streaming",
            "--no-snapshot"
        );

        Map<String, String> readOptions = command.buildReadOptions();
        assertEquals("true", readOptions.get(Options.STREAM_FILES));
        assertEquals("false", readOptions.get(Options.READ_SNAPSHOT));
        assertEquals("anything", readOptions.get(Options.READ_DOCUMENTS_COLLECTIONS));

        Map<String, String> writeOptions = command.buildWriteOptions();
        assertEquals("true", writeOptions.get(Options.STREAM_FILES));
        assertEquals("test:test@host:8000", writeOptions.get(Options.CLIENT_URI),
            "The connection options must be present in the write options so that the writer can connect " +
                "to MarkLogic and read documents.");
    }

    @Test
    void secondaryUris() {
        ExportFilesCommand command = (ExportFilesCommand) getCommand(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--secondary-uris-invoke", "myInvokeFunction.sjs",
            "--secondary-uris-javascript", "myJavascriptFunction()",
            "--secondary-uris-xquery", "myXQueryFunction()",
            "--secondary-uris-javascript-file", "myJavascriptFile.js",
            "--secondary-uris-xquery-file", "myXQueryFile.xqy",
            "--secondary-uris-var", "var1=value1",
            "--secondary-uris-var", "var2=value2"
        );

        Map<String, String> options = command.readParams.makeOptions();
        assertEquals("myInvokeFunction.sjs", options.get(Options.READ_SECONDARY_URIS_INVOKE));
        assertEquals("myJavascriptFunction()", options.get(Options.READ_SECONDARY_URIS_JAVASCRIPT));
        assertEquals("myXQueryFunction()", options.get(Options.READ_SECONDARY_URIS_XQUERY));
        assertEquals("myJavascriptFile.js", options.get(Options.READ_SECONDARY_URIS_JAVASCRIPT_FILE));
        assertEquals("myXQueryFile.xqy", options.get(Options.READ_SECONDARY_URIS_XQUERY_FILE));
        assertEquals("value1", options.get(Options.READ_SECONDARY_URIS_VARS_PREFIX + "var1"));
        assertEquals("value2", options.get(Options.READ_SECONDARY_URIS_VARS_PREFIX + "var2"));
    }

    @Test
    void invalidSecondaryVar() {
        String stderr = applyOptionsAndReturnStderr(
            "export-files",
            "--connection-string", "test:test@host:8000",
            "--collections", "anything",
            "--path", "anywhere",
            "--secondary-uris-var", "var1=value1",
            "--secondary-uris-var", "var2"
        );

        assertTrue(stderr.contains("Unmatched argument at index 10: 'var2'"),
            "The default picocli message for an unmatched map argument is hopefully sufficient to indicate " +
                "that the value for var2 is missing. Both our docs and the inline help will indicate what the " +
                "required syntax is. Actual stderr: " + stderr);
    }

    @Test
    void setOptionsViaExporterApi() {
        GenericFilesExporter exporter = Flux.exportGenericFiles()
            .connectionString("admin:admin@localhost:8002")
            .from(options ->
                options.collections("author")
                    .secondaryUrisInvoke("myInvokeFunction.sjs")
                    .secondaryUrisJavaScript("myJavascriptFunction()")
                    .secondaryUrisXQuery("myXQueryFunction()")
                    .secondaryUrisJavaScriptFile("myJavascriptFile.js")
                    .secondaryUrisXQueryFile("myXQueryFile.xqy")
                    .secondaryUrisVars(Map.of("var1", "value1", "var2", "value2"))
            )
            .to(options -> options.path("/some/path"));

        ExportFilesCommand command = (ExportFilesCommand) exporter;

        Map<String, String> options = command.readParams.makeOptions();
        assertEquals("myInvokeFunction.sjs", options.get(Options.READ_SECONDARY_URIS_INVOKE));
        assertEquals("myJavascriptFunction()", options.get(Options.READ_SECONDARY_URIS_JAVASCRIPT));
        assertEquals("myXQueryFunction()", options.get(Options.READ_SECONDARY_URIS_XQUERY));
        assertEquals("myJavascriptFile.js", options.get(Options.READ_SECONDARY_URIS_JAVASCRIPT_FILE));
        assertEquals("myXQueryFile.xqy", options.get(Options.READ_SECONDARY_URIS_XQUERY_FILE));
        assertEquals("value1", options.get(Options.READ_SECONDARY_URIS_VARS_PREFIX + "var1"));
        assertEquals("value2", options.get(Options.READ_SECONDARY_URIS_VARS_PREFIX + "var2"));
    }
}

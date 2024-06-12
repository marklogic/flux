package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.spark.ConnectorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The "import generic files" command is used in this test, but any command could be used.
 * <p>
 * For the "missing value" tests, turns out that since our connector throws a good exception, we don't need any
 * additional support in the API. It's really just the connection string that we need to validate right away so that
 * we can provide an error message that doesn't include "--connection-string" in it.
 */
class ConnectionTest extends AbstractTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "not@localhost",
        "user:password@host",
        "user:password@host:port:somethingelse"
    })
    void badConnectionString(String connectionString) {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connectionString(connectionString);

        FluxException ex = assertThrowsNtException(() -> importer.execute());
        assertEquals("Invalid value for connection string; must be username:password@host:port/optionalDatabaseName",
            ex.getMessage());
    }

    @Test
    void nullHost() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connection(options -> options.port(8000).username("doesnt").password("matter"))
            .readFiles("src/test/resources");

        ConnectorException ex = assertThrows(ConnectorException.class, () -> importer.execute());
        assertEquals("Unable to connect to MarkLogic; cause: No host provided", ex.getMessage(),
            "For an API user, it seems reasonable to receive a ConnectorException for certain errors, as it's " +
                "still a MarkLogic-specific error and not a Spark-specific one.");
    }

    @Test
    void zeroPort() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connection(options -> options.host("localhost").username("doesnt").password("matter"))
            .readFiles("src/test/resources");

        ConnectorException ex = assertThrows(ConnectorException.class, () -> importer.execute());
        assertEquals("Unable to connect to MarkLogic; cause: unexpected port: 0", ex.getMessage());
    }

    @Test
    void nullUsername() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connection(options -> options.host("localhost").port(8000).password("something"))
            .readFiles("src/test/resources");

        ConnectorException ex = assertThrows(ConnectorException.class, () -> importer.execute());
        assertEquals("Unable to connect to MarkLogic; cause: username must be of type String", ex.getMessage());
    }

    @Test
    void nullPassword() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connection(options -> options.host("localhost").port(8000).username("something"))
            .readFiles("src/test/resources");

        ConnectorException ex = assertThrows(ConnectorException.class, () -> importer.execute());
        assertEquals("Unable to connect to MarkLogic; cause: password must be of type String", ex.getMessage());
    }

    @Test
    void badOutputConnectionString() {
        DocumentCopier copier = Flux.copyDocuments()
            .connectionString(makeConnectionString())
            .outputConnectionString("not@valid:port")
            .readDocuments(options -> options.collections("anything"));

        FluxException ex = assertThrowsNtException(() -> copier.execute());
        assertEquals(
            "Invalid value for output connection string; must be username:password@host:port/optionalDatabaseName",
            ex.getMessage()
        );
    }
}

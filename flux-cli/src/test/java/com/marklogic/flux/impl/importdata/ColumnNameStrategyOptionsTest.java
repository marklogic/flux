/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ColumnNameStrategy;
import com.marklogic.flux.impl.AbstractOptionsTest;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the {@code --column-name-strategy} option is exposed consistently on each of the structured data
 * import commands and is parsed correctly, including rejection of invalid values.
 */
class ColumnNameStrategyOptionsTest extends AbstractOptionsTest {

    @Test
    void delimitedFilesDefaultsToNone() {
        ImportDelimitedFilesCommand command = (ImportDelimitedFilesCommand) getCommand(
            "import-delimited-files",
            "--connection-string", makeConnectionString(),
            "--path", "anywhere"
        );

        assertEquals(ColumnNameStrategy.NONE, command.getReadParams().getStructuredDataParams().getColumnNameStrategy());
    }

    @Test
    void delimitedFilesSimple() {
        ImportDelimitedFilesCommand command = (ImportDelimitedFilesCommand) getCommand(
            "import-delimited-files",
            "--connection-string", makeConnectionString(),
            "--path", "anywhere",
            "--column-name-strategy", "simple"
        );

        assertEquals(ColumnNameStrategy.SIMPLE, command.getReadParams().getStructuredDataParams().getColumnNameStrategy());
    }

    @Test
    void avroFilesSimple() {
        ImportAvroFilesCommand command = (ImportAvroFilesCommand) getCommand(
            "import-avro-files",
            "--connection-string", makeConnectionString(),
            "--path", "anywhere",
            "--column-name-strategy", "simple"
        );

        ImportAvroFilesCommand.ReadAvroFilesParams readParams = (ImportAvroFilesCommand.ReadAvroFilesParams) command.getReadParams();
        assertEquals(ColumnNameStrategy.SIMPLE, readParams.getStructuredDataParams().getColumnNameStrategy());
    }

    @Test
    void orcFilesSimple() {
        ImportOrcFilesCommand command = (ImportOrcFilesCommand) getCommand(
            "import-orc-files",
            "--connection-string", makeConnectionString(),
            "--path", "anywhere",
            "--column-name-strategy", "simple"
        );

        ImportOrcFilesCommand.ReadOrcFilesParams readParams = (ImportOrcFilesCommand.ReadOrcFilesParams) command.getReadParams();
        assertEquals(ColumnNameStrategy.SIMPLE, readParams.getStructuredDataParams().getColumnNameStrategy());
    }

    @Test
    void parquetFilesSimple() {
        ImportParquetFilesCommand command = (ImportParquetFilesCommand) getCommand(
            "import-parquet-files",
            "--connection-string", makeConnectionString(),
            "--path", "anywhere",
            "--column-name-strategy", "simple"
        );

        ImportParquetFilesCommand.ReadParquetFilesParams readParams = (ImportParquetFilesCommand.ReadParquetFilesParams) command.getReadParams();
        assertEquals(ColumnNameStrategy.SIMPLE, readParams.getStructuredDataParams().getColumnNameStrategy());
    }

    @Test
    void jdbcSimple() {
        ImportJdbcCommand command = (ImportJdbcCommand) getCommand(
            "import-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", "select * from mytable",
            "--jdbc-url", "jdbc:h2:mem:test",
            "--column-name-strategy", "simple"
        );

        assertEquals(ColumnNameStrategy.SIMPLE, command.getReadParams().getStructuredDataParams().getColumnNameStrategy());
    }

    @Test
    void invalidValueIsRejected() {
        CommandLine.ParameterException ex = assertThrows(CommandLine.ParameterException.class, () -> getCommand(
            "import-delimited-files",
            "--connection-string", makeConnectionString(),
            "--path", "anywhere",
            "--column-name-strategy", "bogus"
        ));

        assertTrue(ex.getMessage().contains("--column-name-strategy"),
            "The error should reference the invalid option; actual message: " + ex.getMessage());
    }
}

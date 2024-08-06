/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.AbstractExportJdbcTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcExporterTest extends AbstractExportJdbcTest {

    @Test
    void test() {
        Flux.exportJdbc()
            .connectionString(makeConnectionString())
            .from(options -> options.opticQuery(READ_AUTHORS_OPTIC_QUERY))
            .to(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .additionalOptions(Map.of("driver", PostgresUtil.DRIVER))
                .table(EXPORTED_TABLE_NAME)
                .saveMode(SaveMode.ERRORIFEXISTS)
            )
            .execute();

        verifyRowCountInTable(15);
    }

    @Test
    void queryOnly() {
        Flux.exportJdbc()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .to(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .additionalOptions(Map.of("driver", PostgresUtil.DRIVER))
                .table(EXPORTED_TABLE_NAME)
                .saveMode(SaveMode.ERRORIFEXISTS)
            )
            .execute();

        verifyRowCountInTable(15);
    }

    @Test
    void missingJdbcUrl() {
        JdbcExporter exporter = Flux.exportJdbc()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .to(options -> options
                .table(EXPORTED_TABLE_NAME)
                .saveMode(SaveMode.ERRORIFEXISTS)
            );

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
        assertEquals("Must specify a JDBC URL", ex.getMessage());
    }

    @Test
    void missingTable() {
        JdbcExporter exporter = Flux.exportJdbc()
            .connectionString(makeConnectionString())
            .from(READ_AUTHORS_OPTIC_QUERY)
            .to(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .saveMode(SaveMode.ERRORIFEXISTS)
            );

        FluxException ex = assertThrowsFluxException(() -> exporter.execute());
        assertEquals("Must specify a table", ex.getMessage());
    }
}

package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractExportJdbcTest;
import com.marklogic.newtool.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

class JdbcExporterTest extends AbstractExportJdbcTest {

    @Test
    void test() {
        NT.exportJdbc()
            .connectionString(makeConnectionString())
            .readRows(options -> options.opticQuery(READ_AUTHORS_OPTIC_QUERY))
            .writeRows(options -> options
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
        NT.exportJdbc()
            .connectionString(makeConnectionString())
            .readRows(READ_AUTHORS_OPTIC_QUERY)
            .writeRows(options -> options
                .url(PostgresUtil.URL_WITH_AUTH)
                .additionalOptions(Map.of("driver", PostgresUtil.DRIVER))
                .table(EXPORTED_TABLE_NAME)
                .saveMode(SaveMode.ERRORIFEXISTS)
            )
            .execute();

        verifyRowCountInTable(15);
    }
}

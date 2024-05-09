package com.marklogic.newtool.api;

import com.marklogic.newtool.AbstractExportJdbcTest;
import com.marklogic.newtool.command.PostgresUtil;
import org.apache.spark.sql.SaveMode;
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
                .saveMode(SaveMode.ErrorIfExists)
            )
            .execute();

        verifyRowCountInTable(15);
    }
}

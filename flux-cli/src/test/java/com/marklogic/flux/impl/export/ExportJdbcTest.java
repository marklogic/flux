/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.export;

import com.marklogic.flux.AbstractExportJdbcTest;
import com.marklogic.flux.impl.PostgresUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExportJdbcTest extends AbstractExportJdbcTest {

    @Test
    void simpleTest() {
        exportFifteenAuthors();
        verifyRowCountInTable(15, "15 authors should have been written to the table.");

        this.jdbcTemplate.query(String.format("select * from %s where \"LastName\" = 'Golby'", EXPORTED_TABLE_NAME), resultSet -> {
            assertEquals(1, resultSet.getInt("CitationID"));
            assertEquals("Golby", resultSet.getString("LastName"));
            assertEquals("Pen", resultSet.getString("ForeName"));
            assertEquals("2022-07-13", resultSet.getString("Date"));
            final String dateTime = resultSet.getString("DateTime");
            assertTrue(dateTime.startsWith("2022-07-13 0"), "Punting on ensuring the time is the same when run " +
                "on Jenkins; actual value: " + dateTime);
            assertEquals(1, resultSet.getInt("LuckyNumber"));
            assertTrue(resultSet.getBoolean("BooleanValue"));
            assertEquals("interval 2 years 4 month", resultSet.getString("CalendarInterval"));
            assertNotNull(resultSet.getString("Base64Value"));
        });
    }

    @Test
    void appendTwice() {
        exportFifteenAuthorsWithMode("apPEnd");
        verifyRowCountInTable(15, "The 15 authors should have been added to the table, and values for --mode are " +
            "expected to be case insensitive.");

        exportFifteenAuthorsWithMode("append");
        verifyRowCountInTable(30, "Since mode was set to 'append', Spark should have added the rows to the table " +
            "and not overwritten the table.");
    }

    @Test
    void overwriteTwice() {
        exportFifteenAuthorsWithMode("OVERwrite");
        verifyRowCountInTable(15);

        exportFifteenAuthorsWithMode("OVERWRITE");
        verifyRowCountInTable(15, "Since mode was set to 'overwrite', Spark should have removed all rows in the table " +
            "and added the 15 author rows.");
    }

    @Test
    void ignore() {
        exportFifteenAuthors();
        verifyRowCountInTable(15);

        jdbcTemplate.execute(String.format("delete from %s where \"CitationID\" = 1", EXPORTED_TABLE_NAME));
        verifyRowCountInTable(11, "4 authors have CitationID = 1, so with those deleted, there should now be 11 rows.");

        exportFifteenAuthorsWithMode("ignore");
        verifyRowCountInTable(11, "When the mode is 'ignore', Spark is expected to not perform the save and the " +
            "existing rows in the table should not be modified.");
    }

    @Test
    void dynamicParam() {
        run(
            "export-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", "this should be overwritten by the dynamic param",
            "--table", EXPORTED_TABLE_NAME,
            "-Pdriver=" + PostgresUtil.DRIVER
        );

        verifyRowCountInTable(15, "Spark options specified via dynamic params should take precedence over options " +
            "set via command arguments, so the value of -Pdriver should be used, causing the query to work.");
    }

    @Test
    void badJdbcDriver() {
        assertStderrContains(() -> run(
            "export-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", "not.valid.driver.name",
            "--table", EXPORTED_TABLE_NAME
        ), "Command failed, cause: Unable to load class: not.valid.driver.name; " +
            "for a JDBC driver, ensure you are specifying the fully-qualified class name for your JDBC driver.");
    }

    private void exportFifteenAuthors() {
        exportFifteenAuthorsWithMode("errorifexists");
    }

    private void exportFifteenAuthorsWithMode(String saveMode) {
        run(
            "export-jdbc",
            "--connection-string", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--jdbc-url", PostgresUtil.URL_WITH_AUTH,
            "--jdbc-driver", PostgresUtil.DRIVER,
            "--table", EXPORTED_TABLE_NAME,
            "--mode", saveMode
        );
    }
}

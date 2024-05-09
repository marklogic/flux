package com.marklogic.newtool.command.export;

import com.marklogic.newtool.AbstractExportJdbcTest;
import com.marklogic.newtool.command.PostgresUtil;
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
            "export_jdbc",
            "--connectionString", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--jdbcUrl", PostgresUtil.URL_WITH_AUTH,
            "--jdbcDriver", "this should be overwritten by the dynamic param",
            "--table", EXPORTED_TABLE_NAME,
            "-Pdriver=" + PostgresUtil.DRIVER
        );

        verifyRowCountInTable(15, "Spark options specified via dynamic params should take precedence over options " +
            "set via command arguments, so the value of -Pdriver should be used, causing the query to work.");
    }

    private void exportFifteenAuthors() {
        exportFifteenAuthorsWithMode("errorifexists");
    }

    private void exportFifteenAuthorsWithMode(String saveMode) {
        run(
            "export_jdbc",
            "--connectionString", makeConnectionString(),
            "--query", READ_AUTHORS_OPTIC_QUERY,
            "--jdbcUrl", PostgresUtil.URL_WITH_AUTH,
            "--jdbcDriver", PostgresUtil.DRIVER,
            "--table", EXPORTED_TABLE_NAME,
            "--mode", saveMode
        );
    }
}

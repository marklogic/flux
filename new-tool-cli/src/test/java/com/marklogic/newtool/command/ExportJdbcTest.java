package com.marklogic.newtool.command;

import com.marklogic.newtool.AbstractTest;
import org.apache.spark.sql.AnalysisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.*;

class ExportJdbcTest extends AbstractTest {

    private static final String EXPORTED_TABLE_NAME = "export_test";

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void dropTestTableIfItExists() {
        DriverManagerDataSource ds = new DriverManagerDataSource(PostgresUtil.URL_WITH_AUTH);
        ds.setDriverClassName(PostgresUtil.DRIVER);
        this.jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + EXPORTED_TABLE_NAME);
    }

    @Test
    void simpleTest() {
        exportFifteenAuthors();
        verifyRowCountInTable(15, "15 authors should have been written to the table.");

        this.jdbcTemplate.query(String.format("select * from %s where \"LastName\" = 'Golby'", EXPORTED_TABLE_NAME), resultSet -> {
            assertEquals(1, resultSet.getInt("CitationID"));
            assertEquals("Golby", resultSet.getString("LastName"));
            assertEquals("Pen", resultSet.getString("ForeName"));
            assertEquals("2022-07-13", resultSet.getString("Date"));
            assertEquals("2022-07-13 05:00:00", resultSet.getString("DateTime"));
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
    void errorIfExists() {
        exportFifteenAuthors();
        verifyRowCountInTable(15);

        AnalysisException ex = assertThrows(AnalysisException.class, () -> exportFifteenAuthorsWithMode("errorifexists"));
        assertEquals("Table or view 'export_test' already exists. SaveMode: ErrorIfExists.", ex.getMessage(),
            "Not sure about error handling yet; for now, just expecting the exception to be thrown.");
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
            "--clientUri", makeClientUri(),
            "--query", "op.fromView('Medical', 'Authors', '')",
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
            "--clientUri", makeClientUri(),
            "--query", "op.fromView('Medical', 'Authors', '')",
            "--jdbcUrl", PostgresUtil.URL_WITH_AUTH,
            "--jdbcDriver", PostgresUtil.DRIVER,
            "--table", EXPORTED_TABLE_NAME,
            "--mode", saveMode
        );
    }

    private void verifyRowCountInTable(int expectedCount) {
        verifyRowCountInTable(expectedCount, "Unexpected count");
    }

    private void verifyRowCountInTable(int expectedCount, String message) {
        this.jdbcTemplate.query("select count(*) from " + EXPORTED_TABLE_NAME, resultSet -> {
            int count = resultSet.getInt("count");
            assertEquals(expectedCount, count, message);
        });
    }
}
package com.marklogic.newtool;

import com.marklogic.newtool.impl.PostgresUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Base classes that need some plumbing for tests that will write data to the test Postgres database.
 */
public abstract class AbstractExportJdbcTest extends AbstractTest {

    protected static final String EXPORTED_TABLE_NAME = "export_test";

    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void dropTestTableIfItExists() {
        DriverManagerDataSource ds = new DriverManagerDataSource(PostgresUtil.URL_WITH_AUTH);
        ds.setDriverClassName(PostgresUtil.DRIVER);
        this.jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + EXPORTED_TABLE_NAME);
    }


    protected final void verifyRowCountInTable(int expectedCount) {
        verifyRowCountInTable(expectedCount, "Unexpected count");
    }

    protected final void verifyRowCountInTable(int expectedCount, String message) {
        this.jdbcTemplate.query("select count(*) from " + EXPORTED_TABLE_NAME, resultSet -> {
            int count = resultSet.getInt("count");
            assertEquals(expectedCount, count, message);
        });
    }
}

package com.marklogic.flux.api;

import java.util.function.Consumer;

/**
 * Read rows via JDBC and write JSON or XML documents to MarkLogic.
 */
public interface JdbcImporter extends Executor<JdbcImporter> {

    interface ReadJdbcOptions extends JdbcOptions<ReadJdbcOptions> {
        ReadJdbcOptions query(String query);

        ReadJdbcOptions groupBy(String groupBy);

        ReadJdbcOptions aggregateColumns(String newColumnName, String... columns);
    }

    JdbcImporter from(Consumer<ReadJdbcOptions> consumer);

    JdbcImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}

package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface JdbcImporter extends Executor<JdbcImporter> {

    interface ReadJdbcOptions extends JdbcOptions<ReadJdbcOptions> {
        ReadJdbcOptions query(String query);

        ReadJdbcOptions groupBy(String groupBy);

        // Could also support something nicer like withAggregationExpression(String newColumnName, String... columns)
        ReadJdbcOptions aggregationExpressions(String... expressions);
    }

    JdbcImporter readJdbc(Consumer<ReadJdbcOptions> consumer);

    JdbcImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);
}

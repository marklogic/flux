package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface JdbcExporter extends Executor<JdbcExporter> {

    interface WriteRowsOptions extends JdbcOptions<WriteRowsOptions> {
        WriteRowsOptions table(String table);

        WriteRowsOptions saveMode(SaveMode saveMode);
    }

    JdbcExporter from(Consumer<ReadRowsOptions> consumer);

    JdbcExporter from(String opticQuery);

    JdbcExporter to(Consumer<WriteRowsOptions> consumer);
}

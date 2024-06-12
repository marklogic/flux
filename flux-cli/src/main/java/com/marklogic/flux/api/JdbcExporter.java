package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface JdbcExporter extends Executor<JdbcExporter> {

    interface WriteRowsOptions extends JdbcOptions<WriteRowsOptions> {
        WriteRowsOptions table(String table);

        WriteRowsOptions saveMode(SaveMode saveMode);
    }

    JdbcExporter readRows(Consumer<ReadRowsOptions> consumer);

    JdbcExporter readRows(String opticQuery);

    JdbcExporter writeRows(Consumer<WriteRowsOptions> consumer);
}

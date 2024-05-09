package com.marklogic.newtool.api;

import org.apache.spark.sql.SaveMode;

import java.util.function.Consumer;

public interface JdbcExporter extends Executor<JdbcExporter> {

    interface WriteRowsOptions extends JdbcOptions<WriteRowsOptions> {
        WriteRowsOptions table(String table);

        WriteRowsOptions saveMode(SaveMode saveMode);
    }

    JdbcExporter readRows(Consumer<ReadRowsOptions> consumer);

    JdbcExporter writeRows(Consumer<WriteRowsOptions> consumer);
}

package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface ParquetFilesExporter extends Executor<ParquetFilesExporter> {

    ParquetFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    ParquetFilesExporter readRows(String opticQuery);

    ParquetFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);

    ParquetFilesExporter writeFiles(String path);
}

package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface ParquetFilesExporter extends Executor<ParquetFilesExporter> {

    ParquetFilesExporter from(Consumer<ReadRowsOptions> consumer);

    ParquetFilesExporter from(String opticQuery);

    ParquetFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    ParquetFilesExporter to(String path);
}

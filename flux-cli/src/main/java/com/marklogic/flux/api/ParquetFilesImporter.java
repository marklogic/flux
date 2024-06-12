package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface ParquetFilesImporter extends Executor<ParquetFilesImporter> {

    ParquetFilesImporter from(Consumer<ReadSparkFilesOptions> consumer);

    ParquetFilesImporter from(String... paths);

    ParquetFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}

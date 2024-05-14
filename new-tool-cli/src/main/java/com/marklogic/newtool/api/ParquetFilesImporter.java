package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface ParquetFilesImporter extends Executor<ParquetFilesImporter> {

    ParquetFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer);

    ParquetFilesImporter readFiles(String... paths);

    ParquetFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);
}

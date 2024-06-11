package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface OrcFilesImporter extends Executor<OrcFilesImporter> {

    OrcFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer);

    OrcFilesImporter readFiles(String... paths);

    OrcFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);
}

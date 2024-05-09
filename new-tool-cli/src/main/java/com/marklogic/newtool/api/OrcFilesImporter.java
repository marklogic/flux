package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface OrcFilesImporter extends Executor<OrcFilesImporter> {

    OrcFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer);

    OrcFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);
}

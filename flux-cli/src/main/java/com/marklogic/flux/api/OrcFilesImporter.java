package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface OrcFilesImporter extends Executor<OrcFilesImporter> {

    OrcFilesImporter from(Consumer<ReadSparkFilesOptions> consumer);

    OrcFilesImporter from(String... paths);

    OrcFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}

package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface DelimitedFilesImporter extends Executor<DelimitedFilesImporter> {

    DelimitedFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer);

    DelimitedFilesImporter readFiles(String... paths);

    DelimitedFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);

}

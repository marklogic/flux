package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface DelimitedFilesImporter extends Executor<DelimitedFilesImporter> {

    DelimitedFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer);

    DelimitedFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);

}

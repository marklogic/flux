package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface AvroFilesImporter extends Executor<AvroFilesImporter> {

    AvroFilesImporter readFiles(Consumer<ReadSparkFilesOptions> consumer);

    AvroFilesImporter writeDocuments(Consumer<WriteStructuredDocumentsOptions> consumer);
}

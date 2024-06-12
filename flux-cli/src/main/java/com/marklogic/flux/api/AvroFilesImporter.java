package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface AvroFilesImporter extends Executor<AvroFilesImporter> {

    AvroFilesImporter from(Consumer<ReadSparkFilesOptions> consumer);

    AvroFilesImporter from(String... paths);

    AvroFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);
}

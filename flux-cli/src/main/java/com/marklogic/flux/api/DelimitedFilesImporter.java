package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface DelimitedFilesImporter extends Executor<DelimitedFilesImporter> {

    DelimitedFilesImporter from(Consumer<ReadSparkFilesOptions> consumer);

    DelimitedFilesImporter from(String... paths);

    DelimitedFilesImporter to(Consumer<WriteStructuredDocumentsOptions> consumer);

}

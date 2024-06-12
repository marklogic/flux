package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface JsonLinesFilesExporter extends Executor<JsonLinesFilesExporter> {

    JsonLinesFilesExporter from(Consumer<ReadRowsOptions> consumer);

    JsonLinesFilesExporter from(String opticQuery);

    JsonLinesFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    JsonLinesFilesExporter to(String path);
}

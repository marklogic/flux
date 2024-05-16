package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface JsonLinesFilesExporter extends Executor<JsonLinesFilesExporter> {

    JsonLinesFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    JsonLinesFilesExporter readRows(String opticQuery);

    JsonLinesFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);

    JsonLinesFilesExporter writeFiles(String path);
}

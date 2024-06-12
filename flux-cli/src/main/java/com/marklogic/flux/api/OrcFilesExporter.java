package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface OrcFilesExporter extends Executor<OrcFilesExporter> {

    OrcFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    OrcFilesExporter readRows(String opticQuery);

    OrcFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);

    OrcFilesExporter writeFiles(String path);
}

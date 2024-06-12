package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface OrcFilesExporter extends Executor<OrcFilesExporter> {

    OrcFilesExporter from(Consumer<ReadRowsOptions> consumer);

    OrcFilesExporter from(String opticQuery);

    OrcFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    OrcFilesExporter to(String path);
}

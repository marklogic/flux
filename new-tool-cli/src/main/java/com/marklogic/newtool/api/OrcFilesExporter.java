package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface OrcFilesExporter extends Executor<OrcFilesExporter> {

    OrcFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    OrcFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);
}

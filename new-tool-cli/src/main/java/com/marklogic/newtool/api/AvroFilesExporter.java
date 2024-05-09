package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface AvroFilesExporter extends Executor<AvroFilesExporter> {

    AvroFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    AvroFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);
}

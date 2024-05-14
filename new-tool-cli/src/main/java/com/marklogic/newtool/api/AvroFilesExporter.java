package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface AvroFilesExporter extends Executor<AvroFilesExporter> {

    AvroFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    AvroFilesExporter readRows(String opticQuery);

    AvroFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);

    AvroFilesExporter writeFiles(String path);
}

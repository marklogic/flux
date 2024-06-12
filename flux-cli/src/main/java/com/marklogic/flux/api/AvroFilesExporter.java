package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface AvroFilesExporter extends Executor<AvroFilesExporter> {

    AvroFilesExporter from(Consumer<ReadRowsOptions> consumer);

    AvroFilesExporter from(String opticQuery);

    AvroFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    AvroFilesExporter to(String path);
}

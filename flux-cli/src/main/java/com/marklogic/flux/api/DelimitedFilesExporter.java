package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface DelimitedFilesExporter extends Executor<DelimitedFilesExporter> {

    DelimitedFilesExporter from(Consumer<ReadRowsOptions> consumer);

    DelimitedFilesExporter from(String opticQuery);

    DelimitedFilesExporter to(Consumer<WriteSparkFilesOptions> consumer);

    DelimitedFilesExporter to(String path);
}

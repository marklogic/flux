package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface DelimitedFilesExporter extends Executor<DelimitedFilesExporter> {

    DelimitedFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    DelimitedFilesExporter readRows(String opticQuery);

    DelimitedFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);

    DelimitedFilesExporter writeFiles(String path);
}

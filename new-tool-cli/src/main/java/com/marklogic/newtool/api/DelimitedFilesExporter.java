package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface DelimitedFilesExporter extends Executor<DelimitedFilesExporter> {

    DelimitedFilesExporter readRows(Consumer<ReadRowsOptions> consumer);

    DelimitedFilesExporter writeFiles(Consumer<WriteSparkFilesOptions> consumer);
}

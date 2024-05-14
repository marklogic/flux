package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface CustomRowsExporter extends Executor<CustomRowsExporter> {

    CustomRowsExporter readRows(Consumer<ReadRowsOptions> consumer);

    CustomRowsExporter readRows(String opticQuery);

    CustomRowsExporter writeRows(Consumer<CustomExportWriteOptions> consumer);
}

package com.marklogic.flux.api;

import java.util.function.Consumer;

public interface CustomRowsExporter extends Executor<CustomRowsExporter> {

    CustomRowsExporter from(Consumer<ReadRowsOptions> consumer);

    CustomRowsExporter from(String opticQuery);

    CustomRowsExporter to(Consumer<CustomExportWriteOptions> consumer);
}

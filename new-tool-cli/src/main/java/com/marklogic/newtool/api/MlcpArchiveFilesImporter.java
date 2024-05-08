package com.marklogic.newtool.api;

import java.util.function.Consumer;

public interface MlcpArchiveFilesImporter extends Executor<MlcpArchiveFilesImporter> {

    interface ReadMlcpArchiveFilesOptions extends ReadFilesOptions<ReadMlcpArchiveFilesOptions> {
        ReadMlcpArchiveFilesOptions categories(String... categories);
    }

    MlcpArchiveFilesImporter readFiles(Consumer<ReadMlcpArchiveFilesOptions> consumer);

    MlcpArchiveFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer);
}

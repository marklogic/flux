package com.marklogic.newtool.api;

import com.marklogic.newtool.command.importdata.*;

public interface NT {

    static AggregateXmlFilesImporter importAggregateXmlFiles() {
        return new ImportAggregateXmlCommand();
    }

    static ArchivesImporter importArchives() {
        return new ImportArchivesCommand();
    }

    static AvroFilesImporter importAvroFiles() {
        return new ImportAvroFilesCommand();
    }

    static DelimitedFilesImporter importDelimitedFiles() {
        return new ImportDelimitedFilesCommand();
    }

    /**
     * @return an object that can import any type of file as-is, with the document type being determined by
     * the file extension.
     */
    static GenericFilesImporter importGenericFiles() {
        return new ImportFilesCommand();
    }

    static JsonFilesImporter importJsonFiles() {
        return new ImportJsonFilesCommand();
    }

    static MlcpArchivesImporter importMlcpArchives() {
        return new ImportMlcpArchivesCommand();
    }

    static OrcFilesImporter importOrcFiles() {
        return new ImportOrcFilesCommand();
    }
    
    static ParquetFilesImporter importParquetFiles() {
        return new ImportParquetFilesCommand();
    }
}

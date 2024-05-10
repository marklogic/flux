package com.marklogic.newtool.api;

import com.marklogic.newtool.command.export.*;
import com.marklogic.newtool.command.importdata.*;

public interface NT {

    static ArchiveFilesExporter exportArchiveFiles() {
        return new ExportArchivesCommand();
    }

    static AvroFilesExporter exportAvroFiles() {
        return new ExportAvroFilesCommand();
    }

    static DelimitedFilesExporter exportDelimitedFiles() {
        return new ExportDelimitedFilesCommand();
    }

    static GenericFilesExporter exportGenericFiles() {
        return new ExportFilesCommand();
    }

    static JdbcExporter exportJdbc() {
        return new ExportJdbcCommand();
    }

    static OrcFilesExporter exportOrcFiles() {
        return new ExportOrcFilesCommand();
    }

    static ParquetFilesExporter exportParquetFiles() {
        return new ExportParquetFilesCommand();
    }

    static RdfFilesExporter exportRdfFiles() {
        return new ExportRdfFilesCommand();
    }

    /**
     * @return an object that can import aggregate XML files, where each instance of a particular child element is
     * written to MarkLogic as a separate document.
     */
    static AggregateXmlFilesImporter importAggregateXmlFiles() {
        return new ImportAggregateXmlCommand();
    }

    /**
     * @return an object that can import archive files - i.e. ZIP files that contain documents and metadata.
     */
    static ArchiveFilesImporter importArchiveFiles() {
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

    static JdbcImporter importJdbc() {
        return new ImportJdbcCommand();
    }

    static JsonFilesImporter importJsonFiles() {
        return new ImportJsonFilesCommand();
    }

    /**
     * @return an object that can import archive files created by MLCP.
     */
    static MlcpArchiveFilesImporter importMlcpArchiveFiles() {
        return new ImportMlcpArchivesCommand();
    }

    static OrcFilesImporter importOrcFiles() {
        return new ImportOrcFilesCommand();
    }

    static ParquetFilesImporter importParquetFiles() {
        return new ImportParquetFilesCommand();
    }

    static RdfFilesImporter importRdfFiles() {
        return new ImportRdfFilesCommand();
    }
}

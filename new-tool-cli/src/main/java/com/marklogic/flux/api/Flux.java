package com.marklogic.flux.api;

import com.marklogic.flux.impl.copy.CopyCommand;
import com.marklogic.flux.impl.custom.CustomExportDocumentsCommand;
import com.marklogic.flux.impl.custom.CustomExportRowsCommand;
import com.marklogic.flux.impl.custom.CustomImportCommand;
import com.marklogic.flux.impl.export.*;
import com.marklogic.flux.impl.importdata.*;
import com.marklogic.flux.impl.reprocess.ReprocessCommand;

public abstract class Flux {

    public static DocumentCopier copyDocuments() {
        return new CopyCommand();
    }

    public static CustomImporter customImport() {
        return new CustomImportCommand();
    }

    public static CustomDocumentsExporter customExportDocuments() {
        return new CustomExportDocumentsCommand();
    }

    public static CustomRowsExporter customExportRows() {
        return new CustomExportRowsCommand();
    }

    public static ArchiveFilesExporter exportArchiveFiles() {
        return new ExportArchiveFilesCommand();
    }

    public static AvroFilesExporter exportAvroFiles() {
        return new ExportAvroFilesCommand();
    }

    public static DelimitedFilesExporter exportDelimitedFiles() {
        return new ExportDelimitedFilesCommand();
    }

    public static GenericFilesExporter exportGenericFiles() {
        return new ExportFilesCommand();
    }

    public static JdbcExporter exportJdbc() {
        return new ExportJdbcCommand();
    }

    public static JsonLinesFilesExporter exportJsonLinesFiles() {
        return new ExportJsonLinesFilesCommand();
    }

    public static OrcFilesExporter exportOrcFiles() {
        return new ExportOrcFilesCommand();
    }

    public static ParquetFilesExporter exportParquetFiles() {
        return new ExportParquetFilesCommand();
    }

    public static RdfFilesExporter exportRdfFiles() {
        return new ExportRdfFilesCommand();
    }

    /**
     * @return an object that can import aggregate XML files, where each instance of a particular child element is
     * written to MarkLogic as a separate document.
     */
    public static AggregateXmlFilesImporter importAggregateXmlFiles() {
        return new ImportAggregateXmlCommand();
    }

    /**
     * @return an object that can import archive files - i.e. ZIP files that contain documents and metadata.
     */
    public static ArchiveFilesImporter importArchiveFiles() {
        return new ImportArchiveFilesCommand();
    }

    public static AvroFilesImporter importAvroFiles() {
        return new ImportAvroFilesCommand();
    }

    public static DelimitedFilesImporter importDelimitedFiles() {
        return new ImportDelimitedFilesCommand();
    }

    /**
     * @return an object that can import any type of file as-is, with the document type being determined by
     * the file extension.
     */
    public static GenericFilesImporter importGenericFiles() {
        return new ImportFilesCommand();
    }

    public static JdbcImporter importJdbc() {
        return new ImportJdbcCommand();
    }

    public static JsonFilesImporter importJsonFiles() {
        return new ImportJsonFilesCommand();
    }

    /**
     * @return an object that can import archive files created by MLCP.
     */
    public static MlcpArchiveFilesImporter importMlcpArchiveFiles() {
        return new ImportMlcpArchiveFilesCommand();
    }

    public static OrcFilesImporter importOrcFiles() {
        return new ImportOrcFilesCommand();
    }

    public static ParquetFilesImporter importParquetFiles() {
        return new ImportParquetFilesCommand();
    }

    public static RdfFilesImporter importRdfFiles() {
        return new ImportRdfFilesCommand();
    }

    public static Reprocessor reprocess() {
        return new ReprocessCommand();
    }

    private Flux() {
    }
}

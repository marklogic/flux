/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.api;

import com.marklogic.flux.impl.copy.CopyCommand;
import com.marklogic.flux.impl.custom.CustomExportDocumentsCommand;
import com.marklogic.flux.impl.custom.CustomExportRowsCommand;
import com.marklogic.flux.impl.custom.CustomImportCommand;
import com.marklogic.flux.impl.export.*;
import com.marklogic.flux.impl.importdata.*;
import com.marklogic.flux.impl.reprocess.ReprocessCommand;

/**
 * Entry point for executing Flux commands via the API.
 */
public interface Flux {

    static DocumentCopier copyDocuments() {
        return new CopyCommand();
    }

    static CustomImporter customImport() {
        return new CustomImportCommand();
    }

    static CustomDocumentsExporter customExportDocuments() {
        return new CustomExportDocumentsCommand();
    }

    static CustomRowsExporter customExportRows() {
        return new CustomExportRowsCommand();
    }

    static ArchiveFilesExporter exportArchiveFiles() {
        return new ExportArchiveFilesCommand();
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

    static JsonLinesFilesExporter exportJsonLinesFiles() {
        return new ExportJsonLinesFilesCommand();
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
     * @return an object that can import aggregate JSON files, where the files to import either contains an array of
     * JSON objects or conforms to the JSON Lines format.
     */
    static AggregateJsonFilesImporter importAggregateJsonFiles() {
        return new ImportAggregateJsonFilesCommand();
    }

    /**
     * @return an object that can import aggregate XML files, where each instance of a particular child element is
     * written to MarkLogic as a separate document.
     */
    static AggregateXmlFilesImporter importAggregateXmlFiles() {
        return new ImportAggregateXmlFilesCommand();
    }

    /**
     * @return an object that can import archive files - i.e. ZIP files that contain documents and metadata.
     */
    static ArchiveFilesImporter importArchiveFiles() {
        return new ImportArchiveFilesCommand();
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

    /**
     * @return an object that can import archive files created by MLCP.
     */
    static MlcpArchiveFilesImporter importMlcpArchiveFiles() {
        return new ImportMlcpArchiveFilesCommand();
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

    static Reprocessor reprocess() {
        return new ReprocessCommand();
    }
}

package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.spark.Options;

import java.util.Map;

@Parameters(commandDescription = "Read local, HDFS, and S3 files and write documents in MarkLogic.")
public class ImportFilesCommand extends AbstractImportFilesCommand {

    public enum DocumentType {
        JSON,
        TEXT,
        XML
    }

    @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
    private CompressionType compression;

    @Parameter(names = "--documentType", description = "Forces a type for any document with an unrecognized URI extension.")
    private DocumentType documentType;

    @Override
    protected String getReadFormat() {
        return (compression != null) ? MARKLOGIC_CONNECTOR : "binaryFile";
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        if (compression != null) {
            options.put(Options.READ_FILES_COMPRESSION, compression.name());
        }
        return options;
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = super.makeWriteOptions();
        if (documentType != null) {
            options.put(Options.WRITE_FILE_ROWS_DOCUMENT_TYPE, documentType.name());
        }
        return options;
    }
}

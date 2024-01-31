package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Parameters(commandDescription = "Read local, HDFS, and S3 files and write documents in MarkLogic.")
public class ImportFilesCommand extends AbstractCommand {

    public enum DocumentType {
        JSON,
        TEXT,
        XML
    }

    public enum CompressionType {
        ZIP,
        GZIP
    }

    @Parameter(required = true, names = "--path", description = "Specify one or more path expressions for selecting files to import.")
    private List<String> paths = new ArrayList<>();

    @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
    private CompressionType compression;

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

    @Parameter(names = "--documentType", description = "Forces a type for any document with an unrecognized URI extension.")
    private DocumentType documentType;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", paths);
        }
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        String format = (compression != null) ? MARKLOGIC_CONNECTOR : "binaryFile";
        return reader.format(format)
            .options(makeReadOptions())
            .load(paths.toArray(new String[]{}));
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteOptions())
            .mode(SaveMode.Append)
            .save();
    }

    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        options.putAll(writeDocumentParams.makeOptions());
        if (documentType != null) {
            options.put(Options.WRITE_FILE_ROWS_DOCUMENT_TYPE, documentType.name());
        }
        return options;
    }

    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        if (compression != null) {
            options.put(Options.READ_FILES_COMPRESSION, compression.name());
        }
        return options;
    }
}

package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportFilesCommand extends AbstractCommand {

    public enum DocumentType {
        JSON,
        TEXT,
        XML
    }

    @Parameter(required = true, names = "--path", description = "Specify one or more path expressions for selecting files to import.")
    private List<String> paths = new ArrayList<>();

    @ParametersDelegate
    private WriteDocumentParams writeDocumentParams = new WriteDocumentParams();

    @Parameter(names = "--documentType", description = "Forces a type for any document with an unrecognized URI extension.")
    private DocumentType documentType;

    // TODO Support other generic options at https://spark.apache.org/docs/latest/sql-data-sources-generic-options.html
    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", paths);
        }
        return reader.format("binaryFile")
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
            options.put(Options.WRITE_FILES_DOCUMENT_TYPE, documentType.name());
        }
        return options;
    }
}

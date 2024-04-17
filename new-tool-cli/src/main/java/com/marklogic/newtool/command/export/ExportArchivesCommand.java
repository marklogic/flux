package com.marklogic.newtool.command.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.newtool.command.S3Params;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

public class ExportArchivesCommand extends AbstractCommand {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @ParametersDelegate
    private ReadDocumentParams readDocumentParams = new ReadDocumentParams();

    @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of data to include. " +
        "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
    private String categories;

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    // Exporting archives tends to involve a large number of documents, such that getting one zip per partition may
    // be desirable. So no default value is given here.
    @Parameter(names = "--fileCount", description = "Specifies how many files should be written; also an alias for '--repartition'.")
    private Integer fileCount;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (fileCount != null && fileCount > 0) {
            getCommonParams().setRepartition(fileCount);
        }
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(OptionsUtil.makeOptions(Options.READ_DOCUMENTS_CATEGORIES, determineCategories()))
            .options(readDocumentParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .option(Options.WRITE_FILES_COMPRESSION, "zip")
            // The connector only supports "Append" in terms of how Spark defines it, but it will always overwrite files.
            .mode(SaveMode.Append)
            .save(path);
    }

    /**
     * While the "read documents" operation allows for only reading metadata, that isn't valid for an archive - we
     * always need content to be returned as well.
     *
     * @return
     */
    private String determineCategories() {
        if (categories != null && categories.trim().length() > 0) {
            return "content," + categories;
        }
        return "content,metadata";
    }
}

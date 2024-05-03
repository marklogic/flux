package com.marklogic.newtool.command.export;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.CompressionType;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.newtool.command.S3Params;
import com.marklogic.spark.Options;
import org.apache.spark.sql.*;

import java.util.Map;

@Parameters(commandDescription = "Read documents from MarkLogic and write them to a local filesystem, HDFS, or S3.")
public class ExportFilesCommand extends AbstractCommand {

    @Parameter(required = true, names = "--path", description = "Path expression for where files should be written.")
    private String path;

    @ParametersDelegate
    private ReadDocumentParams readDocumentParams = new ReadDocumentParams();

    @ParametersDelegate
    private S3Params s3Params = new S3Params();

    @Parameter(names = "--compression", description = "Set to 'ZIP' to write one zip file per partition, or to 'GZIP' to GZIP each document file.")
    private CompressionType compression;

    @Parameter(names = "--prettyPrint", description = "Pretty-print the contents of JSON and XML files.")
    private Boolean prettyPrint;

    @Parameter(names = "--zipFileCount", description = "Specifies how many ZIP files should be written when --compression is set to 'ZIP'; also an alias for '--repartition'.")
    private Integer zipFileCount;

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (zipFileCount != null && zipFileCount > 0) {
            getCommonParams().setRepartition(zipFileCount);
        }
        return reader.format(MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readDocumentParams.makeOptions())
            .load();
    }

    @Override
    protected void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        s3Params.addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteOptions())
            // The connector only supports "Append" in terms of how Spark defines it, but it will always overwrite files.
            .mode(SaveMode.Append)
            .save(path);
    }

    // Extracted so that it can be unit tested.
    protected Map<String, String> makeWriteOptions() {
        return OptionsUtil.makeOptions(
            Options.WRITE_FILES_COMPRESSION, compression != null ? compression.name() : null,
            Options.WRITE_FILES_PRETTY_PRINT, prettyPrint != null ? prettyPrint.toString() : null
        );
    }
}

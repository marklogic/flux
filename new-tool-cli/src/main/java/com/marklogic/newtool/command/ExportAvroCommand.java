package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;

public class ExportAvroCommand extends AbstractExportCommand {

    @Parameter(names = "--path", required = true)
    private String path;

    // TODO Add Avro-specific params.

    @Override
    protected void writeDataset(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format("avro").mode(SaveMode.Overwrite).save(path);
    }
}

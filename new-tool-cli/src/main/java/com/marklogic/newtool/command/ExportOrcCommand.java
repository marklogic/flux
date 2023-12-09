package com.marklogic.newtool.command;

import org.apache.spark.sql.DataFrameWriter;
import org.apache.spark.sql.Row;

public class ExportOrcCommand extends AbstractExportFilesCommand {

    // TODO Add ORC-specific params.
    
    @Override
    protected DataFrameWriter configureWriter(DataFrameWriter<Row> writer) {
        return writer.format("orc");
    }

}

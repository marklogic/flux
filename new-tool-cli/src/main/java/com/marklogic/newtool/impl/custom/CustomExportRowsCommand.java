package com.marklogic.newtool.impl.custom;

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.CustomExportWriteOptions;
import com.marklogic.newtool.api.CustomRowsExporter;
import com.marklogic.newtool.api.ReadRowsOptions;
import com.marklogic.newtool.impl.AbstractCommand;
import com.marklogic.newtool.impl.export.ReadRowsParams;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.function.Consumer;

@Parameters(commandDescription = "Read rows from MarkLogic and write them using a custom Spark connector or data source.")
public class CustomExportRowsCommand extends AbstractCustomExportCommand<CustomRowsExporter> implements CustomRowsExporter {

    @ParametersDelegate
    private ReadRowsParams readParams = new ReadRowsParams();

    @Override
    protected Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        return reader.format(AbstractCommand.MARKLOGIC_CONNECTOR)
            .options(getConnectionParams().makeOptions())
            .options(readParams.makeOptions())
            .load();
    }

    @Override
    public CustomRowsExporter readRows(Consumer<ReadRowsOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public CustomRowsExporter writeRows(Consumer<CustomExportWriteOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

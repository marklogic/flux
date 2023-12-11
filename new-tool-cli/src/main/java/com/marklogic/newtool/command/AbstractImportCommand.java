package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Base class for import job where "import" = read from somewhere, write to MarkLogic.
 */
abstract class AbstractImportCommand extends AbstractCommand {

    @Parameter(names = "--partitions", description = "number of Spark partitions, which becomes number of DMSDK threads")
    private Integer partitions = 4;

    @ParametersDelegate
    private WriteParams writeParams = new WriteParams();

    @Parameter(names = "--preview", description = "Set to true to log the dataset instead of writing it.")
    private boolean preview;

    protected final Optional<List<Row>> write(Supplier<Dataset<Row>> datasetSupplier) {
        long start = System.currentTimeMillis();

        Dataset<Row> dataset = datasetSupplier.get();
        List<Row> rows = null;
        if (preview) {
            rows = dataset.collectAsList();
        } else {
            dataset
                .repartition(partitions)
                .write()
                .format(MARKLOGIC_CONNECTOR)
                .options(getConnectionParams().makeOptions())
                .options(writeParams.makeOptions())
                .options(getCustomWriteOptions())
                .mode(writeParams.getSaveMode())
                .save();
        }

        logger.info("Completed, duration in ms: " + (System.currentTimeMillis() - start));
        return rows != null ? Optional.of(rows) : Optional.empty();
    }

    public WriteParams getWriteParams() {
        return writeParams;
    }

    public void setWriteParams(WriteParams writeParams) {
        this.writeParams = writeParams;
    }
}

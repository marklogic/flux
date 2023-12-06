package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.marklogic.spark.Options;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;

import java.util.function.Supplier;

/**
 * Base class for import job where "import" = read from somewhere, write to MarkLogic.
 */
abstract class AbstractImportCommand extends AbstractCommand {

    @Parameter(names = "--partitions", description = "number of Spark partitions, which becomes number of DMSDK threads")
    private Integer partitions = 4;

    @Parameter(names = "--collections", description = "Comma-delimited")
    private String collections;

    @Parameter(names = "--permissions")
    private String permissions = "new-tool-role,read,new-tool-role,update";

    @Parameter(names = "--uri-template")
    private String uriTemplate;

    @Parameter(names = "--uri-prefix")
    private String uriPrefix;

    @Parameter(names = "--uri-suffix")
    private String uriSuffix;

    @Parameter(names = "--save-mode")
    private SaveMode saveMode = SaveMode.Append;

    @Parameter(names = "--debug", description = "Set to true to log the dataset instead of writing it")
    private boolean debug = false;

    protected final void write(Supplier<Dataset<Row>> datasetSupplier) {
        long start = System.currentTimeMillis();

        Dataset<Row> dataset = datasetSupplier.get();
        if (debug) {
            dataset.collectAsList().forEach(row -> logger.info(row.prettyJson()));
        } else {
            dataset
                .repartition(partitions)
                .write()
                .format(MARKLOGIC_CONNECTOR)
                .options(makeWriteOptions(
                    Options.WRITE_PERMISSIONS, permissions,
                    Options.WRITE_URI_PREFIX, uriPrefix,
                    Options.WRITE_URI_SUFFIX, uriSuffix,
                    Options.WRITE_URI_TEMPLATE, uriTemplate,
                    Options.WRITE_COLLECTIONS, collections
                ))
                .mode(saveMode)
                .save();
        }

        logger.info("Completed, time: " + (System.currentTimeMillis() - start));
    }

    public void setPartitions(Integer partitions) {
        this.partitions = partitions;
    }

    public void setCollections(String collections) {
        this.collections = collections;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public void setSaveMode(SaveMode saveMode) {
        this.saveMode = saveMode;
    }

    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    public void setUriSuffix(String uriSuffix) {
        this.uriSuffix = uriSuffix;
    }
}

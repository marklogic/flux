package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.ParametersDelegate;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.newtool.SparkUtil;
import com.marklogic.newtool.api.FilesImporter;
import com.marklogic.newtool.command.AbstractCommand;
import com.marklogic.newtool.command.Preview;
import org.apache.spark.sql.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for commands that import files and write to MarkLogic.
 */
public abstract class AbstractImportFilesCommand<T extends FilesImporter<T>> extends AbstractCommand implements FilesImporter<T> {

    @ParametersDelegate
    private ReadFilesParams readFilesParams = new ReadFilesParams();

    @ParametersDelegate
    private WriteDocumentWithTemplateParams writeDocumentParams = new WriteDocumentWithTemplateParams();

    /**
     * Subclass must define the format used for reading - e.g. "csv", "marklogic", etc.
     *
     * @return the name of the Spark data source or connector to pass to the Spark 'format(String)' method
     */
    protected abstract String getReadFormat();

    @Override
    protected final Dataset<Row> loadDataset(SparkSession session, DataFrameReader reader) {
        if (logger.isInfoEnabled()) {
            logger.info("Importing files from: {}", readFilesParams.getPaths());
        }
        readFilesParams.getS3Params().addToHadoopConfiguration(session.sparkContext().hadoopConfiguration());
        return reader
            .format(getReadFormat())
            .options(makeReadOptions())
            .load(readFilesParams.getPaths().toArray(new String[]{}));
    }

    @Override
    protected final void applyWriter(SparkSession session, DataFrameWriter<Row> writer) {
        writer.format(MARKLOGIC_CONNECTOR)
            .options(makeWriteOptions())
            .mode(SaveMode.Append)
            .save();
    }

    /**
     * Subclasses can override this to add their own options.
     *
     * @return a map of options to pass to the Spark reader
     */
    protected Map<String, String> makeReadOptions() {
        return readFilesParams.makeOptions();
    }

    /**
     * Subclasses can override this to add their own options.
     *
     * @return a map of options to pass to the Spark writer
     */
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = getConnectionParams().makeOptions();
        options.putAll(writeDocumentParams.makeOptions());
        return options;
    }

    // Will eventually move up to AbstractCommand
    @Override
    public Optional<Preview> execute() {
        return execute(SparkUtil.buildSparkSession());
    }

    // Will eventually move up to AbstractCommand
    @Override
    public T withConnectionString(String connectionString) {
        getConnectionParams().setConnectionString(connectionString);
        return (T) this;
    }

    @Override
    public T withPath(String path) {
        readFilesParams.getPaths().add(path);
        return (T) this;
    }

    @Override
    public T withFilter(String filter) {
        readFilesParams.setFilter(filter);
        return (T) this;
    }

    @Override
    public T withRecursiveFileLookup(boolean value) {
        readFilesParams.setRecursiveFileLookup(value);
        return (T) this;
    }

    @Override
    public T withCollectionsString(String commaDelimitedCollections) {
        writeDocumentParams.setCollections(commaDelimitedCollections);
        return (T) this;
    }

    @Override
    public T withCollections(String... collections) {
        writeDocumentParams.setCollections(Stream.of(collections).collect(Collectors.joining(",")));
        return (T) this;
    }

    @Override
    public T withPermissionsString(String rolesAndCapabilities) {
        writeDocumentParams.setPermissions(rolesAndCapabilities);
        return (T) this;
    }

    @Override
    public T withPermissions(Map<String, Set<DocumentMetadataHandle.Capability>> permissions) {
        // This likely won't stay here, will get refactored to a non-command-specific location.
        StringBuilder sb = new StringBuilder();
        permissions.entrySet().stream().forEach(entry -> {
            String role = entry.getKey();
            entry.getValue().forEach(capability -> {
                if (!sb.toString().equals("")) {
                    sb.append(",");
                }
                sb.append(role).append(",").append(capability.name());
            });
        });
        writeDocumentParams.setPermissions(sb.toString());
        return (T) this;
    }
}

package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.newtool.SparkUtil;
import com.marklogic.newtool.api.GenericFilesImporter;
import com.marklogic.newtool.command.CompressionType;
import com.marklogic.newtool.command.Preview;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Parameters(commandDescription = "Read local, HDFS, and S3 files and write the contents of each file as a document in MarkLogic.")
public class ImportFilesCommand extends AbstractImportFilesCommand implements GenericFilesImporter {
    
    public enum DocumentType {
        JSON,
        TEXT,
        XML
    }

    @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
    private CompressionType compression;

    @Parameter(names = "--documentType", description = "Forces a type for any document with an unrecognized URI extension.")
    private DocumentType documentType;

    @Override
    protected String getReadFormat() {
        return (compression != null) ? MARKLOGIC_CONNECTOR : "binaryFile";
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        Map<String, String> options = super.makeReadOptions();
        if (compression != null) {
            options.put(Options.READ_FILES_COMPRESSION, compression.name());
        }
        return options;
    }

    @Override
    protected Map<String, String> makeWriteOptions() {
        Map<String, String> options = super.makeWriteOptions();
        if (documentType != null) {
            options.put(Options.WRITE_FILE_ROWS_DOCUMENT_TYPE, documentType.name());
        }
        return options;
    }

    @Override
    public Optional<Preview> execute() {
        return execute(SparkUtil.buildSparkSession());
    }

    @Override
    public GenericFilesImporter withConnectionString(String connectionString) {
        getConnectionParams().setConnectionString(connectionString);
        return this;
    }

    @Override
    public GenericFilesImporter withPath(String path) {
        getReadFilesParams().getPaths().add(path);
        return this;
    }

    @Override
    public GenericFilesImporter withCollectionsString(String commaDelimitedCollections) {
        getWriteDocumentParams().setCollections(commaDelimitedCollections);
        return this;
    }

    @Override
    public GenericFilesImporter withCollections(String... collections) {
        getWriteDocumentParams().setCollections(Stream.of(collections).collect(Collectors.joining(",")));
        return this;
    }

    @Override
    public GenericFilesImporter withPermissionsString(String rolesAndCapabilities) {
        getWriteDocumentParams().setPermissions(rolesAndCapabilities);
        return this;
    }

    @Override
    public GenericFilesImporter withPermissions(Map<String, Set<DocumentMetadataHandle.Capability>> permissions) {
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
        getWriteDocumentParams().setPermissions(sb.toString());
        return this;
    }
}

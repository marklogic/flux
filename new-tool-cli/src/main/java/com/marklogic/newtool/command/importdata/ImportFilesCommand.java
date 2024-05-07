package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
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
        JSON, TEXT, XML
    }

    @ParametersDelegate
    private ReadGenericFilesParams readParams = new ReadGenericFilesParams();

    @ParametersDelegate
    private WriteGenericDocumentsParams writeParams = new WriteGenericDocumentsParams();

    @Override
    protected String getReadFormat() {
        return (readParams.compression != null) ? MARKLOGIC_CONNECTOR : "binaryFile";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentWithTemplateParams getWriteParams() {
        return writeParams;
    }

    public static class ReadGenericFilesParams extends ReadFilesParams {
        @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
        private CompressionType compression;

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            if (compression != null) {
                options.put(Options.READ_FILES_COMPRESSION, compression.name());
            }
            return options;
        }
    }

    public static class WriteGenericDocumentsParams extends WriteDocumentWithTemplateParams {

        @Parameter(names = "--documentType", description = "Forces a type for any document with an unrecognized URI extension.")
        private DocumentType documentType;

        @Override
        public Map<String, String> makeOptions() {
            Map<String, String> options = super.makeOptions();
            if (documentType != null) {
                options.put(Options.WRITE_FILE_ROWS_DOCUMENT_TYPE, documentType.name());
            }
            return options;
        }
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
        getReadParams().getPaths().add(path);
        return this;
    }

    @Override
    public GenericFilesImporter withCollectionsString(String commaDelimitedCollections) {
        getWriteParams().setCollections(commaDelimitedCollections);
        return this;
    }

    @Override
    public GenericFilesImporter withCollections(String... collections) {
        getWriteParams().setCollections(Stream.of(collections).collect(Collectors.joining(",")));
        return this;
    }

    @Override
    public GenericFilesImporter withPermissionsString(String rolesAndCapabilities) {
        getWriteParams().setPermissions(rolesAndCapabilities);
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
        getWriteParams().setPermissions(sb.toString());
        return this;
    }
}

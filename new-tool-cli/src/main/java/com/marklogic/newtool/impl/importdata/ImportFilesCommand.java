package com.marklogic.newtool.impl.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.CompressionType;
import com.marklogic.newtool.api.GenericFilesImporter;
import com.marklogic.newtool.impl.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.function.Consumer;

@Parameters(commandDescription = "Read local, HDFS, and S3 files and write the contents of each file as a document in MarkLogic.")
public class ImportFilesCommand extends AbstractImportFilesCommand<GenericFilesImporter> implements GenericFilesImporter {

    @ParametersDelegate
    private ReadGenericFilesParams readParams = new ReadGenericFilesParams();

    @ParametersDelegate
    private WriteGenericDocumentsParams writeParams = new WriteGenericDocumentsParams();

    @Override
    protected String getReadFormat() {
        return (readParams.compressionType != null) ? MARKLOGIC_CONNECTOR : "binaryFile";
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeParams;
    }

    @Override
    public GenericFilesImporter readFiles(Consumer<ReadGenericFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public GenericFilesImporter readFiles(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public GenericFilesImporter writeDocuments(Consumer<WriteGenericDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    public static class ReadGenericFilesParams extends ReadFilesParams<ReadGenericFilesOptions> implements ReadGenericFilesOptions {

        private CompressionType compressionType;

        @Override
        @Parameter(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
        public ReadGenericFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @Parameter(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private Integer partitions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_NUM_PARTITIONS, partitions != null ? partitions.toString() : null,
                Options.READ_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null
            );
        }

        @Override
        public ReadGenericFilesOptions partitions(Integer partitions) {
            this.partitions = partitions;
            return this;
        }
    }

    public static class WriteGenericDocumentsParams extends WriteDocumentParams<WriteGenericDocumentsOptions> implements WriteGenericDocumentsOptions {

        private DocumentType documentType;

        @Override
        @Parameter(names = "--documentType", description = "Forces a type for any document with an unrecognized URI extension.")
        public WriteGenericDocumentsOptions documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.WRITE_FILE_ROWS_DOCUMENT_TYPE, documentType != null ? documentType.name() : null
            );
        }
    }
}

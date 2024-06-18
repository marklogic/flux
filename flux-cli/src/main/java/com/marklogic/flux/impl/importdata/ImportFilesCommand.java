/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.CompressionType;
import com.marklogic.flux.api.GenericFilesImporter;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;

@CommandLine.Command(
    name = "import-files",
    abbreviateSynopsis = true,
    description = "Read local, HDFS, and S3 files and write the contents of each file as a document in MarkLogic."
)
public class ImportFilesCommand extends AbstractImportFilesCommand<GenericFilesImporter> implements GenericFilesImporter {

    @CommandLine.ArgGroup(exclusive = false, heading = "Read Options\n", multiplicity = "1")
    private ReadGenericFilesParams readParams = new ReadGenericFilesParams();

    @CommandLine.ArgGroup(exclusive = false, heading = "Write Options\n")
    private WriteGenericDocumentsParams writeParams = new WriteGenericDocumentsParams();

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
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
    public GenericFilesImporter from(Consumer<ReadGenericFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public GenericFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public GenericFilesImporter to(Consumer<WriteGenericDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }

    public static class ReadGenericFilesParams extends ReadFilesParams<ReadGenericFilesOptions> implements ReadGenericFilesOptions {

        private CompressionType compressionType;

        @Override
        @CommandLine.Option(names = "--compression", description = "When importing compressed files, specify the type of compression used.")
        public ReadGenericFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
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
        @CommandLine.Option(names = "--document-type", description = "Forces a type for any document with an unrecognized URI extension.")
        public WriteGenericDocumentsOptions documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.WRITE_DOCUMENT_TYPE, documentType != null ? documentType.name() : null
            );
        }
    }
}

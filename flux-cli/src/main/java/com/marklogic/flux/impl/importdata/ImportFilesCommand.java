/*
 * Copyright (c) 2024-2025 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
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
    description = "Read local, HDFS, and S3 files and write the contents of each file as a document in MarkLogic."
)
public class ImportFilesCommand extends AbstractImportFilesCommand<GenericFilesImporter> implements GenericFilesImporter {

    @CommandLine.Mixin
    private ReadGenericFilesParams readParams = new ReadGenericFilesParams();

    @CommandLine.Mixin
    private WriteGenericDocumentsParams writeParams = new WriteGenericDocumentsParams();

    @CommandLine.Option(
        names = "--streaming",
        description = "Causes files to be read from their source directly to MarkLogic. Intended for importing large " +
            "files that cannot be fully read into memory. Features that depend on " +
            "the data in the file, such as --uri-template, will not have any effect when this option is set."
    )
    private boolean streaming;

    @Override
    public GenericFilesImporter streaming() {
        this.streaming = true;
        return this;
    }

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected ReadFilesParams getReadParams() {
        readParams.setStreaming(this.streaming);
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        writeParams.setStreaming(this.streaming);
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

        @CommandLine.Option(names = "--compression", description = "When importing compressed files, specify the type of compression used. "
            + OptionsUtil.VALID_VALUES_DESCRIPTION)
        private CompressionType compressionType;

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding when reading files.")
        private String encoding;

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private int partitions;

        @Override
        public ReadGenericFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @Override
        public ReadGenericFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_NUM_PARTITIONS, OptionsUtil.intOption(partitions),
                Options.READ_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null,
                Options.READ_FILES_ENCODING, encoding
            );
        }

        @Override
        public ReadGenericFilesOptions partitions(int partitions) {
            this.partitions = partitions;
            return this;
        }
    }

    public static class WriteGenericDocumentsParams extends WriteDocumentParams<WriteGenericDocumentsOptions> implements WriteGenericDocumentsOptions {

        private DocumentType documentType;

        @CommandLine.Option(
            names = "--extract-text",
            description = "Specifies that text should be extracted from each file and saved as a separate document."
        )
        private boolean extractText;

        @CommandLine.Option(
            names = "--extracted-text-document-type",
            description = "Specifies the type of document to create with the extracted text. Defaults to JSON."
        )
        private String extractedTextDocumentType;

        @CommandLine.Option(
            names = "--extracted-text-collections",
            description = "Comma-delimited sequence of collection names to add to each extracted text document."
        )
        private String extractedTextCollections;

        @CommandLine.Option(
            names = "--extracted-text-permissions",
            description = "Comma-delimited sequence of MarkLogic role names and capabilities to add to each extracted " +
                "text document - e.g. role1,read,role2,update,role3,execute."
        )
        private String extractedTextPermissions;

        @CommandLine.Option(
            names = "--extracted-text-drop-source",
            description = "Specify this to not write the content that text was extracted from as its own document."
        )
        private boolean extractedTextDropSource;

        @Override
        @CommandLine.Option(
            names = "--document-type",
            description = "Forces a type for any document with an unrecognized URI extension. " + OptionsUtil.VALID_VALUES_DESCRIPTION
        )
        public WriteGenericDocumentsOptions documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.WRITE_DOCUMENT_TYPE, documentType != null ? documentType.name() : null,
                Options.WRITE_EXTRACTED_TEXT, extractText ? "true" : null,
                Options.WRITE_EXTRACTED_TEXT_DOCUMENT_TYPE, extractedTextDocumentType,
                Options.WRITE_EXTRACTED_TEXT_COLLECTIONS, extractedTextCollections,
                Options.WRITE_EXTRACTED_TEXT_PERMISSIONS, extractedTextPermissions,
                Options.WRITE_EXTRACTED_TEXT_DROP_SOURCE, extractedTextDropSource ? "true" : null
            );
        }

        @Override
        public WriteGenericDocumentsOptions extractText() {
            this.extractText = true;
            return this;
        }

        @Override
        public WriteGenericDocumentsOptions extractedTextDocumentType(DocumentType documentType) {
            if (DocumentType.TEXT.equals(documentType)) {
                documentType = DocumentType.JSON;
            }
            this.extractedTextDocumentType = documentType.name();
            return this;
        }

        @Override
        public WriteGenericDocumentsOptions extractedTextCollections(String commaDelimitedCollections) {
            this.extractedTextCollections = commaDelimitedCollections;
            return this;
        }

        @Override
        public WriteGenericDocumentsOptions extractedTextPermissionsString(String rolesAndCapabilities) {
            this.extractedTextPermissions = rolesAndCapabilities;
            return this;
        }

        @Override
        public WriteGenericDocumentsOptions extractedTextDropSource() {
            this.extractedTextDropSource = true;
            return this;
        }
    }
}

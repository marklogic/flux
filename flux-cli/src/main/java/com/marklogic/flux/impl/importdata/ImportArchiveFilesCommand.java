/*
 * Copyright (c) 2024-2026 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.ArchiveFilesImporter;
import com.marklogic.flux.api.DocumentType;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "import-archive-files",
    description = "Read local, HDFS, and S3 Flux archive files and write the documents in each archive to MarkLogic."
)
public class ImportArchiveFilesCommand extends AbstractImportFilesCommand<ArchiveFilesImporter> implements ArchiveFilesImporter {

    @CommandLine.Mixin
    private ReadArchiveFilesParams readParams = new ReadArchiveFilesParams();

    @CommandLine.Mixin
    private WriteArchiveDocumentParams writeParams = new WriteArchiveDocumentParams();

    @CommandLine.Option(
        names = "--streaming",
        description = "Causes entries in archive files to be read from their source directly to MarkLogic. Intended for " +
            "importing archives containing large files that cannot be fully read into memory. Features that depend on " +
            "the data in the file, such as --uri-template, will not have any effect when this option is set."
    )
    private boolean streaming;

    @Override
    protected IReadFilesParams getReadParams() {
        readParams.setStreaming(this.streaming);
        return readParams;
    }

    @Override
    protected WriteArchiveDocumentParams getWriteParams() {
        writeParams.setStreaming(this.streaming);
        return writeParams;
    }

    @Override
    protected String getReadFormat() {
        return AbstractCommand.MARKLOGIC_CONNECTOR;
    }

    public static class WriteArchiveDocumentParams extends WriteDocumentParams<WriteArchiveDocumentsOptions> implements WriteArchiveDocumentsOptions {

        @CommandLine.Option(
            names = "--document-type",
            description = "Forces a type for any document with an unrecognized URI extension. " + OptionsUtil.VALID_VALUES_DESCRIPTION
        )
        private DocumentType documentType;

        @CommandLine.Option(
            names = "--streaming-transform-binary-with-extension",
            description = "Comma-delimited list of URI extensions that controls which documents with a format of BINARY " +
                "to send to a REST transform. Only applies when streaming entries to MarkLogic. Supports applying " +
                "a transform that can convert document to a binary so that the document type is not " +
                "determined by MarkLogic's mimetype mappings."
        )
        private String transformBinaryExtensions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.WRITE_DOCUMENT_TYPE, documentType != null ? documentType.name() : null,
                Options.STREAM_TRANSFORM_BINARY_EXTENSIONS, transformBinaryExtensions
            );
        }

        @Override
        public WriteArchiveDocumentsOptions documentType(DocumentType documentType) {
            this.documentType = documentType;
            return this;
        }

        @Override
        public WriteArchiveDocumentsOptions streamingTransformBinaryWithExtension(String commaDelimitedExtensions) {
            this.transformBinaryExtensions = commaDelimitedExtensions;
            return this;
        }
    }

    public static class ReadArchiveFilesParams extends ReadFilesParams<ReadArchiveFilesOptions> implements ReadArchiveFilesOptions {

        @CommandLine.Option(names = "--categories", description = "Comma-delimited sequence of categories of metadata to include. " +
            "If not specified, all types of metadata are included. " +
            "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
        private String categories;

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private int partitions;

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding when reading files.")
        private String encoding;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_FILES_TYPE, "archive",
                Options.READ_FILES_ENCODING, encoding,
                Options.READ_ARCHIVES_CATEGORIES, categories,
                Options.READ_NUM_PARTITIONS, OptionsUtil.intOption(partitions)
            );
        }

        @Override
        public ReadArchiveFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public ReadArchiveFilesOptions categories(String... categories) {
            this.categories = Stream.of(categories).collect(Collectors.joining(","));
            return this;
        }

        @Override
        public ReadArchiveFilesOptions partitions(int partitions) {
            this.partitions = partitions;
            return this;
        }
    }

    @Override
    public ArchiveFilesImporter from(Consumer<ReadArchiveFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public ArchiveFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public ArchiveFilesImporter streaming() {
        this.streaming = true;
        return this;
    }

    @Override
    public ArchiveFilesImporter toOptions(Consumer<WriteArchiveDocumentsOptions> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

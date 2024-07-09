/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.MlcpArchiveFilesImporter;
import com.marklogic.flux.api.WriteDocumentsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "import-mlcp-archive-files",
    description = "Read local, HDFS, and S3 archive files written by MLCP and write the documents in each archive to MarkLogic."
)
public class ImportMlcpArchiveFilesCommand extends AbstractImportFilesCommand<MlcpArchiveFilesImporter> implements MlcpArchiveFilesImporter {

    @CommandLine.Mixin
    private ReadMlcpArchiveFilesParams readParams = new ReadMlcpArchiveFilesParams();

    @CommandLine.Mixin
    private WriteDocumentParamsImpl writeParams = new WriteDocumentParamsImpl();

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected WriteDocumentParams getWriteParams() {
        return writeParams;
    }

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    public static class ReadMlcpArchiveFilesParams extends ReadFilesParams<ReadMlcpArchiveFilesOptions> implements ReadMlcpArchiveFilesOptions {

        @CommandLine.Option(names = "--categories", description = "Comma-delimited sequence of categories of metadata to include. " +
            "If not specified, all types of metadata are included. " +
            "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
        private String categories;

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding when reading files.")
        private String encoding;

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private Integer partitions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_FILES_TYPE, "mlcp_archive",
                Options.READ_ARCHIVES_CATEGORIES, categories,
                Options.READ_FILES_ENCODING, encoding,
                Options.READ_NUM_PARTITIONS, partitions != null ? partitions.toString() : null
            );
        }

        @Override
        public ReadMlcpArchiveFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        @Override
        public ReadMlcpArchiveFilesOptions categories(String... categories) {
            this.categories = Stream.of(categories).collect(Collectors.joining(","));
            return this;
        }

        @Override
        public ReadMlcpArchiveFilesOptions partitions(Integer partitions) {
            this.partitions = partitions;
            return this;
        }
    }

    @Override
    public MlcpArchiveFilesImporter from(Consumer<ReadMlcpArchiveFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public MlcpArchiveFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public MlcpArchiveFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

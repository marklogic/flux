package com.marklogic.flux.impl.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.flux.api.MlcpArchiveFilesImporter;
import com.marklogic.flux.api.WriteDocumentsOptions;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Parameters(commandDescription = "Read local, HDFS, and S3 archive files written by MLCP and write the documents in each archive to MarkLogic.")
public class ImportMlcpArchiveFilesCommand extends AbstractImportFilesCommand<MlcpArchiveFilesImporter> implements MlcpArchiveFilesImporter {

    @ParametersDelegate
    private ReadMlcpArchiveFilesParams readParams = new ReadMlcpArchiveFilesParams();

    @ParametersDelegate
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

        @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of metadata to include. " +
            "If not specified, all types of metadata are included. " +
            "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
        private String categories;

        @Parameter(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private Integer partitions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(super.makeOptions(),
                Options.READ_FILES_TYPE, "mlcp_archive",
                Options.READ_ARCHIVES_CATEGORIES, categories,
                Options.READ_NUM_PARTITIONS, partitions != null ? partitions.toString() : null
            );
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
    public MlcpArchiveFilesImporter readFiles(Consumer<ReadMlcpArchiveFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public MlcpArchiveFilesImporter readFiles(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public MlcpArchiveFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

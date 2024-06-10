package com.marklogic.newtool.impl.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.marklogic.newtool.api.AggregateXmlFilesImporter;
import com.marklogic.newtool.api.CompressionType;
import com.marklogic.newtool.api.WriteDocumentsOptions;
import com.marklogic.newtool.impl.AbstractCommand;
import com.marklogic.newtool.impl.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Parameters(commandDescription = "Read aggregate XML files from local, HDFS, and S3 locations using Spark's support " +
    "with each row being written to MarkLogic.")
public class ImportAggregateXmlCommand extends AbstractImportFilesCommand<AggregateXmlFilesImporter> implements AggregateXmlFilesImporter {

    @ParametersDelegate
    private ReadXmlFilesParams readParams = new ReadXmlFilesParams();

    @ParametersDelegate
    private WriteDocumentParamsImpl writeParams = new WriteDocumentParamsImpl();

    @Override
    protected String getReadFormat() {
        return AbstractCommand.MARKLOGIC_CONNECTOR;
    }

    @Override
    protected ReadFilesParams getReadParams() {
        return readParams;
    }

    @Override
    protected Supplier<Map<String, String>> getWriteParams() {
        return writeParams;
    }

    public static class ReadXmlFilesParams extends ReadFilesParams<ReadXmlFilesOptions> implements ReadXmlFilesOptions {

        @Parameter(required = true, names = "--element",
            description = "Specifies the local name of the element to use as the root of each document."
        )
        private String element;

        @Parameter(names = "--namespace",
            description = "Specifies the namespace of the element to use as the root of each document."
        )
        private String namespace;

        @Parameter(names = "--uri-element",
            description = "Specifies the local name of the element used for creating URIs."
        )
        private String uriElement;

        @Parameter(names = "--uri-namespace",
            description = "Specifies the namespace of the element used for creating URIs."
        )
        private String uriNamespace;

        @Parameter(names = "--compression",
            description = "When importing compressed files, specify the type of compression used."
        )
        private CompressionType compressionType;

        @Parameter(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private Integer partitions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(
                super.makeOptions(),
                Options.READ_NUM_PARTITIONS, partitions != null ? partitions.toString() : null,
                Options.READ_AGGREGATES_XML_ELEMENT, element,
                Options.READ_AGGREGATES_XML_NAMESPACE, namespace,
                Options.READ_AGGREGATES_XML_URI_ELEMENT, uriElement,
                Options.READ_AGGREGATES_XML_URI_NAMESPACE, uriNamespace,
                Options.READ_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null
            );
        }

        @Override
        public ReadXmlFilesOptions element(String element) {
            this.element = element;
            return this;
        }

        @Override
        public ReadXmlFilesOptions namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        @Override
        public ReadXmlFilesOptions uriElement(String uriElement) {
            this.uriElement = uriElement;
            return this;
        }

        @Override
        public ReadXmlFilesOptions uriNamespace(String uriNamespace) {
            this.uriNamespace = uriNamespace;
            return this;
        }

        @Override
        public ReadXmlFilesOptions compressionType(CompressionType compressionType) {
            this.compressionType = compressionType;
            return this;
        }

        @Override
        public ReadXmlFilesOptions partitions(Integer partitions) {
            this.partitions = partitions;
            return this;
        }
    }

    @Override
    protected void validateDuringApiUsage() {
        super.validateDuringApiUsage();
        OptionsUtil.validateRequiredOptions(readParams.makeOptions(),
            Options.READ_AGGREGATES_XML_ELEMENT, "Must specify an aggregate XML element name"
        );
    }

    @Override
    public AggregateXmlFilesImporter readFiles(Consumer<ReadXmlFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AggregateXmlFilesImporter readFiles(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public AggregateXmlFilesImporter writeDocuments(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

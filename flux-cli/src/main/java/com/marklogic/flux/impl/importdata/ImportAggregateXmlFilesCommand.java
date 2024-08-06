/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.AggregateXmlFilesImporter;
import com.marklogic.flux.api.CompressionType;
import com.marklogic.flux.api.WriteDocumentsOptions;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import picocli.CommandLine;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@CommandLine.Command(
    name = "import-aggregate-xml-files",
    description = "Read aggregate XML files from local, HDFS, and S3 locations with each row being written to MarkLogic."
)
public class ImportAggregateXmlFilesCommand extends AbstractImportFilesCommand<AggregateXmlFilesImporter> implements AggregateXmlFilesImporter {

    @CommandLine.Mixin
    private ReadXmlFilesParams readParams = new ReadXmlFilesParams();

    @CommandLine.Mixin
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

        @CommandLine.Option(required = true, names = "--element",
            description = "Specifies the local name of the element to use as the root of each document."
        )
        private String element;

        @CommandLine.Option(names = "--namespace",
            description = "Specifies the namespace of the element to use as the root of each document."
        )
        private String namespace;

        @CommandLine.Option(names = "--uri-element",
            description = "Specifies the local name of the element used for creating URIs."
        )
        private String uriElement;

        @CommandLine.Option(names = "--uri-namespace",
            description = "Specifies the namespace of the element used for creating URIs."
        )
        private String uriNamespace;

        @CommandLine.Option(names = "--compression",
            description = "When importing compressed files, specify the type of compression used. " + OptionsUtil.VALID_VALUES_DESCRIPTION
        )
        private CompressionType compressionType;

        @CommandLine.Option(names = "--encoding", description = "Specify an encoding when reading files.")
        private String encoding;

        @CommandLine.Option(names = "--partitions", description = "Specifies the number of partitions used for reading files.")
        private int partitions;

        @Override
        public Map<String, String> makeOptions() {
            return OptionsUtil.addOptions(
                super.makeOptions(),
                Options.READ_FILES_ENCODING, encoding,
                Options.READ_NUM_PARTITIONS, OptionsUtil.intOption(partitions),
                Options.READ_AGGREGATES_XML_ELEMENT, element,
                Options.READ_AGGREGATES_XML_NAMESPACE, namespace,
                Options.READ_AGGREGATES_XML_URI_ELEMENT, uriElement,
                Options.READ_AGGREGATES_XML_URI_NAMESPACE, uriNamespace,
                Options.READ_FILES_COMPRESSION, compressionType != null ? compressionType.name() : null
            );
        }

        @Override
        public ReadXmlFilesOptions encoding(String encoding) {
            this.encoding = encoding;
            return this;
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
        public ReadXmlFilesOptions partitions(int partitions) {
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
    public AggregateXmlFilesImporter from(Consumer<ReadXmlFilesOptions> consumer) {
        consumer.accept(readParams);
        return this;
    }

    @Override
    public AggregateXmlFilesImporter from(String... paths) {
        readParams.paths(paths);
        return this;
    }

    @Override
    public AggregateXmlFilesImporter to(Consumer<WriteDocumentsOptions<? extends WriteDocumentsOptions>> consumer) {
        consumer.accept(writeParams);
        return this;
    }
}

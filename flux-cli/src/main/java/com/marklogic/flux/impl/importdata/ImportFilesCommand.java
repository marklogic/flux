/*
 * Copyright © 2024 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.impl.importdata;

import com.marklogic.flux.api.CompressionType;
import com.marklogic.flux.api.GenericFilesImporter;
import com.marklogic.flux.impl.OptionsUtil;
import com.marklogic.spark.Options;
import com.marklogic.spark.udf.TextExtractor;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.UserDefinedFunction;
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
    protected Dataset<Row> afterDatasetLoaded(Dataset<Row> dataset) {
        // This might not be the right time, as it's before the limit/repartition stuff is implemented.
        if (writeParams.extractText) {
            dataset = dataset.withColumn("extractedText", TextExtractor.build().apply(new Column("content")));
        }
        Map<String, String> classifierOptions = writeParams.makeOptions();
        if (classifierOptions.containsKey(Options.WRITE_CLASSIFIER_HOST) && !classifierOptions.get(Options.WRITE_CLASSIFIER_HOST).isEmpty()) {
            UserDefinedFunction textClassifier = writeParams.getClassifierParams().buildTextClassifier();
            dataset = dataset.withColumn("classificationResponse", textClassifier.apply(new Column("content")));
        }
        return super.afterDatasetLoaded(dataset);
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

        @CommandLine.Option(names = "--extract-text", description = "Specifies that text should be extracted from each " +
            "file and saved as a separate document.")
        private boolean extractText;

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
                Options.WRITE_DOCUMENT_TYPE, documentType != null ? documentType.name() : null
            );
        }
    }
}

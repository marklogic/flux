package com.marklogic.newtool.command.export;

import com.marklogic.newtool.AbstractOptionsTest;
import com.marklogic.spark.Options;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExportRdfFilesOptionsTest extends AbstractOptionsTest {

    @Test
    void test() {
        ExportRdfFilesCommand command = (ExportRdfFilesCommand) getCommand(
            "export_rdf_files",
            "--connectionString", "test:test@host:8000",
            "--uris", "/a1.json\n/a2.json",
            "--stringQuery", "hello",
            "--query", "<query/>",
            "--graphs", "g1,g2",
            "--collections", "c1,c2",
            "--directory", "/dir/",
            "--options", "my-options",
            "--batchSize", "50",
            "--partitionsPerForest", "2",
            "--path", "anywhere",
            "--format", "trig",
            "--graphOverride", "use-this-graph"
        );

        Map<String, String> options = command.readParams.get();
        assertEquals("/a1.json\n/a2.json", options.get(Options.READ_TRIPLES_URIS));
        assertEquals("hello", options.get(Options.READ_TRIPLES_STRING_QUERY));
        assertEquals("<query/>", options.get(Options.READ_TRIPLES_QUERY));
        assertEquals("g1,g2", options.get(Options.READ_TRIPLES_GRAPHS));
        assertEquals("c1,c2", options.get(Options.READ_TRIPLES_COLLECTIONS));
        assertEquals("/dir/", options.get(Options.READ_TRIPLES_DIRECTORY));
        assertEquals("my-options", options.get(Options.READ_TRIPLES_OPTIONS));
        assertEquals("50", options.get(Options.READ_BATCH_SIZE));
        assertEquals("2", options.get(Options.READ_DOCUMENTS_PARTITIONS_PER_FOREST));

        options = command.writeParams.get();
        assertEquals("trig", options.get(Options.WRITE_RDF_FILES_FORMAT));
        assertEquals("use-this-graph", options.get(Options.WRITE_RDF_FILES_GRAPH));
    }
}

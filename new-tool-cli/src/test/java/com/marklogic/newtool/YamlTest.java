package com.marklogic.newtool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.File;

public class YamlTest {

    @Test
    void test() throws Exception {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        JsonNode doc = om.readTree(new File("src/test/resources/args.yaml"));
        System.out.println(doc.toPrettyString());
    }
}

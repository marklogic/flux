package com.marklogic.newtool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.springframework.vault.support.JsonMapFlattener;

import java.io.File;
import java.util.Map;

public class YamlTest {

    /**
     * So.... what we could do here is decouple our commands from the JCommander API and instead.... have our
     * commands depend on a config map? Map<String, Object>?
     *
     * That would be nice in that the API and all the commands would no longer know about JCommander. But it would
     * eliminate CLI args and usage too.
     *
     * However, we could also map this to options, where we'd have to prefix the marklogic.client ones.
     *
     * @throws Exception
     */
    @Test
    void test() throws Exception {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        File f = new File("src/test/resources/args.yaml");
        Map<String, Object> map = om.readValue(f, Map.class);
        System.out.println(JsonMapFlattener.flattenToStringMap(map));
    }
}

package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExecuteCustomCommandTest extends AbstractTest {

    @Test
    void test() {
        run(
            "custom",
            "--class-name", "com.marklogic.newtool.TestCustomCommand",
            "-P:param1=value1",
            "-P:param2=value2"
        );

        Map<String, String> params = TestCustomCommand.getDynamicParameters();
        assertEquals("value1", params.get("param1"));
        assertEquals("value2", params.get("param2"));
    }
}

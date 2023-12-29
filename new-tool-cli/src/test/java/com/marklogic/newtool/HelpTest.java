package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class HelpTest extends AbstractTest {

    @Test
    void viewUsage() {
        run();
    }

    @Test
    void helpForSingleCommand() {
        String stdout = runAndReturnStdout(() -> run("help", "import_jdbc"));
        assertTrue(stdout.contains("Common Options:"));
        assertTrue(stdout.contains("import_jdbc"));
        assertFalse(stdout.contains("export_jdbc"));
        System.out.println(stdout);
    }
}

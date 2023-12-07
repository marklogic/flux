package com.marklogic.newtool;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HelpTest extends AbstractTest {

    @Test
    void test() {
        run();
    }

    @Test
    void helpForSingleCommand() throws IOException {
        PrintStream stdout = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        run("help", "import_files");

        outputStream.flush();
        System.setOut(stdout);
        String output = new String(outputStream.toByteArray());
        assertTrue(output.contains("Common Options:"));
        assertTrue(output.contains("import_files"));
        assertFalse(output.contains("import_jdbc"));
        
        System.out.println(output);
    }
}

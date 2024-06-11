package com.marklogic.flux.impl;

import com.marklogic.flux.AbstractTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OverrideSparkConfTest extends AbstractTest {

    /**
     * Verifies that the default spark.sql.session.timeZone of UTC that is set via SparkUtil can still be overridden
     * via the "-C" option. This test may intermittently fail, as runAndReturnStdout hasn't proven to be 100%
     * reliable.
     */
    @Test
    @Disabled("Another disabled test due to runAndReturnStdout not being reliable; should work fine when run manually.")
    void overriddenTimeZone() {
        String stdout = runAndReturnStdout(() -> run(
            "import-orc-files",
            "-Cspark.sql.session.timeZone=America/Los_Angeles",
            "--path", "src/test/resources/orc-files/authors.orc",
            "--preview", "1"
        ));

        assertTrue(stdout.contains("2022-07-13 02:00:00"),
            "The timeZone should have been overridden so that 02:00 is shown instead of " +
                "09:00 (the value shown for UTC); actual stdout: " + stdout);
    }

}

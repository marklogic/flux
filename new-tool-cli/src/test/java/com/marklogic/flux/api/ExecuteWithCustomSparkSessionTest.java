package com.marklogic.flux.api;

import com.marklogic.flux.AbstractTest;
import com.marklogic.flux.impl.SparkUtil;
import org.apache.spark.SparkFirehoseListener;
import org.apache.spark.scheduler.SparkListenerEvent;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecuteWithCustomSparkSessionTest extends AbstractTest {

    @Test
    void test() {
        SparkSession session = SparkUtil.buildSparkSession();
        TestListener testListener = new TestListener();
        session.sparkContext().addSparkListener(testListener);

        Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .readFiles("src/test/resources/mixed-files")
            .writeDocuments(options -> options
                .collections("custom-session")
                .permissionsString(DEFAULT_PERMISSIONS))
            .withSparkSession(session)
            .execute();

        assertTrue(testListener.events.size() > 0, "This verifies that our custom Spark session is used instead of " +
            "a default one. We don't care how many Spark events are captured. We just need proof that our custom listener " +
            "was invoked at least once.");
    }

    @Test
    void notASparkSession() {
        GenericFilesImporter importer = Flux.importGenericFiles()
            .connectionString(makeConnectionString())
            .readFiles("src/test/resources/mixed-files")
            .writeDocuments(options -> options
                .collections("custom-session")
                .permissionsString(DEFAULT_PERMISSIONS));

        FluxException ex = assertThrowsNtException(() -> importer.withSparkSession("This will cause an error"));
        assertEquals("The session object must be an instance of org.apache.spark.sql.SparkSession", ex.getMessage());
    }

    static class TestListener extends SparkFirehoseListener {

        List<SparkListenerEvent> events = new ArrayList<>();

        @Override
        public void onEvent(SparkListenerEvent event) {
            this.events.add(event);
        }
    }
}

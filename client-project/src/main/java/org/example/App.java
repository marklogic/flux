package org.example;

import com.marklogic.newtool.api.NT;

public class App {

    public static void main(String[] args) {
        // Currently depends on spark-etl test-app.
        NT.importFiles()
                .withConnectionString("new-tool-user:password@localhost:8003")
                .withPath("../new-tool-cli/src/test/resources/mixed-files")
                .withCollections("client-files")
                .withPermissionsString("new-tool-role,read,new-tool-role,update")
                .execute();
    }
}

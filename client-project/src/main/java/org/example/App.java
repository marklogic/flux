package org.example;

import com.marklogic.flux.api.Flux;

import java.security.DrbgParameters;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        // Currently depends on spark-etl test-app.
        Flux.importGenericFiles()
            .connectionString("new-tool-user:password@localhost:8003")
            .readFiles(options -> options
                .paths("../new-tool-cli/src/test/resources/mixed-files"))
            .writeDocuments(options -> options
                .collections("client-files")
                .permissionsString("new-tool-role,read,new-tool-role,update"))
            .execute();
    }
}

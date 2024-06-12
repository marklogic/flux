package org.example;

import com.marklogic.flux.api.Flux;

public class App {

    public static void main(String[] args) {
        // Currently depends on flux test-app.
        Flux.importGenericFiles()
            .connectionString("flux-test-user:password@localhost:8003")
            .readFiles(options -> options
                .paths("../flux-cli/src/test/resources/mixed-files"))
            .writeDocuments(options -> options
                .collections("client-files")
                .permissionsString("flux-test-role,read,flux-test-role,update"))
            .execute();
    }
}

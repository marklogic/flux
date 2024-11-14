package org.example;

import com.marklogic.flux.api.Flux;

public class App {

    public static void main(String[] args) {
        // Depends on the example application created in the Getting Started guide.
        Flux.importGenericFiles()
            .connectionString("flux-example-user:password@localhost:8004")
            .from(options -> options
                .paths("../../flux-cli/src/test/resources/mixed-files"))
            .to(options -> options
                .collections("client-files")
                .permissionsString("flux-example-role,read,flux-example-role,update"))
            .execute();
    }
}

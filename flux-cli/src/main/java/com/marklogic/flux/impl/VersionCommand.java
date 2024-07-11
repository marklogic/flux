package com.marklogic.flux.impl;

import org.apache.spark.sql.SparkSession;
import picocli.CommandLine;

import java.util.ListResourceBundle;
import java.util.Optional;
import java.util.ResourceBundle;

@CommandLine.Command(
    name = "version",
    description = "Print the version of Flux."
)
public class VersionCommand implements Command {

    private CommandLine commandLine;

    @CommandLine.Option(names = "--verbose", description = "Print additional details about Flux.")
    private boolean verbose;

    @Override
    public Optional<Preview> execute(SparkSession session) {
        ResourceBundle versionProperties = getResourceBundle();
        commandLine.getOut().println("Flux version: " + versionProperties.getString("version"));
        commandLine.getOut().println("Java version: " + System.getProperty("java.version"));
        commandLine.getOut().println("Spark version: " + session.version());
        if (verbose) {
            commandLine.getOut().println("Build time: " + versionProperties.getString("buildTime"));
        }
        return Optional.empty();
    }

    @Override
    public void validateCommandLineOptions(CommandLine.ParseResult parseResult) {
        this.commandLine = parseResult.commandSpec().commandLine();
    }

    private ResourceBundle getResourceBundle() {
        try {
            return ResourceBundle.getBundle("flux-version");
        } catch (Exception e) {
            // This should only occur in a development environment, and specifically only when running tests via
            // something besides Gradle.
            return new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return new Object[][]{
                        {"version", "Unknown; please ensure that flux-version.properties exists on the classpath."},
                        {"buildTime", "Unknown."}
                    };
                }
            };
        }
    }
}

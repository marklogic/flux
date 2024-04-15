package com.marklogic.newtool.command;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.spark.Options;

import java.util.Map;

@Parameters(commandDescription = "Read local, HDFS, and S3 archive files written via 'export_archives' and write the documents in each archive to MarkLogic.")
public class ImportArchivesCommand extends AbstractImportFilesCommand {

    @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of metadata to include. " +
        "If not specified, all types of metadata are included. " +
        "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
    private String categories;

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        return OptionsUtil.addOptions(super.makeReadOptions(),
            Options.READ_FILES_TYPE, "archive",
            Options.READ_ARCHIVES_CATEGORIES, categories
        );
    }
}

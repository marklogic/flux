package com.marklogic.newtool.command.importdata;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.newtool.api.MlcpArchivesImporter;
import com.marklogic.newtool.command.OptionsUtil;
import com.marklogic.spark.Options;

import java.util.Map;

@Parameters(commandDescription = "Read local, HDFS, and S3 archive files written by MLCP and write the documents in each archive to MarkLogic.")
public class ImportMlcpArchivesCommand extends AbstractImportFilesCommand<MlcpArchivesImporter> implements MlcpArchivesImporter {
    
    private String categories;

    @Override
    protected String getReadFormat() {
        return MARKLOGIC_CONNECTOR;
    }

    @Override
    protected Map<String, String> makeReadOptions() {
        return OptionsUtil.addOptions(super.makeReadOptions(),
            Options.READ_FILES_TYPE, "mlcp_archive",
            Options.READ_ARCHIVES_CATEGORIES, categories
        );
    }

    @Override
    @Parameter(names = "--categories", description = "Comma-delimited sequence of categories of metadata to include. " +
        "If not specified, all types of metadata are included. " +
        "Valid choices are: collections, permissions, quality, properties, and metadatavalues.")
    public MlcpArchivesImporter withCategoriesString(String commaDelimitedCategories) {
        this.categories = commaDelimitedCategories;
        return this;
    }
}

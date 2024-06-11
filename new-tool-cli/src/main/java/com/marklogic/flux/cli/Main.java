package com.marklogic.flux.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.MissingCommandException;
import com.beust.jcommander.ParameterException;
import com.marklogic.flux.impl.AbstractCommand;
import com.marklogic.flux.impl.Command;
import com.marklogic.flux.impl.Preview;
import com.marklogic.flux.impl.SparkUtil;
import com.marklogic.flux.impl.export.*;
import com.marklogic.flux.impl.importdata.*;
import com.marklogic.flux.impl.reprocess.ReprocessCommand;
import com.marklogic.flux.impl.copy.CopyCommand;
import com.marklogic.flux.impl.custom.CustomExportDocumentsCommand;
import com.marklogic.flux.impl.custom.CustomExportRowsCommand;
import com.marklogic.flux.impl.custom.CustomImportCommand;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger("com.marklogic.flux");

    // TBD Need to account for Windows.
    private static final String PROGRAM_NAME = "./bin/flux";

    private static final int COLUMN_SIZE = 120;

    private final String[] args;
    private final JCommander commander;
    private final Object selectedCommand;

    // Only used for logging
    private String selectedCommandName;

    // Sonar's not happy about stderr/stdout usage; will revisit this, ignoring warnings for now.
    @SuppressWarnings({"java:S106", "java:S4507"})
    public static void main(String[] args) {
        try {
            Main main = new Main(args);
            main.run();
        } catch (MissingCommandException ex) {
            System.err.println("Invalid command name: " + args[0]);
            System.err.println(String.format("To see all commands, run '%s' with no arguments.", PROGRAM_NAME));
        } catch (ParameterException ex) {
            System.err.println(determineErrorMessageForParameterException(ex));
            System.err.println(String.format("To see usage for the command, run '%s help %s'.", PROGRAM_NAME, args[0]));
        } catch (Exception ex) {
            for (String arg : args) {
                if ("--stacktrace".equals(arg)) {
                    ex.printStackTrace();
                }
            }
            System.err.println(String.format("%nCommand failed, cause: %s", ex.getMessage()));
        }
    }

    public Main(String... args) {
        this.args = args;
        this.commander = buildCommander();
        this.selectedCommand = getSelectedCommand(commander, args);
    }

    public void run() {
        if (selectedCommand == null) {
            commander.usage();
        } else if (selectedCommand instanceof HelpCommand) {
            String commandName = args[args.length - 1];
            ((HelpCommand) selectedCommand).printUsageForCommand(commander, commandName);
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Executing command: {}", selectedCommandName);
            }
            Command command = (Command) selectedCommand;
            SparkSession session = buildSparkSession(command);
            if (logger.isDebugEnabled()) {
                logger.debug("Spark master URL: {}", session.sparkContext().master());
            }
            Optional<Preview> preview = command.execute(session);
            if (preview.isPresent()) {
                preview.get().showPreview();
            }
        }
    }

    protected SparkSession buildSparkSession(Command selectedCommand) {
        String masterUrl = null;
        if (selectedCommand instanceof AbstractCommand) {
            masterUrl = ((AbstractCommand) selectedCommand).getCommonParams().getSparkMasterUrl();
        }
        return masterUrl != null && masterUrl.trim().length() > 0 ?
            SparkUtil.buildSparkSession(masterUrl) :
            SparkUtil.buildSparkSession();
    }

    private JCommander buildCommander() {
        JCommander jc = JCommander.newBuilder()
            .programName(PROGRAM_NAME)
            .addCommand("copy", new CopyCommand())
            .addCommand("custom_export_documents", new CustomExportDocumentsCommand())
            .addCommand("custom_export_rows", new CustomExportRowsCommand())
            .addCommand("custom_import", new CustomImportCommand())
            .addCommand("export_archive_files", new ExportArchiveFilesCommand())
            .addCommand("export_avro_files", new ExportAvroFilesCommand())
            .addCommand("export_delimited_files", new ExportDelimitedFilesCommand())
            .addCommand("export_files", new ExportFilesCommand())
            .addCommand("export_jdbc", new ExportJdbcCommand())
            .addCommand("export_json_lines_files", new ExportJsonLinesFilesCommand())
            .addCommand("export_orc_files", new ExportOrcFilesCommand())
            .addCommand("export_parquet_files", new ExportParquetFilesCommand())
            .addCommand("export_rdf_files", new ExportRdfFilesCommand())
            .addCommand("help", new HelpCommand(PROGRAM_NAME, COLUMN_SIZE))
            .addCommand("import_aggregate_xml_files", new ImportAggregateXmlCommand())
            .addCommand("import_archive_files", new ImportArchiveFilesCommand())
            .addCommand("import_avro_files", new ImportAvroFilesCommand())
            .addCommand("import_mlcp_archive_files", new ImportMlcpArchiveFilesCommand())
            .addCommand("import_delimited_files", new ImportDelimitedFilesCommand())
            .addCommand("import_files", new ImportFilesCommand())
            .addCommand("import_jdbc", new ImportJdbcCommand())
            .addCommand("import_json_files", new ImportJsonFilesCommand())
            .addCommand("import_orc_files", new ImportOrcFilesCommand())
            .addCommand("import_parquet_files", new ImportParquetFilesCommand())
            .addCommand("import_rdf_files", new ImportRdfFilesCommand())
            .addCommand("reprocess", new ReprocessCommand())
            .columnSize(COLUMN_SIZE)
            .build();
        jc.setUsageFormatter(new SummaryUsageFormatter(jc));
        return jc;
    }

    private Object getSelectedCommand(JCommander commander, String... args) {
        commander.parse(args);
        this.selectedCommandName = commander.getParsedCommand();
        if (this.selectedCommandName == null) {
            return null;
        }
        return commander.getCommands().get(this.selectedCommandName).getObjects().get(0);
    }

    private static String determineErrorMessageForParameterException(ParameterException ex) {
        String message = ex.getMessage();
        if (message.startsWith("Dynamic parameter expected a value of the form a=b")) {
            return "Options specified via '-C' or '-P' must have a form of -Ckey=value or -Pkey=value.";
        }
        boolean isInvalidParameterMessage = message.indexOf("'") > -1 && message.contains(" but no main parameter");
        if (isInvalidParameterMessage) {
            // JCommander's message for an invalid parameter is e.g.
            // "Was passed main parameter '--not-a-real-param' but no main parameter was defined in your arg class."
            // That is unlikely to make any sense to a user, so it's massaged here.
            message = message.substring(message.indexOf("'"));
            int pos = message.indexOf(" but no main parameter");
            if (pos != -1) {
                String paramName = message.substring(0, pos);
                return String.format("Invalid option: %s", paramName);
            }
        }
        return ex.getMessage();
    }

    public Object getSelectedCommand() {
        return selectedCommand;
    }
}

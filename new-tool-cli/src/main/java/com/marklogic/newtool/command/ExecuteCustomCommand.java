package com.marklogic.newtool.command;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.marklogic.etl.api.CustomCommand;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Parameters(commandDescription = "Execute a custom command via its fully-qualified class name.")
// TODO Do we extend AbstractCommand with this? Do we need custom read/write options?
public class ExecuteCustomCommand implements Command {

    @Parameter(names = "--class-name", description = "fully-qualified class name of custom command class to execute")
    private String className;

    @DynamicParameter(names = "-P:", description = "Parameters to pass to the custom command class")
    private Map<String, String> params = new HashMap<>();

    @Override
    public Optional<List<Row>> execute(SparkSession session) {
        CustomCommand command;
        try {
            command = (CustomCommand) Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class: " + className, e);
        }

        command.execute(session, params);
        return null;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}

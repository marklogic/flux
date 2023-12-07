package com.marklogic.newtool;

import com.beust.jcommander.DefaultUsageFormatter;
import com.beust.jcommander.IUsageFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterDescription;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UsageFormatter extends DefaultUsageFormatter implements IUsageFormatter {

    private final static Set<String> COMMON_PARAMETER_NAMES = new HashSet() {{
        add("--host");
        add("--port");
        add("--username");
        add("--password");
        add("-R:");
        add("-W:");
    }};

    private JCommander commander;
    private final String referenceCommandName;

    public UsageFormatter(JCommander commander) {
        this(commander, "import_jdbc");
    }

    public UsageFormatter(JCommander commander, String referenceCommandName) {
        super(commander);
        this.commander = commander;
        this.referenceCommandName = referenceCommandName;
    }

    // This is what we'd override to provide the common parameters
    // Ideally, we can call this with a list of the sorted params
    @Override
    public void appendAllParametersDetails(StringBuilder out, int indentCount, String indent, List<ParameterDescription> sortedParameters) {
        JCommander readRows = commander.getCommands().get(referenceCommandName);
        List<ParameterDescription> commonParams = readRows.getFields().values().stream()
            .filter(param -> COMMON_PARAMETER_NAMES.contains(param.getLongestName()))
            .collect(Collectors.toList());
        super.appendAllParametersDetails(out, indentCount, indent, commonParams);

        String content = out.toString();
        out.replace(0, content.length(), content.replace("  Options:", "  Common Options:"));
        out.append("\n");
    }
}

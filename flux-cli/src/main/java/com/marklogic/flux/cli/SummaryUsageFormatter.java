/*
 * Copyright © 2024 Progress Software Corporation and/or its subsidiaries or affiliates. All Rights Reserved.
 */
package com.marklogic.flux.cli;

import com.beust.jcommander.DefaultUsageFormatter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameters;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

class SummaryUsageFormatter extends DefaultUsageFormatter {

    private final JCommander commander;

    public SummaryUsageFormatter(JCommander commander) {
        super(commander);
        this.commander = commander;
    }

    /**
     * Overrides the parent method so that command parameters are not printed. Command names are right-padded as well
     * so that the names and descriptions line up nicely.
     */
    @Override
    public void appendCommands(StringBuilder out, int indentCount, int descriptionIndent, String indent) {
        out.append(indent + "  Commands:\n");
        final int longestNameLength = getLengthOfLongestCommandName();
        final int descriptionNewlineIndent = indentCount + descriptionIndent + longestNameLength;
        for (Map.Entry<JCommander.ProgramName, JCommander> commands : commander.getRawCommands().entrySet()) {
            Object arg = commands.getValue().getObjects().get(0);
            Parameters p = arg.getClass().getAnnotation(Parameters.class);
            if (p == null || !p.hidden()) {
                JCommander.ProgramName programName = commands.getKey();
                String displayName = StringUtils.rightPad(programName.getDisplayName(), longestNameLength);
                String commandDescription = getCommandDescription(programName.getName());
                String description = s(indentCount) + displayName + s(descriptionIndent) + commandDescription;
                wrapDescription(out, descriptionNewlineIndent, description);
                out.append("\n\n");
            }
        }
    }

    private int getLengthOfLongestCommandName() {
        int longestLength = 0;
        for (String name : commander.getCommands().keySet()) {
            if (name.length() > longestLength) {
                longestLength = name.length();
            }
        }
        return longestLength;
    }
}

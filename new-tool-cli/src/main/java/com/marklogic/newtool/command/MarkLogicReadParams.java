package com.marklogic.newtool.command;

/**
 * The idea here is that we can define all the params for reading via Optic and via custom code.
 * This would then be a delegate on every Import class. We'd have the same with WriteParams for every
 * Export class. That allows a user to choose whether to use Optic/custom-code while reading and whether
 * to write documents or process with custom code while writing.
 */
public class MarkLogicReadParams {
}

package com.marklogic.newtool.impl.importdata;

/**
 * Class exists solely to avoid Sonar warnings of "Provide the parametrized type for this generic" when command
 * classes use WriteDocumentParams directly. There's probably a better way to avoid that warning (without suppressing
 * it) but don't know it yet.
 */
public class WriteDocumentParamsImpl extends WriteDocumentParams<WriteDocumentParamsImpl> {
}

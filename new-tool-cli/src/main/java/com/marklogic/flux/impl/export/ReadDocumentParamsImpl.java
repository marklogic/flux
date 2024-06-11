package com.marklogic.flux.impl.export;

/**
 * Class exists solely to avoid Sonar warnings of "Provide the parametrized type for this generic" when command
 * classes use ReadDocumentParams directly. There's probably a better way to avoid that warning (without suppressing
 * it) but don't know it yet.
 */
public class ReadDocumentParamsImpl extends ReadDocumentParams<ReadDocumentParamsImpl> {
}

/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import marklogicspark.marklogic.client.io.DOMHandle;
import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BuildXmlTdeTest {

    private static final StructType SCHEMA = new StructType().add("myString", DataTypes.StringType);

    @Test
    void noNamespace() {
        final String expectedXml = """
            <template xmlns="http://marklogic.com/xdmp/tde">
              <rows>
                <row>
                  <schema-name>my-schema</schema-name>
                  <view-name>my-view</view-name>
                  <columns>
                    <column>
                      <name>myString</name>
                      <scalar-type>string</scalar-type>
                      <val>myString</val>
                    </column>
                  </columns>
                </row>
              </rows>
              <context>/my-root</context>
              <collections>
                <collection>customer</collection>
              </collections>
            </template>""";

        Document doc = buildTde(
            new TdeInputs("my-schema", "my-view", new SparkColumnIterator(SCHEMA))
                .withXmlRootName("my-root", null));

        Diff diff = DiffBuilder.compare(expectedXml)
            .withTest(doc)
            .ignoreWhitespace()
            .checkForIdentical()
            .build();

        assertFalse(diff.hasDifferences(), "XML does not match expected template:\n" + diff);
    }

    @Test
    void withNamespace() {
        final String expectedXml = """
            <template xmlns="http://marklogic.com/xdmp/tde">
              <rows>
                <row>
                  <schema-name>my-schema</schema-name>
                  <view-name>my-view</view-name>
                  <columns>
                    <column>
                      <name>myString</name>
                      <scalar-type>string</scalar-type>
                      <val>myString</val>
                    </column>
                  </columns>
                </row>
              </rows>
              <context>/ns1:my-root</context>
              <path-namespaces>
                <path-namespace>
                  <prefix>ns1</prefix>
                  <namespace-uri>org:example</namespace-uri>
                </path-namespace>
              </path-namespaces>
              <collections>
                <collection>customer</collection>
              </collections>
            </template>""";

        Document doc = buildTde(
            new TdeInputs("my-schema", "my-view", new SparkColumnIterator(SCHEMA))
                .withXmlRootName("my-root", "org:example"));

        Diff diff = DiffBuilder.compare(expectedXml)
            .withTest(doc)
            .ignoreWhitespace()
            .checkForIdentical()
            .build();

        assertFalse(diff.hasDifferences(), "XML does not match expected template:\n" + diff);
    }

    private Document buildTde(TdeInputs inputs) {
        AbstractWriteHandle handle = TdeBuilder.newTdeBuilder(inputs).buildTde(inputs).toWriteHandle();
        return ((DOMHandle) handle).get();
    }
}

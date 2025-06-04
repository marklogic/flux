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
                .withCollections("customer")
                .withDirectories()
                .withXmlRootName("my-root", null));

        verifyTemplate(expectedXml, doc);
    }

    @Test
    void withNamespaceAndRootNameAndViewLayout() {
        final String expectedXml = """
            <template xmlns="http://marklogic.com/xdmp/tde">
              <rows>
                <row>
                  <schema-name>my-schema</schema-name>
                  <view-name>my-view</view-name>
                  <view-layout>sparse</view-layout>
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
                <collection>another-collection</collection>
              </collections>
              <directories>
                <directory>/dir1/</directory>
                <directory>dir2/</directory>
              </directories>
            </template>""";

        Document doc = buildTde(
            new TdeInputs("my-schema", "my-view", new SparkColumnIterator(SCHEMA))
                .withCollections("customer", "another-collection")
                .withDirectories("/dir1/", "dir2/")
                .withViewLayout("sparse")
                .withXmlRootName("my-root", "org:example"));

        verifyTemplate(expectedXml, doc);
    }

    @Test
    void withCustomContextAndDisabled() {
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
              <context>/some-custom-context</context>
              <enabled>false</enabled>
            </template>""";

        Document doc = buildTde(
            new TdeInputs("my-schema", "my-view", new SparkColumnIterator(SCHEMA))
                .withContext("/some-custom-context")
                .withDisabled(true)
                .withXmlRootName("my-root", null));

        verifyTemplate(expectedXml, doc);

        doc = buildTde(
            new TdeInputs("my-schema", "my-view", new SparkColumnIterator(SCHEMA))
                .withXmlRootName("my-root", null)
                .withDisabled(true)
                .withContext("/some-custom-context"));

        verifyTemplate(expectedXml, doc);
    }

    private Document buildTde(TdeInputs inputs) {
        AbstractWriteHandle handle = TdeBuilder.newTdeBuilder(inputs).buildTde(inputs).toWriteHandle();
        return ((DOMHandle) handle).get();
    }

    private void verifyTemplate(String expectedXml, Document doc) {
        Diff diff = DiffBuilder.compare(expectedXml)
            .withTest(doc)
            .ignoreWhitespace()
            .checkForIdentical()
            .build();

        assertFalse(diff.hasDifferences(), "XML does not match expected template:\n" + diff);
    }
}

/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.marklogic.flux.impl.importdata.SparkColumnIterator;
import marklogicspark.marklogic.client.io.DOMHandle;
import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BuildXmlTdeTest extends AbstractTdeTest {

    private static final StructType SCHEMA = new StructType().add("myString", DataTypes.StringType);

    @Test
    void noNamespace() {
        final String expectedXml = """
            <template xmlns="http://marklogic.com/xdmp/tde">
              <rows>
                <row>
                  <schema-name>my_schema</schema-name>
                  <view-name>my_view</view-name>
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
            new TdeInputs("my_schema", "my_view")
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
                  <schema-name>my_schema</schema-name>
                  <view-name>my_view</view-name>
                  <view-layout>sparse</view-layout>
                  <columns>
                    <column>
                      <name>myString</name>
                      <scalar-type>string</scalar-type>
                      <val>ex:myString</val>
                    </column>
                  </columns>
                </row>
              </rows>
              <context>/ex:my-root</context>
              <path-namespaces>
                <path-namespace>
                  <prefix>ex</prefix>
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
            new TdeInputs("my_schema", "my_view")
                .withCollections("customer", "another-collection")
                .withDirectories("/dir1/", "dir2/")
                .withViewLayout("sparse")
                .withXmlRootName("my-root", "org:example", "ex"));

        verifyTemplate(expectedXml, doc);
    }

    @Test
    void withCustomContextAndDisabled() {
        final String expectedXml = """
            <template xmlns="http://marklogic.com/xdmp/tde">
              <rows>
                <row>
                  <schema-name>my_schema</schema-name>
                  <view-name>my_view</view-name>
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
            new TdeInputs("my_schema", "my_view")
                .withContext("/some-custom-context")
                .withDisabled(true)
                .withXmlRootName("my-root", null));

        verifyTemplate(expectedXml, doc);

        doc = buildTde(
            new TdeInputs("my_schema", "my_view")
                .withXmlRootName("my-root", null)
                .withDisabled(true)
                .withContext("/some-custom-context"));

        verifyTemplate(expectedXml, doc);
    }

    @Test
    void customizedColumn() {
        final String expectedXml = """
            <template xmlns="http://marklogic.com/xdmp/tde">
              <rows>
                <row>
                  <schema-name>my_schema</schema-name>
                  <view-name>my_view</view-name>
                  <columns>
                    <column>
                      <name>myString</name>
                      <scalar-type>int</scalar-type>
                      <val>myStringValue</val>
                      <nullable>true</nullable>
                      <default>0</default>
                      <invalid-values>reject</invalid-values>
                      <reindexing>visible</reindexing>
                      <permissions>
                        <role-name>rest-reader</role-name>
                        <role-name>rest-writer</role-name>
                      </permissions>
                      <collation>http://marklogic.com/collation/codepoint</collation>
                    </column>
                  </columns>
                </row>
              </rows>
              <context>/my-root</context>
            </template>""";

        Document doc = buildTde(
            new TdeInputs("my_schema", "my_view")
                .withXmlRootName("my-root", null)
                .withColumnVals(Map.of("myString", "myStringValue"))
                .withColumnTypes(Map.of("myString", "int"))
                .withNullableColumns(List.of("myString"))
                .withColumnDefaultValues(Map.of("myString", "0"))
                .withColumnInvalidValues(Map.of("myString", "reject"))
                .withColumnReindexing(Map.of("myString", "visible"))
                .withColumnPermissions(Map.of("myString", Set.of("rest-reader", "rest-writer")))
                .withColumnCollations(Map.of("myString", "http://marklogic.com/collation/codepoint"))
        );

        verifyTemplate(expectedXml, doc);
    }

    private Document buildTde(TdeInputs inputs) {
        TdeTemplate template = new XmlTdeBuilder().buildTde(inputs, new SparkColumnIterator(SCHEMA, inputs));
        assertEquals("/tde/%s/%s.xml".formatted(inputs.getSchemaName(), inputs.getViewName()), template.getUri());

        verifyTdeCanBeLoaded(template);

        AbstractWriteHandle handle = template.getWriteHandle();
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

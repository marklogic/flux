/*
 * Copyright © 2025 MarkLogic Corporation. All Rights Reserved.
 */
package com.marklogic.flux.tde;

import com.marklogic.flux.api.FluxException;
import com.marklogic.spark.dom.DOMHelper;
import marklogicspark.marklogic.client.io.DOMHandle;
import marklogicspark.marklogic.client.io.marker.AbstractWriteHandle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Iterator;
import java.util.Map;

public class XmlTdeBuilder implements TdeBuilder {

    private static final String NAMESPACE = "http://marklogic.com/xdmp/tde";

    @Override
    public TdeTemplate buildTde(TdeInputs tdeInputs) {
        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // default to best practices for conservative security including recommendations per
            // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            doc = factory.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new FluxException("Failed to create XML DocumentBuilder", e);
        }

        XmlTemplate xmlTemplate = new XmlTemplate(doc, tdeInputs.getSchemaName(), tdeInputs.getViewName(), tdeInputs.getViewLayout());
        xmlTemplate.setContext(tdeInputs.getContext(), tdeInputs.getNamespaces());
        xmlTemplate.setCollections(tdeInputs.getCollections());
        xmlTemplate.setDirectories(tdeInputs.getDirectories());
        Iterator<TdeInputs.Column> columns = tdeInputs.getColumns();
        while (columns.hasNext()) {
            xmlTemplate.addColumn(columns.next(), tdeInputs);
        }
        if (tdeInputs.isDisabled()) {
            xmlTemplate.setDisabled();
        }

        return new DOMTemplate(doc);
    }

    private static class XmlTemplate {

        private final Document doc;
        private final Element root;
        private final Element row;
        private final Element columns;

        XmlTemplate(Document doc, String schemaName, String viewName, String viewLayout) {
            this.doc = doc;
            this.root = doc.createElementNS(NAMESPACE, "template");
            doc.appendChild(root);

            Element rows = addChild(root, "rows");
            row = addChild(rows, "row");
            addChildWithText(row, "schema-name", schemaName);
            addChildWithText(row, "view-name", viewName);
            if (viewLayout != null && !viewLayout.isEmpty()) {
                addChildWithText(row, "view-layout", viewLayout);
            }
            columns = addChild(row, "columns");
        }

        void setContext(String context, Map<String, String> namespaces) {
            addChildWithText(root, "context", context);
            if (namespaces != null && !namespaces.isEmpty()) {
                Element pathNamespaces = addChild(root, "path-namespaces");
                for (Map.Entry<String, String> entry : namespaces.entrySet()) {
                    Element pathNamespace = addChild(pathNamespaces, "path-namespace");
                    addChildWithText(pathNamespace, "prefix", entry.getKey());
                    addChildWithText(pathNamespace, "namespace-uri", entry.getValue());
                }
            }
        }

        void addColumn(TdeInputs.Column column, TdeInputs inputs) {
            Element columnElement = addColumnWithRequiredFields(column, inputs);
            final String name = column.getName();
            
            if (inputs.getNullableColumns() != null && inputs.getNullableColumns().contains(name)) {
                addChildWithText(columnElement, "nullable", "true");
            }

            if (inputs.getColumnDefaultValues() != null && inputs.getColumnDefaultValues().containsKey(name)) {
                addChildWithText(columnElement, "default", inputs.getColumnDefaultValues().get(name));
            }

            if (inputs.getColumnInvalidValues() != null && inputs.getColumnInvalidValues().containsKey(name)) {
                addChildWithText(columnElement, "invalid-values", inputs.getColumnInvalidValues().get(name));
            }

            if (inputs.getColumnReindexing() != null && inputs.getColumnReindexing().containsKey(name)) {
                addChildWithText(columnElement, "reindexing", inputs.getColumnReindexing().get(name));
            }

            if (inputs.getColumnPermissions() != null && inputs.getColumnPermissions().containsKey(name)) {
                Element permissions = addChild(columnElement, "permissions");
                for (String roleName : inputs.getColumnPermissions().get(name).split(",")) {
                    addChildWithText(permissions, "role-name", roleName.trim());
                }
            }

            if (inputs.getColumnCollations() != null && inputs.getColumnCollations().containsKey(name)) {
                addChildWithText(columnElement, "collation", inputs.getColumnCollations().get(name));
            }
        }

        private Element addColumnWithRequiredFields(TdeInputs.Column column, TdeInputs inputs) {
            Element columnElement = addChild(columns, "column");
            final String name = column.getName();
            addChildWithText(columnElement, "name", name);

            final String scalarType = (inputs.getColumnTypes() != null && inputs.getColumnTypes().containsKey(name))
                ? inputs.getColumnTypes().get(name) : column.getScalarType();
            addChildWithText(columnElement, "scalar-type", scalarType);

            final String val = inputs.getColumnVals() != null && inputs.getColumnVals().containsKey(name) ?
                inputs.getColumnVals().get(name) : name;
            addChildWithText(columnElement, "val", val);
            return columnElement;
        }

        void setCollections(String[] collections) {
            if (collections != null && collections.length > 0) {
                Element wrapper = addChild(root, "collections");
                for (String collection : collections) {
                    addChildWithText(wrapper, "collection", collection);
                }
            }
        }

        void setDirectories(String[] directories) {
            if (directories != null && directories.length > 0) {
                Element wrapper = addChild(root, "directories");
                for (String directory : directories) {
                    addChildWithText(wrapper, "directory", directory);
                }
            }
        }

        void setDisabled() {
            addChildWithText(root, "enabled", "false");
        }

        private Element addChild(Element parent, String tagName) {
            return addChildWithText(parent, tagName, null);
        }

        private Element addChildWithText(Element parent, String tagName, String textContent) {
            Element child = doc.createElementNS(NAMESPACE, tagName);
            if (textContent != null) {
                child.setTextContent(textContent);
            }
            parent.appendChild(child);
            return child;
        }
    }

    private static class DOMTemplate implements TdeTemplate {
        private final Document tdeTemplate;

        public DOMTemplate(Document tdeTemplate) {
            this.tdeTemplate = tdeTemplate;
        }

        @Override
        public AbstractWriteHandle toWriteHandle() {
            return new DOMHandle(tdeTemplate);
        }

        @Override
        public String toPrettyString() {
            return DOMHelper.prettyPrintNode(tdeTemplate);
        }
    }
}

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
import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.Set;

public class XmlTdeBuilder implements TdeBuilder {

    private static final String NAMESPACE = "http://marklogic.com/xdmp/tde";

    private final DocumentBuilderFactory documentBuilderFactory;

    public XmlTdeBuilder() {
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            // default to best practices for conservative security including recommendations per
            // https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.md
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(false);
        } catch (Exception e) {
            throw new FluxException("Failed to create XML DocumentBuilderFactory", e);
        }
    }

    @Override
    public TdeTemplate buildTde(TdeInputs tdeInputs, Iterator<Column> columns) {
        Document doc;
        try {
            doc = this.documentBuilderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new FluxException("Failed to create XML DocumentBuilder", e);
        }

        XmlTemplate xmlTemplate = new XmlTemplate(doc, tdeInputs.getSchemaName(), tdeInputs.getViewName(), tdeInputs.getViewLayout());

        final boolean hasContextNamespaceWithPrefix = tdeInputs.hasContextNamespaceWithPrefix();
        if (hasContextNamespaceWithPrefix) {
            xmlTemplate.setContext(tdeInputs.getContext(), tdeInputs.getContextNamespaceUri(), tdeInputs.getContextNamespacePrefix());
        } else {
            xmlTemplate.setContext(tdeInputs.getContext(), null, null);
        }

        xmlTemplate.setCollections(tdeInputs.getCollections());
        xmlTemplate.setDirectories(tdeInputs.getDirectories());
        while (columns.hasNext()) {
            if (hasContextNamespaceWithPrefix) {
                xmlTemplate.addColumn(columns.next(), tdeInputs.getContextNamespacePrefix());
            } else {
                xmlTemplate.addColumn(columns.next(), null);
            }
        }
        if (tdeInputs.isDisabled()) {
            xmlTemplate.setDisabled();
        }

        return new DOMTemplate(doc, tdeInputs.getPermissions(), tdeInputs);
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

        void setContext(String context, String contextNamespaceUri, String contextNamespacePrefix) {
            addChildWithText(root, "context", context);
            if (contextNamespaceUri != null) {
                Element pathNamespaces = addChild(root, "path-namespaces");
                Element pathNamespace = addChild(pathNamespaces, "path-namespace");
                addChildWithText(pathNamespace, "prefix", contextNamespacePrefix);
                addChildWithText(pathNamespace, "namespace-uri", contextNamespaceUri);
            }
        }

        void addColumn(Column column, String contextNamespacePrefix) {
            Element columnElement = addChild(columns, "column");
            addChildWithText(columnElement, "name", column.getName());
            addChildWithText(columnElement, "scalar-type", column.getScalarType());

            String val = column.getVal();
            if (contextNamespacePrefix != null) {
                val = String.format("%s:%s", contextNamespacePrefix, val);
            }
            addChildWithText(columnElement, "val", val);

            if (column.isNullable()) {
                addChildWithText(columnElement, "nullable", "true");
            }

            String defaultValue = column.getDefaultValue();
            if (defaultValue != null) {
                addChildWithText(columnElement, "default", defaultValue);
            }

            String invalidValues = column.getInvalidValues();
            if (invalidValues != null) {
                addChildWithText(columnElement, "invalid-values", invalidValues);
            }

            String reindexing = column.getReindexing();
            if (reindexing != null) {
                addChildWithText(columnElement, "reindexing", reindexing);
            }

            Set<String> permissions = column.getPermissions();
            if (permissions != null) {
                Element permissionsElement = addChild(columnElement, "permissions");
                for (String roleName : permissions) {
                    addChildWithText(permissionsElement, "role-name", roleName.trim());
                }
            }

            String collation = column.getCollation();
            if (collation != null) {
                addChildWithText(columnElement, "collation", collation);
            }
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
        private final String permissions;
        private final String uri;

        public DOMTemplate(Document tdeTemplate, String permissions, TdeInputs inputs) {
            this.tdeTemplate = tdeTemplate;
            this.permissions = permissions;
            final String inputUri = inputs.getUri();
            this.uri = inputUri != null && !inputUri.isEmpty() ? inputUri :
                String.format("/tde/%s/%s.xml", inputs.getSchemaName(), inputs.getViewName());
        }

        @Override
        public AbstractWriteHandle getWriteHandle() {
            return new DOMHandle(tdeTemplate);
        }

        @Override
        public String toPrettyString() {
            return DOMHelper.prettyPrintNode(tdeTemplate);
        }

        @Override
        public String getUri() {
            return uri;
        }

        @Override
        public String getPermissions() {
            return permissions;
        }
    }
}

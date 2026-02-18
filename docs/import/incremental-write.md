---
layout: default
title: Incremental write
parent: Importing Data
nav_order: 9
---

Flux supports incremental writing, which skips writing a document to MarkLogic when the document's content has not
changed since it was last written. This can significantly reduce load on MarkLogic and speed up repeated import runs
when only a subset of your source data changes between runs.

Incremental write can be applied to every import command regardless of the data source.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## How it works

When incremental writing is enabled, Flux computes a hash of each document's content before writing it. On the first
write, the hash is stored as a metadata entry on the document in MarkLogic. On subsequent runs, Flux looks for an
existing document in MarkLogic with the same URI as the document it is about to write. If a matching document is found, 
Flux retrieves its stored hash, computes a new hash for the current version of the document, and skips the 
write if the two match.

Flux retrieves hashes for an entire batch of URIs in a single query. The batch size is controlled by the 
`--batch-size` option, so tuning that value also controls how many hash-retrieval queries Flux makes against 
MarkLogic. See [Tuning performance](tuning-performance.md) for more information on batch size.

Flux can retrieve stored hashes in two ways:

1. **Metadata field range index (default)** — Flux queries a MarkLogic
   [metadata field range index](https://docs.progress.com/bundle/marklogic-server-administrate-12/page/topics/fields-database-settings/configuring-fields/configuring-a-new-metadata-field.html)
   to retrieve hashes. The index must be configured on the metadata key used to store the hash value.
   Flux uses an [Optic `fromLexicons`](https://docs.marklogic.com/op.fromLexicons) query to perform this lookup.
2. **Schema and view** — Flux queries a MarkLogic view (typically created via a [TDE template](https://docs.progress.com/bundle/marklogic-server-develop-server-side-apps-12/page/topics/TDE.html)) to retrieve hashes.
   This approach requires no metadata field range index; instead, the view must expose the document URI and its
   associated hash value as columns.
   Flux uses an [Optic `fromView`](https://docs.marklogic.com/op.fromView) query to perform this lookup.

## Enabling incremental write

To enable incremental write on any import command, add the following option:

    --incremental-write

The incremental write feature depends on the existence of a field named `incrementalWriteHash` that also has a range index applied to it. 
You can [configure this manually](https://docs.progress.com/bundle/marklogic-server-administrate-12/page/topics/fields-database-settings/configuring-fields/configuring-a-new-metadata-field.html) 
via the MarkLogic Admin application. 

If you are using [ml-gradle](https://github.com/marklogic/ml-gradle) to deploy an application to MarkLogic, you can
instead configure this in your database configuration file. The following shows a minimal `content-database.json`
file with the field and field range index that incremental write requires:

```
{
  "database-name": "my-database",
  "field": [
    {
      "field-name": "incrementalWriteHash",
      "metadata": ""
    }
  ],
  "range-field-index": [
    {
      "scalar-type": "unsignedLong",
      "field-name": "incrementalWriteHash",
      "range-value-positions": false,
      "invalid-values": "reject"
    }
  ]
}
```

You can use the above as a starting point for your `content-database.json` file, adding any other database configuration your application requires. After deploying to MarkLogic, you will have the necessary indexes in place for incremental write to work properly.

By default, Flux will log a message on every 10,000 skipped documents. You can customize this interval
via `--log-skipped`. For example, the following will result in Flux logging a message on every 500 skipped 
documents.

    --log-skipped 500

## Using a schema and view

If you prefer not to create a metadata field range index, you can instead point Flux at a MarkLogic view — typically
defined via a [TDE template](https://docs.progress.com/bundle/marklogic-server-develop-server-side-apps-12/page/topics/TDE.html) —
using the `--incremental-write-schema` and `--incremental-write-view` options. Both must be specified together, 
with `--incremental-write-schema` defining the schema name and `--incremental-write-view` defining the view name.

The view must expose a column named `uri` containing document URIs and a column containing the stored hash values. The hash column
name must match the hash key name (see [Customizing the hash key name](#customizing-the-hash-key-name) below), 
which defaults to `incrementalWriteHash`.

The following shows a simple TDE template that would work with this feature:

```
{
  "template": {
    "description": "Example template for incremental write",
    "context": "/",
    "collections": ["example-data"],
    "rows": [
      {
        "schemaName": "example",
        "viewName": "incrementalWrite",
        "columns": [
          {
            "name": "uri",
            "scalarType": "string",
            "val": "xdmp:node-uri(.)"
          },
          {
            "name": "incrementalWriteHash",
            "scalarType": "unsignedLong",
            "val": "xdmp:node-metadata-value(., 'incrementalWriteHash')",
            "nullable": true
          }
        ]
      }
    ]
  }
}
```

The above TDE would then be referenced via the following options:

    --incremental-write-schema example 
    --incremental-write-view incrementalWrite


## Customizing the metadata key names

By default, Flux stores hash values in a document metadata field named `incrementalWriteHash`. You can override this with the
`--incremental-write-hash-name` option. When using a schema and view, this value must also match the name of the
column in the view that contains hash values. For example:

    --incremental-write-hash-name myCustomHashKey

To also store a write timestamp on each document, use `--incremental-write-timestamp-name` with the metadata key
name you want the timestamp stored under:

    --incremental-write-timestamp-name myWriteTimestamp

Documents that are skipped due to an unchanged hash will not have their timestamp updated.

## Controlling the hash calculation

Flux offers several options for controlling how a hash is calculated for a document. 

### Excluding content from the hash

You can instruct Flux to exclude portions of a document's content from the hash calculation. This is useful when
documents contain fields that change on every import (such as a last-modified timestamp populated at the source) but
should not be considered meaningful changes.

For JSON documents, use `--incremental-write-json-exclusion` with a
[JSON Pointer](https://www.rfc-editor.org/rfc/rfc6901) expression. For XML documents, use
`--incremental-write-xml-exclusion` with an XPath expression. Both options can be specified multiple times to exclude
multiple paths.

| Option | Description |
| --- | --- |
| `--incremental-write-json-exclusion` | JSON Pointer expression identifying content to exclude from the hash. Can be specified multiple times. |
| `--incremental-write-xml-exclusion` | XPath expression identifying content to exclude from the hash. Can be specified multiple times. |

Example for JSON:

    --incremental-write-json-exclusion "/lastModified" 
    --incremental-write-json-exclusion "/internalAudit/timestamp"

Example for XML:

    --incremental-write-xml-exclusion "/root/lastModified" 
    --incremental-write-xml-exclusion "/root/audit/timestamp"

If your XPath expressions reference namespaces, use `--xpath-namespace` to register each prefix and namespace URI:

    --xpath-namespace "ex=http://example.org/ns" 
    --incremental-write-xml-exclusion "/ex:root/ex:lastModified"

### JSON canonicalization

By default, Flux canonicalizes JSON documents before computing their hash. Canonicalization normalizes whitespace and
key ordering so that semantically identical JSON documents that differ only in formatting or key order are treated as
unchanged. 

To disable this behavior, include the `--incremental-write-disable-json-canonicalization` flag. You may wish to do this if you
know that the JSON documents you write to MarkLogic do not require any canonicalization, e.g. the order of keys in 
each document is guaranteed to be the same, and thus you can avoid the cost of canonicalizing each JSON document.


---
layout: default
title: Copying Data
nav_order: 6
---

Flux supports copying documents and their associated metadata from one database to another.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

Similar to the commands for [exporting documents](export/export-documents.md), the `copy` command requires that you 
specify the documents you wish to copy, along with connection information for the MarkLogic database you wish to read
from.

The following options control which documents are read from MarkLogic:

| Option | Description | 
| --- |--- |
| `--collections` | Comma-delimited sequence of collection names. |
| `--directory` | A database directory for constraining on URIs. |
| `--options` | Name of a REST API search options document; typically used with a string query. |
| `--query` | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| `--string-query` | A string query utilizing MarkLogic's search grammar. |
| `--uris` | Newline-delimited sequence of document URIs to retrieve. |

You must specify at least one of `--collections`, `--directory`, `--query`, `--string-query`, or `--uris`. You may specify any
combination of those options as well, with the exception that `--query` will be ignored if `--uris` is specified.

For examples of what the `--query` option support, please see 
[the MarkLogic search documentation](https://docs.marklogic.com/guide/rest-dev/search#id_49329).

The `copy` command then requires that you specify connection information for the output database that the documents
will be copied into. Each of the [connection options](common-options.md) can be used for this output database, but with
`output` as a prefix so that they are distinguished from the connections used for the source database. For example, 
`--output-connection-string` is used to specify a connection string for the output database. If you are copying the documents
to the same database that they were read from, you can omit output connection options.

The following shows an example of copying documents from a collection to a different database in the same MarkLogic 
cluster:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux copy \
  --connection-string "flux-example-user:password@localhost:8004" \
  --collections "example" \
  --output-connection-string "flux-example-user:password@localhost:8004" \
  --output-database "target-database"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux copy ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --collections "example" ^
  --output-connection-string "flux-example-user:password@localhost:8004" ^
  --output-database "target-database"
```
{% endtab %}
{% endtabs %}

## Controlling what metadata is read

The `--categories` option controls what metadata is read from the source database. The option defaults to a value of 
`content,metadata`, resulting in the document content and all metadata being read for each matching URI. 
In addition to `content` and `metadata`, valid choices include `collections`, `permissions`, `quality`, `properties`, 
and `metadatavalues`. Choices should be concatenated together into a comma-delimited string. For example, the 
following will only read documents and their collections and permissions:

    --categories content,collections,permissions

## Controlling how documents are written

The `copy` command supports many of the same options as the [import commands](import/common-import-features.md) for 
writing documents. Similar to the output connection options, each option for controlling how documents are written
is prefixed with `output`. For example, to specify collections for the documents, `--output-collections` is used instead
of `--collections`.

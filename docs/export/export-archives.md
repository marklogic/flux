---
layout: default
title: Exporting archives
parent: Exporting Data
nav_order: 4
---

Flux can export documents with their metadata as "archive files" - ZIP files that contain an entry for each document
and another entry for the XML metadata file associated with each document. Archive files can then be imported via 
the `import-archive-files` command, providing a convenient mechanism for storing data and later importing it into a separate
database.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `export-archive-files` command requires a query for selecting documents to export and a directory path for writing 
archive files to:

```
./bin/flux export-archive-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination
```

The following options control which documents are selected to be exported:

| Option | Description | 
| --- |--- |
| `--collections` | Comma-delimited sequence of collection names. |
| `--directory` | A database directory for constraining on URIs. |
| `--options` | Name of a REST API search options document; typically used with a string query. |
| `--query` | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| `--string-query` | A string query utilizing MarkLogic's search grammar. |
| `--uris` | Newline-delimited sequence of document URIs to retrieve.  |

You must specify at least one of `--collections`, `--directory`, `--query`, `--string-query`, or `--uris`. You may specify any
combination of those options as well, with the exception that `--query` will be ignored if `--uris` is specified.

You must then use the `--path` option to specify a directory to write archive files to.

### Windows-specific issues with zip files

In the likely event that you have one or more URIs with a forward slash - `/` - in them, then creating a zip file
with those URIs - which are used as the zip entry names - will produce confusing behavior on Windows. If you open the
zip file via Windows Explorer, Windows will erroneously think the zip file is empty. If you open the zip file using
7-Zip, you will see a top-level entry named `_` if one or more of your URIs begin with a forward slash. These are
effectively issues that only occur when viewing the file within Windows and do not reflect the actual contents of the
zip file. The contents of the file are correct and if you were to import them with Flux via the `import-archive-files`
command, you will get the expected results.


## Controlling document metadata

Each exported document will have all of its associated metadata - collections, permissions, quality, properties, and 
metadata values - included in an XML document in the archive zip file. You can control which types of metadata are
included with the `--categories` option. This option accepts a comma-delimited sequence of the following metadata types:

- `collections`
- `permissions`
- `quality`
- `properties`
- `metadatavalues`

If the option is not included, all metadata will be included. 

## Transforming document content

You can apply a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
to each document before it is written to an archive. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| `--transform` | Name of a MarkLogic REST transform to apply to the document before writing it. |
| `--transform-params` | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| `--transform-params-delimiter` | Delimiter for `--transform-params`; typically set when a value contains a comma. |

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
You can specify an alternate encoding when exporting archives via the `--encoding` option - e.g.:

```
./bin/flux export-archives \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination \
    --encoding ISO-8859-1
```

The encoding will be used for both document and metadata entries in each archive zip file. 

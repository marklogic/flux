---
layout: default
title: Exporting archives
parent: Exporting Data
nav_order: 4
---

NT can export documents with their metadata as "archive files" - ZIP files that contain an entry for each document
and another entry for the XML metadata file associated with each document. Archive files can then be imported via 
the `import_archives` command, providing a convenient mechanism for storing data and later importing it into a separate
database.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `export_archives` command requires a query for selecting documents to export and a directory path for writing 
archive files to. 

The following options then control which documents are selected to be exported:

| Option | Description | 
| --- |--- |
| --stringQuery | A string query utilizing MarkLogic's search grammar. |
| --query | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| --options | Name of a REST API search options document; typically used with a string query. |
| --collections | Comma-delimited sequence of collection names. |
| --directory | A database directory for constraining on URIs. |

You must specify at least one of `--stringQuery`, `--query`, `--collections`, or `--directory`. You may specify any
combination of those options as well.

You must then use the `--path` option to specify a directory to write archive files to.

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
| --transform | Name of a MarkLogic REST transform to apply to the document before writing it. |
| --transformParams | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| --transformParamsDelimiter | Delimiter for `--transformParams`; typically set when a value contains a comma. |

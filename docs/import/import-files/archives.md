---
layout: default
title: Importing archive files
parent: Importing files
grand_parent: Importing Data
nav_order: 7
---

NT can import archive files containing documents and their associated metadata. This includes archives written via 
the [`export_archive_files` command](../../export/export-archives.md) as well as archives written by 
[MarkLogic Content Pump](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/importing-content-into-marklogic-server/loading-content-and-metadata-from-an-archive.html), 
which are hereafter referred to as "MLCP archives".

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import_archive_files` command will import the documents and metadata files in a ZIP file produced by the 
`export_archive_files` command. You must specify at least one `--path` option along with connection information for the
MarkLogic database you wish to write to:

    ./bin/nt import_archive_files --path /path/to/files --connectionString "user:password@localhost:8000"

## Importing MLCP archives

You can also import 
[MLCP archives](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/exporting-content-from-marklogic-server/exporting-to-an-archive.html)
that were produced via the `EXPORT` command in MLCP. The `import_mlcp_archive_files` command is used instead, and it also
requires at least one `--path` option along with connection information for the MarkLogic database you wish to write to:

    ./bin/nt import_mlcp_archive_files --path /path/to/files --connectionString "user:password@localhost:8000"

## Restricting metadata

By default, all metadata associated with a document will be included when the document is written to MarkLogic. This is
true for both the `import_archive_files` command the `import_mlcp_archive_files` command. 

You can restrict which types of metadata are included via the `--categories` option. This option accepts a comma-delimited
sequence of the following metadata types:

- `collections`
- `permissions`
- `quality`
- `properties`
- `metadatavalues`

For example, the following option will only include the collections and properties found in each metadata entry in an 
archive ZIP file:

    --categories collections,properties

The `--categories` option can be used to restrict metadata for both `import_archive_files` and `import_mlcp_archive_files`.

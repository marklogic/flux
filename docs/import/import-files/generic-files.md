---
layout: default
title: Importing generic files
parent: Importing files
grand_parent: Importing Data
nav_order: 2
---

Flux can import any type of file as-is, with the contents of the file becoming a new document in MarkLogic. The term 
"generic files" is used in this context to refer to files that do not require any special processing
other than potentially decompressing the files.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-files` command is used to import a set of files into MarkLogic, with each file being written as a separate
document. You must specify at least one `--path` option along with connection information for the MarkLogic database
you wish to write to. For example:

    ./bin/flux import-files --path /path/to/files --connection-string user:password@localhost:8000

## Controlling document URIs

Each document will have an initial URI based on the absolute path of the associated file. See 
[common import features](../common-import-features.md) for details on adjusting this URI. In particular, the 
`--uri-replace` option is often useful for removing most of the absolute path to produce a concise, self-describing
URI. 

## Specifying a document type

The type of each document written to MarkLogic is determined by the file extension found in the URI along with the
set of [MIME types configured in MarkLogic](https://docs.marklogic.com/admin-help/mimetype). For unrecognized file
extensions, or URIs that do not have a file extension, you can force a document type via the `--document-type` option.
The value of this option must be one of `JSON`, `XML`, or `TEXT`.

## Specifying an encoding

MarkLogic stores all data [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your files use a different encoding, you must specify that via the `--encoding` option - e.g.:

    ./bin/flux import-files --path source --encoding ISO-8859-1 ...

## Importing Gzip files

To import Gzip files with each file being decompressed before written to MarkLogic, include the `--compression` option
with a value of `GZIP`. You can also import Gzip files as-is - i.e. without decompressing them - by not including the
`--compression` option.
 
## Importing ZIP files

To import each entry in a ZIP file as a separate document, include the `--compression` option with a value of `ZIP`.
Each document will have an initial URI based on both the absolute path of the ZIP file and the name of the ZIP entry. 
You can also use the `--document-type` option as described above to force a document type for any entry that has a file
extension not recognized by MarkLogic.

---
layout: default
title: Importing ORC files
parent: Importing files
grand_parent: Importing Data
nav_order: 9
---

Flux can import ORC files, with each row being written as a document in MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import_orc_files` command is used to read ORC files and write the contents of each file as one or more JSON
documents in MarkLogic. You must specify at least one `--path` option along with connection information for the
MarkLogic database you wish to write to:

    ./bin/flux import_orc_files --path /path/to/files --connectionString "user:password@localhost:8000"

## Specifying a JSON root name

By default, each column found in an ORC file will become a top-level field in the JSON document written to
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--jsonRootName` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

## Creating XML documents

To create XML documents for the rows in an ORC file instead of JSON documents, include the `--xmlRootName`
option to specify the name of the root element in each XML document. You can optionally include `--xmlNamespace` to
specify a namespace for the root element that will then be inherited by every child element as well.

## Advanced options

The `import_orc_files` command reuses Spark's support for reading ORC files. You can include any of
the [Spark ORC options](https://spark.apache.org/docs/latest/sql-data-sources-orc.html) via the `-P` dynamic option
to control how Avro content is read. Dynamic options are expressed as `-PoptionName=optionValue`.


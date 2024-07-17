---
layout: default
title: Importing ORC
parent: Importing files
grand_parent: Importing Data
nav_order: 8
---

Flux can import ORC files, with each row being written as a document in MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-orc-files` command reads ORC files and writes the contents of each file as one or more JSON
documents in MarkLogic. You must specify at least one `--path` option along with connection information for the
MarkLogic database you wish to write to:

    ./bin/flux import-orc-files --path /path/to/files --connection-string "user:password@localhost:8000"

## Specifying a JSON root name

By default, each column found in an ORC file will become a top-level field in the JSON document written to
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

## Creating XML documents

To create XML documents for the rows in an ORC file instead of JSON documents, include the `--xml-root-name`
option to specify the name of the root element in each XML document. You can optionally include `--xml-namespace` to
specify a namespace for the root element that will then be inherited by every child element as well.

## Aggregating rows

The `import-orc` command supports aggregating related rows together to produce hierarchical documents. See
[Aggregating rows](../aggregating-rows.md) for more information.

## Advanced options

The `import-orc-files` command reuses Spark's support for reading ORC files. You can include any of
the [Spark ORC options](https://spark.apache.org/docs/latest/sql-data-sources-orc.html) via the `-P` option
to control how Avro content is read. These options are expressed as `-PoptionName=optionValue`.


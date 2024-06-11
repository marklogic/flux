---
layout: default
title: Importing Parquet files
parent: Importing files
grand_parent: Importing Data
nav_order: 7
---

Flux can import Parquet files, with each row being written as a document in MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-parquet-files` command is used to read Parquet files and write the contents of each file as one or more JSON
documents in MarkLogic. You must specify at least one `--path` option along with connection information for the 
MarkLogic database you wish to write to:

    ./bin/flux import-parquet-files --path /path/to/files --connection-string "user:password@localhost:8000"

## Specifying a JSON root name

By default, each column found in a Parquet file will become a top-level field in the JSON document written to
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

## Creating XML documents

To create XML documents for the rows in a Parquet file instead of JSON documents, include the `--xml-root-name`
option to specify the name of the root element in each XML document. You can optionally include `--xml-namespace` to
specify a namespace for the root element that will then be inherited by every child element as well.

## Advanced options

The `import-parquet-files` command reuses Spark's support for reading Parquet files. You can include any of
the [Spark Parquet options](https://spark.apache.org/docs/latest/sql-data-sources-parquet.html) via the `-P` dynamic option
to control how Parquet content is read. Dynamic options are expressed as `-PoptionName=optionValue`.

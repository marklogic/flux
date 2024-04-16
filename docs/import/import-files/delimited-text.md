---
layout: default
title: Importing delimited text
parent: Importing files
grand_parent: Importing Data
nav_order: 3
---

NT can import rows from delimited text files, with each row being written as a document to MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import_delimited_files` command is used to read delimited text files. The command defaults to using a comma as
the delimiter for each row value. You must specify at least one `--path` option along with connection information 
for the MarkLogic database you wish to write to:

    ./bin/nt import_delimited_files --path /path/to/files --clientUri "user:password@localhost:8000"

## Specifying a JSON root name

By default, each column found in a delimited text file will become a top-level field in the JSON document written to 
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--jsonRootName` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

## Creating XML documents

To create XML documents for the rows in a delimited text file instead of JSON documents, include the `--xmlRootName`
option to specify the name of the root element in each XML document. You can optionally include `--xmlNamespace` to 
specify a namespace for the root element that will then be inherited by every child element as well.

## Advanced options

The `import_delimited_files` command reuses Spark's support for reading delimited text data. You can include any of
the [Spark CSV options](https://spark.apache.org/docs/latest/sql-data-sources-csv.html) via the `-P` dynamic option
to control how delimited text is read. Dynamic options are expressed as `-PoptionName=optionValue`.

The command defaults to setting the `header` option to `true` and the
`inferSchema` option to `true`. You can override those two options or include additional Spark CSV options - for
example:

    ./bin/nt import_delimited_files -Pheader=false -PescapeQuotes=false ....

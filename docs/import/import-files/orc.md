---
layout: default
title: Importing ORC
parent: Importing files
grand_parent: Importing Data
nav_order: 8
---

Flux can import ORC files, with each row being written as a JSON or XML document in MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-orc-files` command reads ORC files and writes the contents of each file as one or more
documents in MarkLogic. You must specify at least one `--path` option along with connection information for the
MarkLogic database you wish to write to:

```
./bin/flux import-orc-files \
    --path /path/to/files \
    --connection-string "user:password@localhost:8000"
```

The URI of each document will default to a UUID followed by `.json`. To include the file path at the start of the URI,
include the `--uri-include-file-path` option. You can also make use of the
[common import features](../common-import-features.md) for controlling document URIs.

## Specifying a JSON root name

By default, each column in an ORC file will become a top-level field in the JSON document written to
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

For example, including an option of `--json-root-name Customer` will produce JSON documents with the following format:

```
{
    "Customer": {
        "field1": "value1",
        etc...
    }
}
```

## Creating XML documents

To create XML documents for the rows in an ORC file instead of JSON documents, include the `--xml-root-name`
option to specify the name of the root element in each XML document. You can optionally include `--xml-namespace` to
specify a namespace for the root element that will then be inherited by every child element as well.

For example, including `--xml-root-name Customer --xml-namespace "org:example"` in the options will produce XML
documents with the following format:

```
<Customer xmlns="org:example">
    <field1>value1</field1>
    etc...
</Customer>
```

## Ignoring null fields

By default, Flux will include any fields in an ORC file that have a null value
when creating JSON or XML documents. You can instead ignore fields with a null value
via the `--ignore-null-fields` option:

```
./bin/flux import-orc-files \
    --path /path/to/files \
    --ignore-null-fields \
    --connection-string "user:password@localhost:8000" etc...
```

The decision on whether to include null fields will depend on your application requirements. For example, if your
documents have large numbers of null fields, you may find them to be noise and decide to ignore them. In another case,
it may be important to query for documents that have a particular field with a value of null.

## Aggregating rows

The `import-orc-files` command supports aggregating related rows together to produce hierarchical documents. See
[Aggregating rows](../aggregating-rows.md) for more information.

## Reading compressed files

Flux will automatically read files compressed with GZIP when they have a filename ending in `.gz`; you do not need to
specify a compression option. As noted in the "Advanced options" section below, you can use `-Pcompression=` to
explicitly specify a compression algorithm if Flux is not able to read your compressed files automatically.

## Advanced options

The `import-orc-files` command reuses Spark's support for reading ORC files. You can include any of
the [Spark ORC data source options](https://spark.apache.org/docs/latest/sql-data-sources-orc.html) via the `-P` option
to control how ORC content is read. These options are expressed as `-PoptionName=optionValue`.

For the configuration options listed in the above Spark ORC guide, use the `-C` option instead. For example, 
`-Cspark.sql.orc.filterPushdown=false` would configure Spark ORC to not push down filters.

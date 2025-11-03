---
layout: default
title: Importing Parquet
parent: Importing files
grand_parent: Importing Data
nav_order: 10
---

Flux can import Parquet files, with each row being written as a JSON or XML document in MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-parquet-files` command reads Parquet files and writes the contents of each file as one or more
documents in MarkLogic. You must specify at least one `--path` option along with connection information for the 
MarkLogic database you wish to write to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-parquet-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-parquet-files ^
    --path path\to\files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


The URI of each document will default to a UUID followed by `.json`. To include the file path at the start of the URI,
include the `--uri-include-file-path` option. You can also make use of the 
[common import features](../common-import-features.md) for controlling document URIs.

## Specifying a JSON root name

By default, each column in a Parquet file will become a top-level field in the JSON document written to
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

To create XML documents for the rows in a Parquet file instead of JSON documents, include the `--xml-root-name`
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

By default, Flux will include any fields in a Parquet file that have a null value
when creating JSON or XML documents. You can instead ignore fields with a null value
via the `--ignore-null-fields` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-parquet-files \
    --path /path/to/files \
    --ignore-null-fields \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-parquet-files ^
    --path path\to\files ^
    --ignore-null-fields ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


The decision on whether to include null fields will depend on your application requirements. For example, if your
documents have large numbers of null fields, you may find them to be noise and decide to ignore them. In another case,
it may be important to query for documents that have a particular field with a value of null.

## Aggregating rows

The `import-parquet-files` command supports aggregating related rows together to produce hierarchical documents. See
[Aggregating rows](../aggregating-rows.md) for more information.

## Generating a TDE template

The `import-parquet-files` command supports generating TDE templates to make imported data immediately available for relational
queries. See [TDE template generation](../tde-generation.md) for more information.

## Reading compressed files

Flux will automatically read files compressed with gzip when they have a filename ending in `.gz`; you do not need to
specify a compression option. As noted in the "Advanced options" section below, you can use `--spark-prop compression=` to
explicitly specify a compression algorithm if Flux is not able to read your compressed files automatically.

## Advanced options

The `import-parquet-files` command reuses Spark's support for reading Parquet files. You can include any of
the [Spark Parquet data source options](https://spark.apache.org/docs/3.5.6/sql-data-sources-parquet.html) via the `--spark-prop` option
to control how Parquet content is read. These options are expressed as `--spark-prop optionName=optionValue`.

For the configuration options listed in the above Spark Parquet guide, use the `--spark-conf` option instead. For example,
`--spark-conf spark.sql.parquet.filterPushdown=false` would configure Spark Parquet to not push down filters.

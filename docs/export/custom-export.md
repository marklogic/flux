---
layout: default
title: Exporting to custom targets
parent: Exporting Data
nav_order: 6
---

The `custom-export-rows` and `custom-export-documents` commands allow you to read rows and documents respectively from 
MarkLogic and write the results to a custom target.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

With the required `--target` option, you can specify
[any Spark data source](https://spark.apache.org/docs/latest/sql-data-sources.html) or the name of a thirdparty Spark
connector. For a thirdparty Spark connector, you must include the necessary JAR files for the connector in the
`./ext` directory of your Flux installation. Note that if the connector is not available as a single "uber" jar, you
will need to ensure that the connector and all of its dependencies are added to the `./ext` directory.

As an example, Flux does not provide an out-of-the-box command that uses the
[Spark Text data source](https://spark.apache.org/docs/latest/sql-data-sources-text.html). You can use this data source
via `custom-export-rows`:

```
./bin/flux custom-export-rows --target text \
  -Ppath=export \
  --connection-string user:password@localhost:8000 \
  --query "op.fromView('schema', 'view')" etc...
```

## Exporting rows

When using `custom-export-rows` with an Optic query to select rows from MarkLogic, each row sent to the connector or 
data source defined by `--target` will have a schema based on the output of the Optic query. You may find the 
`--preview` option helpful in understanding what data will be these rows. See [Common Options](../common-options.md) 
for more information.

## Exporting documents

When using `custom-export-documents`, each document returned by MarkLogic will be represented as a Spark row with 
the following column definitions:

1. `URI` containing a string. 
2. `content` containing a byte array.
3. `format` containing a string. 
4. `collections` containing an array of strings.
5. `permissions` containing a map of strings and arrays of strings representing roles and permissions. 
6. `quality` containing an integer.
7. `properties` containing an XML document serialized to a string.
8. `metadataValues` containing a map of string keys and string values.

These are normal Spark rows that can be written via Spark data sources like Parquet and ORC. If using a thirdparty 
Spark connector, you will likely need to understand how that connector will make use of rows defined via the above 
schema in order to get your desired results. 


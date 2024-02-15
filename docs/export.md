---
layout: default
title: Exporting Data
nav_order: 3
---

This will eventually document each of the commands for exporting data from MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Exporting documents

More to come, but some basics:

NT can export documents to files via the `export_files` command. Each document can be written to its own file, or many
documents can be written to a zip file. Document content can be transformed as well via a MarkLogic REST transform.

### Selecting documents

NT supports the following options for selecting documents to export:

| Option | Description                                                               | 
| --- |---------------------------------------------------------------------------|
| --stringQuery | A string query utilizing MarkLogic's search grammar.                      |
| --query | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| --options | Name of a REST API search options document; typically used with a string query. |
| --collections | Comma-delimited sequence of collection names.                             |
| --directory | A database directory for constraining on URIs.                            |

You must specify at least one of `--stringQuery`, `--query`, `--collections`, or `--directory`. You may specify any
combination of those options as well.

### Transforming document content

More to come, but the relevant options:

| Option | Description | 
| --- | --- |
| --transform | Name of a MarkLogic REST transform to apply to the document before writing it. |
| --transformParams | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| --transformParamsDelimiter | Delimiter for `--transformParams`; typically set when a value contains a comma. |

### Understanding partitions

As NT is built on top of Apache Spark, it is heavily influenced by how Spark 
[defines and manages partitions](https://sparkbyexamples.com/spark/spark-partitioning-understanding/). Within the 
context of NT, partitions can be thought of as "workers", with each worker operating in parallel on a different subset
of data. Generally, more partitions allow for more parallel work and thus improved performance.

When exporting documents to files, the number of partitions impacts how many files will be written. For example, run 
the following command below from the [Getting Started guide](getting-started.md):

```
rm export/*.zip
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip
```

The `./export` directory will have 12 zip files in it. This count is due to how NT reads data from MarkLogic,
which involves creating 4 partitions by default per forest in the MarkLogic database. The example application has 3 
forests in its content database, and thus 12 partitions are created, resulting in 12 separate zip files. 

You can use the `--partitionsPerForest` option to control how many partitions - and thus workers - read documents
from each forest in your database:

```
rm export/*.zip
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --partitionsPerForest 1
```

This approach will produce 3 zip files - one per forest. 

You can also use the `--repartition` option, available on every command, to force the number of partitions used when
writing data, regardless of how many were used to read the data:

```
rm export/*.zip
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --repartition 1
```

This approach will produce a single zip file due to the use of a single partition when writing files. 

## Exporting rows

NT can export rows selected via a MarkLogic Optic query and write them to a variety of destinations.

### JDBC

TODO `export_jdbc`, but basic thing is you must specify `--table` to define the table that you wish to write.

### Avro

`export_avro_files` with `-P` used to specify [Avro options](https://spark.apache.org/docs/latest/sql-data-sources-avro.html).

### ORC files

`export_orc_files` with `-P` used to specify [ORC options](https://spark.apache.org/docs/latest/sql-data-sources-orc.html).

### Parquet files

`export_parquet_files` with `-P` used to specify [Parquet options](https://spark.apache.org/docs/latest/sql-data-sources-parquet.html).


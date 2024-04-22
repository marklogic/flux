---
layout: default
title: Exporting documents
parent: Exporting Data
nav_order: 2
---

NT can export documents to files, with each document being written as a separate file and optionally compressed.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `export_files` command is used to select documents in a MarkLogic database and write them to a filesystem.
You must specify a `--path` option for where files should be written along with connection information for the
MarkLogic database you wish to write to:

    ./bin/nt export_files --path /path/to/files --connectionString "user:password@localhost:8000"

The following options then control which documents are selected to be exported:

| Option | Description | 
| --- |--- |
| --stringQuery | A string query utilizing MarkLogic's search grammar. |
| --query | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| --options | Name of a REST API search options document; typically used with a string query. |
| --collections | Comma-delimited sequence of collection names. |
| --directory | A database directory for constraining on URIs. |

You must specify at least one of `--stringQuery`, `--query`, `--collections`, or `--directory`. You may specify any
combination of those options as well.

## Transforming document content

You can apply a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
to each document before it is written to a file. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| --transform | Name of a MarkLogic REST transform to apply to the document before writing it. |
| --transformParams | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| --transformParamsDelimiter | Delimiter for `--transformParams`; typically set when a value contains a comma. |

## Compressing content

The `--compression` option is used to write files either to Gzip or ZIP files. 

To Gzip each file, include `--compression GZIP`. 

To write multiple files to one or more ZIP files, include `--compression ZIP`. A zip file will be created for each 
partition that was created when reading data via Optic. You can include `--repartition 1` to force all documents to be
written to a single ZIP file. See the next section on "Understanding partitions" for more information. 

## Understanding partitions

As NT is built on top of Apache Spark, it is heavily influenced by how Spark
[defines and manages partitions](https://sparkbyexamples.com/spark/spark-partitioning-understanding/). Within the
context of NT, partitions can be thought of as "workers", with each worker operating in parallel on a different subset
of data. Generally, more partitions allow for more parallel work and thus improved performance.

When exporting documents to files, the number of partitions impacts how many files will be written. For example, run
the following command below from the [Getting Started guide](getting-started.md):

```
rm export/*.zip
./bin/nt export_files --connectionString nt-user:password@localhost:8004 \
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
./bin/nt export_files --connectionString nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --partitionsPerForest 1
```

This approach will produce 3 zip files - one per forest.

You can also use the `--repartition` option, available on every command, to force the number of partitions used when
writing data, regardless of how many were used to read the data:

```
rm export/*.zip
./bin/nt export_files --connectionString nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --repartition 1
```

This approach will produce a single zip file due to the use of a single partition when writing files. 

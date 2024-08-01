---
layout: default
title: Exporting documents
parent: Exporting Data
nav_order: 2
---

Flux can export documents to files, with each document being written as a separate file and optionally compressed.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `export-files` command selects documents in a MarkLogic database and writes them to a filesystem.
You must specify a `--path` option for where files should be written along with connection information for the
MarkLogic database you wish to write to - for example:

```
./bin/flux export-files \
    --path /path/to/files \
    --connection-string "user:password@localhost:8000" etc..
```

The following options control which documents are selected to be exported:

| Option | Description | 
| --- |--- |
| `--collections` | Comma-delimited sequence of collection names. |
| `--directory` | A database directory for constraining on URIs. |
| `--options` | Name of a REST API search options document; typically used with a string query. |
| `--query` | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| `--string-query` | A string query utilizing MarkLogic's search grammar. |
| `--uris` | Newline-delimited sequence of document URIs to retrieve. |

You must specify at least one of `--collections`, `--directory`, `--query`, `--string-query`, or `--uris`. You may specify any
combination of those options as well, with the exception that `--query` will be ignored if `--uris` is specified.

## Transforming document content

You can apply a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
to each document before it is written to a file. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| `--transform` | Name of a MarkLogic REST transform to apply to each document before writing it to its destination. |
| `--transform-params` | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| `--transform-params-delimiter` | Delimiter for `--transform-params`; typically set when a value contains a comma. |

## Compressing content

The `--compression` option is used to write files either to Gzip or ZIP files. 

To Gzip each file, include `--compression GZIP`. 

To write multiple files to one or more ZIP files, include `--compression ZIP`. A zip file will be created for each 
partition that was created when reading data via Optic. You can include `--zip-file-count 1` to force all documents to be
written to a single ZIP file. See the below section on "Understanding partitions" for more information. 

### Windows-specific issues with zip files

In the likely event that you have one or more URIs with a forward slash - `/` - in them, then creating a zip file
with those URIs - which are used as the zip entry names - will produce confusing behavior on Windows. If you open the
zip file via Windows Explorer, Windows will erroneously think the zip file is empty. If you open the zip file using
7-Zip, you will see a top-level entry named `_` if one or more of your URIs begin with a forward slash. These are
effectively issues that only occur when viewing the file within Windows and do not reflect the actual contents of the
zip file. The contents of the file are correct and if you were to import them with Flux via the `import-files` 
command, you will get the expected results.

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
You can specify an alternate encoding when exporting documents to files via the `--encoding` option - e.g.:

```
./bin/flux export-files \
    --path destination \
    --encoding ISO-8859-1 \
    etc...
```

## Understanding partitions

As Flux is built on top of Apache Spark, it is heavily influenced by how Spark
[defines and manages partitions](https://sparkbyexamples.com/spark/spark-partitioning-understanding/). Within the
context of Flux, partitions can be thought of as "workers", with each worker operating in parallel on a different subset
of data. Generally, more partitions allow for more parallel work and improved performance.

When exporting documents to files, the number of partitions impacts how many files will be written. For example, run
the following command below from the [Getting Started guide](getting-started.md):

```
rm export/*.zip
./bin/flux export-files \
    --connection-string flux-example-user:password@localhost:8004 \
    --collections employee \
    --path export \
    --compression zip
```

The `./export` directory will have 12 zip files in it. This count is due to how Flux reads data from MarkLogic,
which involves creating 4 partitions by default per forest in the MarkLogic database. The example application has 3
forests in its content database, and thus 12 partitions are created, resulting in 12 separate zip files.

You can use the `--partitions-per-forest` option to control how many partitions - and thus workers - read documents
from each forest in your database:

```
rm export/*.zip
./bin/flux export-files \
    --connection-string flux-example-user:password@localhost:8004 \
    --collections employee \
    --path export \
    --compression zip \
    --partitions-per-forest 1
```

This approach will produce 3 zip files - one per forest.

You can also use the `--repartition` option, available on every command, to force the number of partitions used when
writing data, regardless of how many were used to read the data:

```
rm export/*.zip
./bin/flux export-files --connection-string flux-example-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --repartition 1
```

This approach will produce a single zip file due to the use of a single partition when writing files. 
The `--zip-file-count` option is effectively an alias for `--repartition`. Both options produce the same outcome. 
`--zip-file-count` is included as a more intuitive option for the common case of configuring how many files should
be written. 

Note that Spark's support for repartitioning may negatively impact overall performance due to the need to read all 
data from the data source first before writing any data. 

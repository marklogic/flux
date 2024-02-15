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

## Exporting to files

Discuss exporting to "regular" files, with zip/gzip/S3 support.

### Understanding partitions

As NT is built on top of Apache Spark, it is heavily influenced by how Spark 
[defines and manages partitions](https://sparkbyexamples.com/spark/spark-partitioning-understanding/). Within the 
context of NT, partitions can be thought of as "workers", with each worker operating in parallel on a different subset
of data. Generally, more partitions allow for more parallel work and thus improved performance.

When writing files, the number of partitions impacts how many files will be written. For example, run the following
command below from the [Getting Started guide](getting-started.md):

```
rm export/*.zip
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip
```

The `./export` directory will have 12 zip files in it. This count is due to how NT reads data from MarkLogic,
which involves creating 4 partitions per forest in the MarkLogic database. The example application has 3 forests in its
content database, and thus 12 partitions are created, resulting in 12 separate zip files. 

You can use the `--partitionsPerForest` argument to control how many partitions - and thus workers - read documents
from each forest in your database:

```
rm export/*.zip
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --partitionsPerForest 1
```

This approach will produce 3 zip files - one per forest. 

You can also use the `--repartition` argument, available on every command, to force the number of partitions used when
writing data, regardless of how many were used to read the data:

```
rm export/*.zip
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip \
    --repartition 1
```

This approach will produce a single zip file due to the use of a single partition when writing files. 

### Avro

TODO import_avro_files.

### ORC

TODO import_orc_files.

### Parquet

TODO import_parquet_files.

## Exporting to JDBC


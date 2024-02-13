---
layout: default
title: Common Features
nav_order: 6
---

This guide describes features common to every command in NT.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Using an options file

TODO.

## Previewing data

The `--preview` argument works on every command in NT. For example, given a set of Parquet files in a directory, 
you can preview an import without writing any of the data to MarkLogic:

```
./bin/nt import_parquet_files \
    --clientUri "nt-user:password@localhost:8004" \
    --path export/parquet \
    --preview 10
```

The number after `--preview` specifies how many records to show. You can use `--previewDrop` to specify potentially
verbose columns to drop from the preview. And you can use `--previewVertical` to see the results a vertical display
instead of in a table:

```
./bin/nt import_parquet_files \
    --clientUri "nt-user:password@localhost:8004" \
    --path export/parquet \
    --preview 10 \
    --previewDrop job_title,department
    --previewVertical
```

Note that in the case of previewing an import, NT will show the data as it has been read, which consists of a set of
Spark rows. The data is not shown as a set of documents yet, as the transformation of rows to documents occurs when
the data is written to MarkLogic.

## Applying a limit

TODO - `--limit` will limit the number of rows read. 

## Repartitioning

TODO - `--repartition`.

## Viewing a stacktrace

TODO - `--stacktrace`.

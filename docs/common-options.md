---
layout: default
title: Common Options
nav_order: 6
---

This guide describes options common to every command in NT.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Reading options from a file

NT supports reading options from a file, similar to MLCP. In a text file, put each option name and value on separate
lines:

```
-host
localhost
-port
8000
etc...
```

You then reference the file via the `@` symbol followed by a filename:

    ./bin/nt import_files @my-options.txt

You can reference multiple files this way and also include options on the command line too.

## Previewing data

The `--preview` option works on every command in NT. For example, given a set of Parquet files in a directory, 
you can preview an import without writing any of the data to MarkLogic:

```
./bin/nt import_parquet_files \
    --path export/parquet \
    --preview 10
```

For commands that read from a source other than MarkLogic, you are not required to specify any MarkLogic connection
information when including `--preview` since no connection needs to be made to MarkLogic.

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
Spark rows with columns. The data is not shown as a set of documents yet, as the transformation of rows to documents 
occurs when the data is written to MarkLogic.

## Applying a limit

For many use cases, it can be useful to only process a small subset of the source data to ensure that the results
are correct. The `--limit` option will achieve this by limiting the number of rows read by a command.

The following shows an example of only importing the first 10 rows from a delimited file:

```
./bin/nt import_delimited_files \
    --path ../data/employees.csv.gz \
    --clientUri "nt-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections employee \
    --uriTemplate "/employee/{id}.json" \
    --limit 10
```

## Repartitioning

TODO - `--repartition`.

## Viewing a stacktrace

When a command fails, NT will stop execution of the command and display an error message. If you wish to see the 
underlying stacktrace associated with the error, run the command with the `--stacktrace` option included. This is 
included primarily for debugging purposes, as the stacktrace may be fairly long with only a small portion of it 
potentially being helpful. The initial error message displayed by NT is intended to be as helpful as possible. 

---
layout: default
title: Handling errors
parent: Importing Data
nav_order: 10
---

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Write failures

Each import command results in batches of one or more documents being written to MarkLogic, with each batch sent in a 
separate HTTP call to MarkLogic. If Flux fails to write a batch of documents, it will attempt to write each document 
in the batch in smaller batches until it concludes it cannot write one or more specific documents. Flux will log an 
error for each failed document and will continue processing additional batches of documents. 

To force Flux to throw an error when it fails to write a batch of documents, include the `--abort-on-write-failure` 
option without a value. When a batch fails, Flux will log the error and cease processing.

## Retrying failed documents

When Flux is not using the `--abort-on-write-failure` option, you can capture failed documents with their metadata in a
ZIP archive file. To enable this, include the `--failed-documents-path` option with a file path for where you want 
archive files written containing failed documents. 

You can later use the `import-archive-files` command to retry these failed documents, presumably after making a fix to 
either the data or your application that will allow the documents to be successfully imported.

## Errors with Snappy compression

When importing files compressed [using Snappy](https://google.github.io/snappy/), such as Parquet files, 
you may run into the following error:

    failed to map segment from shared object when /tmp is mounted with noexec

Per the [Snappy documentation](https://www.javadoc.io/doc/org.xerial.snappy/snappy-java/1.1.7.1/org/xerial/snappy/SnappyLoader.html), 
this can be fixed by configuring Snappy to use a different temp directory when it compresses or decompresses a file - e.g. 

    export JAVA_OPTS='-Dorg.xerial.snappy.tempdir=/opt/tmp'
    ./bin/flux import-parquet-files etc...

---
layout: default
title: Tuning performance
parent: Importing Data
nav_order: 9
---

Flux provides several options for tuning performance when importing data. This guide covers the main settings that 
affect import performance, starting with the most commonly used options.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Write performance to MarkLogic

When writing to MarkLogic, the two main settings that affect performance (ignoring how the MarkLogic
cluster itself is configured, such as index settings and number of hosts) are the batch size - the number of documents
sent in a request to MarkLogic - and the number of threads used to send requests to MarkLogic.

### Batch size

Batch size is configured via the `--batch-size` option, which defaults to a value of 200. Depending on the size of
your documents, you may find improved performance by raising this value significantly for smaller documents, such as 500
or even 1000.

### Thread count

The `--thread-count` option determines how many total threads will be used across your entire import process to send
requests to MarkLogic. This is typically the only thread setting you need to configure.

For advanced use cases, you can alternatively configure a number of threads per partition using `--thread-count-per-partition`.
Flux is built on [Apache Spark](https://spark.apache.org), which divides work into partitions based on the data source being read.
Flux will log the number of partitions as shown below:

    INFO com.marklogic.spark: Number of partitions: 1

In most cases, using `--thread-count` is more intuitive as you can simply specify the total number of threads you want
to use (e.g., "I want 96 total threads"), rather than having to calculate threads per partition.

### Thread count and cluster size

**You should take care** not to exceed the number of requests that your MarkLogic cluster can reasonably handle at a
given time. A general rule of thumb is not to use more threads than the number of hosts multiplied by the number of
threads per app server. A MarkLogic app server defaults to a limit of 32 threads. So for a 3-host cluster, you should
not exceed 96 total threads. This also assumes that each host is receiving requests, which can be accomplished
by placing a load balancer in front of MarkLogic or by configuring direct connections as described below.

The rule of thumb can thus be expressed as:

    Value of --thread-count <= Number of hosts * number of app server threads

### Direct connections to each host

In a scenario where Flux can connect directly to each host in your MarkLogic cluster without a load balancer being 
present, you can set the `--connection-type` option to a value of `direct`. Flux will then effectively act as a load 
balancer by distributing work across each host in the cluster.

## Document pipeline performance

When importing JSON or XML documents, Flux can optionally process each document through a series of steps known as the 
"document pipeline". This pipeline can include [text extraction](import-files/generic-files.md), 
[text splitting](splitting.md), [text classification](classifier.md), and [embedding generation](embedder/embedder.md). 

The performance of this pipeline can be controlled by the `--pipeline-batch-size` option. This option determines how many documents are collected in a batch before being sent through the document pipeline.

**Note**: In Flux 1.3.0, pipeline batch size affects embedding generation in all cases, but only affects text classification when it is processing "chunks" - the output of the text splitter feature. This limitation for text classification may be addressed in future releases.

By default, pipeline batch size is not set, meaning each document is processed individually through the pipeline. 
Depending on the size of your documents, you may find improved performance by setting this to a higher value, such as 50 or 100.
This will control how many documents are sent to the embedding model in a single request, and when using text classification with text splitting, how many chunks are sent to the text classifier in a single request. 

Pipeline performance is also impacted by the number of "partitions" used by Flux. Flux is built on  
[Apache Spark](https://spark.apache.org), which uses partitions to divide work - i.e. reading, processing, and writing data.
The number of partitions created depends on the data source that Flux reads from. You can see the number of partitions
in the Flux logs:

    INFO com.marklogic.spark: Number of partitions: 1

Each partition has its own document pipeline. Thus, if Flux creates 5 partitions and you configure `--pipeline-batch-size`
with a value of 10, up to 50 documents could be processed in parallel. 

For more information on partitions, including how to override the number of partitions, please see the
[Advanced Spark Options in the Common Options guide](../common-options.md). 

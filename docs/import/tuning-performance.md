---
layout: default
title: Tuning performance
parent: Importing Data
nav_order: 4
---

When writing to MarkLogic, the two main settings within Flux that affect performance (i.e. ignoring how the MarkLogic
cluster itself is configured, such as index settings and number of hosts) are the batch size - the number of documents
sent in a request to MarkLogic - and the number of threads used to send requests to MarkLogic.

Batch size is configured via the `--batchSize` option, which defaults to a value of 200. Depending on the size of
your documents, you may find improved performance by raising this value significantly for smaller documents, such as 500
or even 1000.

For the number of threads used to send requests to MarkLogic, two factors come into play. The product of the
number of partitions and the value of the `--threadCount` option determines how many total threads will be used to send
requests. For example, if the import command uses 4 partitions to read data and `--threadCount` is set to 4 (its
default value), 16 total threads will send requests to MarkLogic.

The number of partitions is determined by how data is read and differs across the various import commands.
Flux will log the number of partitions for each import command as shown below:

    INFO com.marklogic.spark: Number of partitions: 1

You can force a number of partitions via the `--repartition` option.

### Thread count and cluster size

**You should take care** not to exceed the number of requests that your MarkLogic cluster can reasonably handle at a
given time. A general rule of thumb is not to use more threads than the number of hosts multiplied by the number of
threads per app server. A MarkLogic app server defaults to a limit of 32 threads. So for a 3-host cluster, you should
not exceed 96 total threads. This also assumes that each host is receiving requests. This would be accomplished either
by placing a load balancer in front of MarkLogic or by configuring direct connections as described below.

The rule of thumb can thus be expressed as:

    Number of partitions * Value of --threadCount <= Number of hosts * number of app server threads

### Direct connections to each host

In a scenario where Flux can connect directly to each host in your MarkLogic cluster without a load balancer being 
present, you can set the `--connectionType` option to a value of `direct`. Flux will then effectively act as a load 
balancer by distributing work across each host in the cluster. 

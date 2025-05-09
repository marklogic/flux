---
layout: default
title: Tuning performance
parent: Importing Data
nav_order: 9
---

When writing to MarkLogic, the two main settings within Flux that affect performance (i.e. ignoring how the MarkLogic
cluster itself is configured, such as index settings and number of hosts) are the batch size - the number of documents
sent in a request to MarkLogic - and the number of threads used to send requests to MarkLogic.

Batch size is configured via the `--batch-size` option, which defaults to a value of 200. Depending on the size of
your documents, you may find improved performance by raising this value significantly for smaller documents, such as 500
or even 1000.

For the number of threads used to send requests to MarkLogic, two factors come into play.
The value of the `--thread-count` option determines how many total threads will be used across all partitions to send
requests. You can alternatively configure a number of threads per partition using `--thread-count-per-partition`.

The number of partitions is determined by how data is read and differs across the various import commands.
Flux will log the number of partitions for each import command as shown below:

    INFO com.marklogic.spark: Number of partitions: 1

Because each partition writer in an import process has its own pool of threads (independent from the number of 
Spark worker threads), you typically will not need to worry about the number of partitions for an import process. 
Setting `--thread-count` gives you the control you will typically need to ensure that Flux is able to use a sufficient 
number of threads for sending requests to MarkLogic.

### Thread count and cluster size

**You should take care** not to exceed the number of requests that your MarkLogic cluster can reasonably handle at a
given time. A general rule of thumb is not to use more threads than the number of hosts multiplied by the number of
threads per app server. A MarkLogic app server defaults to a limit of 32 threads. So for a 3-host cluster, you should
not exceed 96 total threads. This also assumes that each host is receiving requests. This would be accomplished either
by placing a load balancer in front of MarkLogic or by configuring direct connections as described below.

The rule of thumb can thus be expressed as:

    Value of --thread-count <= Number of hosts * number of app server threads

### Direct connections to each host

In a scenario where Flux can connect directly to each host in your MarkLogic cluster without a load balancer being 
present, you can set the `--connection-type` option to a value of `direct`. Flux will then effectively act as a load 
balancer by distributing work across each host in the cluster. 

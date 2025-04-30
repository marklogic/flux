---
layout: default
title: Overview
nav_order: 1
---

MarkLogic Flux is a single extensible application for all of your data movement use cases with MarkLogic.
Flux supports importing, exporting, copying, and reprocessing data via a simple command-line interface.
Flux can also be easily embedded in your own application to support any flow of data to and from MarkLogic.

With Flux, you can automate common data movement use cases including:

- Importing rows from an RDBMS.
- Importing JSON, XML, CSV, Parquet and other file types from a local filesystem or S3.
- Extract text from binary documents and classify it using [Progress Semaphore](https://www.progress.com/semaphore).
- Implementing a data pipeline for a [RAG solution with MarkLogic](https://www.progress.com/marklogic/solutions/generative-ai).
- Copying data from one MarkLogic database to another database.
- Reprocessing data in MarkLogic via custom code.
- Exporting data to an RDBMS, a local filesystem, or S3.

Flux has the following system requirements:

* Java 11 or Java 17.
* MarkLogic 10.0-9 or higher.
* A [MarkLogic REST API app server](https://docs.marklogic.com/guide/rest-dev) to connect to. 

Java 21 should work but has not been thoroughly tested yet. Java 23 will not yet work. 

Earlier versions of MarkLogic 9 and 10 will support any features not involving Optic queries.
Additionally, the latest version of MarkLogic 11 or 12 is recommended if possible.

Flux is built on top of [Apache Spark](https://spark.apache.org/), but you do not need to know anything about Spark
to use Flux. If you are already making use of Spark for other use cases, see the 
[Spark Integration guide](spark-integration.md) for information on using Flux with your existing Spark cluster.

You can download the latest release of the Flux application zip from [the releases page](https://github.com/marklogic/flux/releases).

Please see the [Getting Started guide](getting-started.md) to begin using Flux.  

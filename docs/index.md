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
- Copying data from one MarkLogic database to another database.
- Reprocessing data in MarkLogic via custom code.
- Exporting data to an RDBMS, a local filesystem, or S3.

Flux has the following system requirements:

* Java 11 or higher.
* MarkLogic 10.0-9 or higher.
* A [MarkLogic REST API app server](https://docs.marklogic.com/guide/rest-dev) to connect to. 

Earlier versions of MarkLogic 9 and 10 will support any features not involving Optic queries.
Additionally, the latest version of MarkLogic 11 is recommended if possible.

Flux is built on top of [Apache Spark](https://spark.apache.org/), but you do not need to know anything about Spark
to use Flux. If you are already making use of Spark for other use cases, see the 
[Spark Integration guide](spark-integration.md) for information on using Flux with your existing Spark cluster.

Please see the [Getting Started guide](getting-started.md) to begin using Flux.  

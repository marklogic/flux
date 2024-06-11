---
layout: default
title: Introduction
nav_order: 1
---

The MarkLogic Flux application is a single solution
for all of your data movement use cases involving MarkLogic. Flux leverages the
[MarkLogic Spark connector](https://github.com/marklogic/marklogic-spark-connector) and
[Apache Spark](https://spark.apache.org/) to provide a scalable, flexible, and extensible solution for importing,
reprocessing, copying, and exporting your data with MarkLogic. Flux provides many of the benefits and best practices
supported by [MarkLogic Content Pump](https://developer.marklogic.com/products/mlcp/) and
[CoRB 2](https://developer.marklogic.com/code/corb/), while supporting a wide variety of data sources vis a vis
Spark and its ecosystem.

With Flux, you can automate common data movement use cases including:

- Import rows from an RDBMS.
- Import JSON, XML, CSV, Parquet and other file types from a local filesystem or S3.
- Copy data from one MarkLogic database to another database.
- Reprocess data in MarkLogic via custom code.
- Export data to an RDBMS, a local filesystem, or S3.

You also soon be able to extend Flux with your own commands, along with embedding Flux as a dependency in your own application.

Flux has the following system requirements:

* Java 11 or higher.
* MarkLogic 10.0-9 or higher.
* A [MarkLogic REST API app server](https://docs.marklogic.com/guide/rest-dev) to connect to. 

Earlier versions of MarkLogic 9 and 10 will support any features not involving Optic queries.
Additionally, the latest version of MarkLogic 11 is recommended if possible.

Please see the [Getting Started guide](getting-started.md) to begin using Flux.  

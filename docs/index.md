---
layout: default
title: Introduction
nav_order: 1
---

The MarkLogic Spark ETL tool (currently known as "NT" ("new tool") until a real name is branded) is a single application
for all of your data movement use cases involving MarkLogic. NT leverages the 
[MarkLogic Spark connector](https://github.com/marklogic/marklogic-spark-connector) and
[Apache Spark](https://spark.apache.org/) to provide a scalable, flexible, and extensible solution for importing, 
reprocessing, copying, and exporting your data with MarkLogic. NT provides many of the benefits and best practices 
supported by [MarkLogic Content Pump](https://developer.marklogic.com/products/mlcp/) and 
[CoRB 2](https://developer.marklogic.com/code/corb/), while supporting a wide variety of data sources vis a vis 
Spark and its ecosystem. 

With NT, you can automate common data movement use cases including:

- Import rows from an RDBMS.
- Import JSON, XML, CSV, Parquet and other file types from a local filesystem or S3.
- Copy data from one MarkLogic database to another database.
- Reprocess data in MarkLogic via custom code.
- Export data to an RDBMS, a local filesystem, or S3.

NT has the following system requirements:

* Java 11 or higher.
* MarkLogic 10.0-9 or higher.

Earlier versions of MarkLogic 9 and 10 will support any features not involving Optic queries.
Additionally, the latest version of MarkLogic 11 is recommended if possible.

Please see the [Getting Started guide](getting-started.md) to begin using NT.  

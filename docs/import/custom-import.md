---
layout: default
title: Importing from custom sources
parent: Importing Data
nav_order: 4
---

The `custom-import` command allows you to read data from any Spark-supported data source and write 
JSON or XML documents to MarkLogic. 

With the required `--source` option, you can specify 
[any Spark data source](https://spark.apache.org/docs/latest/sql-data-sources.html) or the name of a third-party Spark
connector. For a third-party Spark connector, you must include the necessary JAR files for the connector in the 
`./ext` directory of your Flux installation. Note that if the connector is not available as a single "uber" jar, you 
will need to ensure that the connector and all of its dependencies are added to the `./ext` directory.

As an example, Flux does not provide an out-of-the-box command that uses the 
[Spark Text data source](https://spark.apache.org/docs/latest/sql-data-sources-text.html). You can use this data source
via `custom-import`:

```
./bin/flux custom-import --source text \
  -Ppath=/path/to/text/files \
  --connection-string user:password@localhost:8000 etc..
```

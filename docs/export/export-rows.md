---
layout: default
title: Exporting rows
parent: Exporting Data
nav_order: 3
---

NT can export rows retrieved via the [MarkLogic Optic API](https://docs.marklogic.com/guide/app-dev/OpticAPI), writing
them either to files or to another database via JDBC.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Querying for rows

The following commands all support executing an Optic query and exporting the matching rows to an external data source:

- `export-avro-files`
- `export-jdbc`
- `export-orc-files`
- `export-parquet-files`

An Optic query is specified via the `--query` option. The query must be defined using the 
[Optic DSL](https://docs.marklogic.com/guide/app-dev/OpticAPI#id_46710) and must being with `op.fromView`. The 
[MarkLogic Spark connector documentation](https://marklogic.github.io/marklogic-spark-connector/reading-data/optic.html#optic-query-requirements)
provides additional guidance on how to write an Optic query. 

You must also specify connection information for the MarkLogic database you wish to query. Please see the 
[guide on common options](../common-options.md) for instructions on doing so.

The `--batchSize` and `--partitions` query are used to tune performance by controlling how many rows are retrieved in
a single call to MarkLogic and how many requests are made in parallel. It is recommended to first test your command
without setting these options to see if the performance is acceptable. When you are ready to attempt to optimize the 
performance of your export command, please see the 
[this guide on Optic query performance](https://marklogic.github.io/marklogic-spark-connector/reading-data/optic.html#tuning-performance).

## Exporting to JDBC

The `export-jdbc` command writes rows retrieved by an Optic query to a table in an external database via a JDBC 
driver specific to the database. 

### JDBC driver installation

To export rows to a database, you must obtain the database's JDBC driver JAR file and add it to the `./ext` directory
location in the NT installation directory. Any JAR file placed in the `./ext` directory is added to the classpath of
NT.

### Configuring a JDBC connection

The `export-jdbc` command requires that you specify connection details for the database you wish to write to via JDBC.
Connection details are specified via the following options:

- `--jdbcUrl` is required and specifies the JDBC connection URL.
- `--jdbcDriver` is required specifies the main class name of the JDBC driver.
- `--jdbcUser` specifies an optional user to authenticate as (this may already be specified via `--jdbcUrl`).
- `--jdbcPassword` specifies an optional password to authenticate with (this may already be specified via `--jdbcUrl`).

### Exporting to a table

Once you have installed your database's JDBC driver and determined your JDBC connection details, you can use 
`export-jdbc` to export all rows matching an Optic query to a table in the external database. The following shows 
a notional example of doing so:

```
./bin/nt export-jdbc --connectionString user:password@localhost:8000 \
  --query "op.fromView('example', 'employee', '')" \
  --jdbcUrl "jdbc:postgresql://localhost/example?user=postgres&password=postgres" \
  --jdbcDriver "org.postgresql.Driver" \
  --table "marklogic-employee-data"
```

## Exporting to files

Rows selected via an Optic query can be exported to either Avro, ORC, or Parquet files. 

### Avro

The `export-avro-files` command writes one or more Avro files to the directory specified by the `--path` option. This
command reuses Spark's support for writing Avro files. You can include any of the 
[Spark Avro options](https://spark.apache.org/docs/latest/sql-data-sources-avro.html) via the `-P` dynamic option to
control how Avro content is written. Dynamic options are expressed as `-PoptionName=optionValue`.

### ORC

The `export-orc-files` command writes one or more ORC files to the directory specified by the `--path` option. This
command reuses Spark's support for writing ORC files. You can include any of the
[Spark ORC options](https://spark.apache.org/docs/latest/sql-data-sources-orc.html) via the `-P` dynamic option to
control how ORC content is written. Dynamic options are expressed as `-PoptionName=optionValue`.

### Parquet

The `export-parquet-files` command writes one or more Parquet files to the directory specified by the `--path` option. This
command reuses Spark's support for writing Parquet files. You can include any of the
[Spark Parquet options](https://spark.apache.org/docs/latest/sql-data-sources-parquet.html) via the `-P` dynamic option to
control how Parquet content is written. Dynamic options are expressed as `-PoptionName=optionValue`.

## Controlling the save mode

Each of the commands for exporting rows to files supports a `--mode` option that controls how data is written to a 
location where data already exists. This option supports the following values:

- `Append` = append data if the destination already exists.
- `Overwrite` = replace data if the destination already exists. 
- `ErrorIfExists` = throw an error if the destination already exists.
- `Ignore` = do not write any data if the destination already exists. 

For convenience, the above values are case-sensitive so that you can ignore casing when choosing a value. 

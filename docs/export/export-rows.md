---
layout: default
title: Exporting rows
parent: Exporting Data
nav_order: 3
---

Flux can export rows retrieved via the [MarkLogic Optic API](https://docs.marklogic.com/guide/app-dev/OpticAPI), writing
them either to files or to another database via JDBC.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Querying for rows

The following commands support executing an Optic query and exporting the matching rows to an external data source:

- `export-avro-files`
- `export-delimited-files`
- `export-jdbc`
- `export-json-lines-files`
- `export-orc-files`
- `export-parquet-files`

An Optic query is specified via the `--query` option. 

Starting with Flux 1.2.0, the Optic query can use any
[data access function](https://docs.marklogic.com/guide/app-dev/OpticAPI#id_66011) with a caveat - only Optic
queries that use `op.fromView` can be partitioned into multiple calls to MarkLogic. If you use a data access function
other than `op.fromView`, you should ensure that your query can return its results in a single call to MarkLogic. 

If you are using Flux 1.1.x or earlier, your Optic query must use the `op.fromView` data access function. 

Please see the  
[MarkLogic Spark connector documentation](https://marklogic.github.io/marklogic-spark-connector/reading-data/optic.html#optic-query-requirements)
for additional guidance on how to write an Optic query. 

You must also specify connection information for the MarkLogic database you wish to query. Please see the 
[guide on common options](../common-options.md) for instructions on doing so.

## Exporting to JDBC

The `export-jdbc` command writes rows retrieved by an Optic query to a table in an external database via a JDBC 
driver specific to the database. 

### JDBC driver installation

To export rows to a database, you must obtain the database's JDBC driver JAR file and add it to the `./ext` directory
location in the Flux installation directory. Any JAR file placed in the `./ext` directory is added to the classpath of
Flux.

### Configuring a JDBC connection

The `export-jdbc` command requires that you specify connection details for the database you wish to write to via JDBC.
Connection details are specified via the following options:

- `--jdbc-url` is required and specifies the JDBC connection URL.
- `--jdbc-driver` is required specifies the main class name of the JDBC driver.
- `--jdbc-user` specifies an optional user to authenticate as (this may already be specified via `--jdbc-url`).
- `--jdbc-password` specifies an optional password to authenticate with (this may already be specified via `--jdbc-url`).

### Exporting to a table

Once you have installed your database's JDBC driver and determined your JDBC connection details, you can use 
`export-jdbc` to export all rows matching an Optic query to a table in the external database. The following shows 
a notional example of doing so:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-jdbc \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('example', 'employee', '')" \
    --jdbc-url "jdbc:postgresql://localhost/example?user=postgres&password=postgres" \
    --jdbc-driver "org.postgresql.Driver" \
    --table "marklogic-employee-data"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-jdbc ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --query "op.fromView('example', 'employee', '')" ^
    --jdbc-url "jdbc:postgresql://localhost/example?user=postgres&password=postgres" ^
    --jdbc-driver "org.postgresql.Driver" ^
    --table "marklogic-employee-data"
```
{% endtab %}
{% endtabs %}

## Exporting to files

Rows selected via an Optic query can be exported to any of the below file formats.

**Exporting rows to files defaults in Flux 1.0 to overwriting data in the selected path**. 
The "Controlling save mode" section belows describes the different options for controlling this behavior. A future 
release of Flux will change the default from "overwrite" to "append" to avoid accidentally deleting any data in an
existing file path.

### Avro

The `export-avro-files` command writes one or more Avro files to the directory specified by the `--path` option. This
command reuses Spark's support for writing Avro files:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-avro-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-avro-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections example ^
    --path destination
```
{% endtab %}
{% endtabs %}

You can include any of the 
[Spark Avro data source options](https://spark.apache.org/docs/3.5.6/sql-data-sources-avro.html) via the `-P` option to
control how Avro content is written. These options are expressed as `-PoptionName=optionValue`.

For configuration options listed in the above Spark Avro guide, use the `--spark-conf` option instead. For example,
`--spark-conf spark.sql.avro.compression.codec=deflate` would change the type of compression used for writing Avro files.

### Delimited text

The `export-delimited-files` command writes one or more delimited text (commonly CSV) files to the directory 
specified by the `--path` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-delimited-files \
  --connection-string "flux-example-user:password@localhost:8004" \
  --query "op.fromView('example', 'employee', '')" \
  --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-delimited-files ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --query "op.fromView('example', 'employee', '')" ^
  --path destination
```
{% endtab %}
{% endtabs %}

This command reuses Spark's support for writing delimited text files. You can include
any of the [Spark CSV options](https://spark.apache.org/docs/3.5.6/sql-data-sources-csv.html) via the `-P` 
option to control how delimited text is written. These options are expressed as `-PoptionName=optionValue`. 

The command defaults to setting the Spark CSV `header` option to `true` so that column
names from your Optic query for selecting rows from MarkLogic are included in each output file. You can override this
via `-Pheader=false` if desired.

The command also defaults to setting the Spark CSV `inferSchema` option to `true`. This results in Flux, via Spark CSV,
attempting to determine a type for each column in the delimited text file. To disable this behavior, resulting in 
every column having a type of `string`, include `-PinferSchema=false`. 

By default, each file will be written using the UTF-8 encoding. You can specify an alternate encoding via the 
`--encoding` option - e.g. 

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-delimited-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('example', 'employee', '')" \
    --path destination \
    --encoding ISO-8859-1 \
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-delimited-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --query "op.fromView('example', 'employee', '')" ^
    --path destination ^
    --encoding ISO-8859-1 ^
```
{% endtab %}
{% endtabs %}

### JSON Lines

The `export-json-lines-files` command writes one or more [JSON Lines](https://jsonlines.org/) to the directory 
specified by the `--path` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-json-lines-files \
  --connection-string "flux-example-user:password@localhost:8004" \
  --query "op.fromView('example', 'employee', '')" \
  --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-json-lines-files ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --query "op.fromView('example', 'employee', '')" ^
  --path destination
```
{% endtab %}
{% endtabs %}


This command reuses Spark's support for writing JSON files. You can include any of the
[Spark JSON options](https://spark.apache.org/docs/3.5.6/sql-data-sources-json.html) via the `-P` option to control
how JSON Lines files are written. These options are expressed as `-PoptionName=optionValue`.

By default, each file will be written using the UTF-8 encoding. You can specify an alternate encoding via the
`--encoding` option - e.g.

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-json-lines-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('example', 'employee', '')" \
    --path destination \
    --encoding ISO-8859-1
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-json-lines-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --query "op.fromView('example', 'employee', '')" ^
    --path destination ^
    --encoding ISO-8859-1
```
{% endtab %}
{% endtabs %}

### ORC

The `export-orc-files` command writes one or more ORC files to the directory specified by the `--path` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-orc-files \
  --connection-string "flux-example-user:password@localhost:8004" \
  --query "op.fromView('example', 'employee', '')" \
  --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-orc-files ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --query "op.fromView('example', 'employee', '')" ^
  --path destination
```
{% endtab %}
{% endtabs %}


This command reuses Spark's support for writing ORC files. You can include any of the
[Spark ORC data source options](https://spark.apache.org/docs/3.5.6/sql-data-sources-orc.html) via the `-P` option to
control how ORC content is written. These options are expressed as `-PoptionName=optionValue`.

For configuration options listed in the above Spark ORC guide, use the `--spark-conf` option instead. For example,
`--spark-conf spark.sql.orc.impl=hive` would change the type of ORC implementation.

### Parquet

The `export-parquet-files` command writes one or more Parquet files to the directory specified by the `--path` option: 

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-parquet-files \
  --connection-string "flux-example-user:password@localhost:8004" \
  --query "op.fromView('example', 'employee', '')" \
  --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-parquet-files ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --query "op.fromView('example', 'employee', '')" ^
  --path destination
```
{% endtab %}
{% endtabs %}

This command reuses Spark's support for writing Parquet files. You can include any of the
[Spark Parquet data source options](https://spark.apache.org/docs/3.5.6/sql-data-sources-parquet.html) via the `-P` option to
control how Parquet content is written. These options are expressed as `-PoptionName=optionValue`.

For configuration options listed in the above Spark Parquet guide, use the `--spark-conf` option instead. For example, 
`--spark-conf spark.sql.parquet.compression.codec=gzip` would change the compressed used for writing Parquet files.

## Controlling the save mode

Each of the commands for exporting rows to files supports a `--mode` option that controls how data is written to a 
location where data already exists. This option supports the following values:

- `Append` = append data if the destination already exists.
- `Overwrite` = replace data if the destination already exists. 
- `ErrorIfExists` = throw an error if the destination already exists. This is the default mode.
- `Ignore` = do not write any data if the destination already exists. 

For convenience, the above values are case-sensitive so that you can ignore casing when choosing a value. 

As of the 1.1.0 release of Flux, `--mode` defaults to `Append` for commands that write to a filesystem. In the 1.0.0
release, these commands defaulted to `Overwrite`. The `export-jdbc` command defaults to `ErrorIfExists` to avoid altering
an existing table in any way.

For further information on each mode, please see 
[the Spark documentation](https://spark.apache.org/docs/3.5.6/sql-data-sources-load-save-functions.html#save-modes).

## Tuning query performance

This section only applies for Optic queries that begin with `op.fromView` as the data access function. For other Optic
queries, Flux will execute the query in a single call to MarkLogic. The performance of such a query will be determined
by the query, your MarkLogic index settings, and your MarkLogic environment. 

The `--batch-size` and `--partitions` options are used to tune performance by controlling how many rows are retrieved in
a single call to MarkLogic and how many requests are made in parallel to MarkLogic. It is recommended to first test your command
without setting these options to see if the performance is acceptable. When you are ready to attempt to optimize the
performance of your export command, please see the
[this guide on Optic query performance](https://marklogic.github.io/marklogic-spark-connector/reading-data/optic.html#tuning-performance).

If your Optic query performs one or more joins, and/or is intended more to produce a report, consider using the 
following options:

```
--partitions 1
--batch-size 0
```

The above options will result in the query being executed in a single call to MarkLogic. This is typically appropriate
for queries with joins and aggregations, as trying to partition that query may result in either duplicate rows or 
incorrect results. You will need to verify though that the total number of rows returned by your query can be retrieved
in a single request to MarkLogic without the request timing out. 

### Partitions and Spark worker threads

As of Flux 1.2.0, when you set the number of partitions via `--partitions`, Flux will default the value of the 
`--spark-master-url` option to `local[N]`, where `N` is the number of partitions. This option controls the number of 
worker threads available to Spark. This default value ensures that each partition that reads rows from MarkLogic has 
a worker thread available for it. 

Depending on the target for writing rows, you may wish to use the `--repartition` option as well. This will adjust the
number of partitions after all the rows have been read from MarkLogic. This approach can be useful for a scenario where
Flux can quickly read a large number of rows from MarkLogic, but writing them to the target of the export command is 
much slower.

---
layout: default
title: Common Options
nav_order: 3
---

This guide describes options common to every command in Flux.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Viewing usage

You can view usage for all Flux commands by running Flux without any command name or options:

    ./bin/flux

To see a list of options for a particular command, run the `help` command followed by the name of the command
you are interested in:

    ./bin/flux help import-jdbc

The "help" for each command will first list the command-specific options, with each required option having an asterisk
next to it. The command-specific options are followed by the connection options for connecting to MarkLogic, and those
are followed by a list of options common to every Flux command. 

## Command abbreviations

You can specify a command name without entering its full name, as long as you enter a sufficient number of characters
such that Flux can uniquely identify the command name.

For example, instead of entering `import-parquet-files`, you can enter `import-p` as it is the only command in
Flux beginning with that sequence of letters:

    ./bin/flux import-p --path path/to/data etc...

If Flux cannot uniquely identify the command name, it will print an error and list the command names that match what
you entered.

## Reading options from a file

Flux supports reading options from a file. In a text file, you can either add each option name and value to a separate
line:

```
--host
localhost
--port
8000
etc...
```

Or you can put one or more options on the same line:

```
--host localhost
--port 8000
etc...
```

You then reference the file via the `@` symbol followed by a filename:

    ./bin/flux import-files @my-options.txt

You can reference multiple files this way and also include additional options on the command line.

## Connecting to MarkLogic

Every command in Flux will need to connect to a MarkLogic database, either for reading data or writing data or both. 
Generally, you must include at least the following information for each command:

- The name of a MarkLogic host.
- The port of a [MarkLogic REST API app server](https://docs.marklogic.com/guide/rest-dev) connected to the database you wish to interact with.
- Authentication information.

### Using a connection string

For the common use case of using digest or basic authentication with a MarkLogic app server, you can use the 
`--connection-string` option to specify the host, port, username, and password in a single concise option:

    --connection-string user:password@host:port

You can also specify a database name in case the database you wish to interact with is not the one associated with 
the port of the app server you connect to:

    --connection-string user:password@host:port/databaseName

In case your username or password use symbols like "@", you can 
[percent encode](https://developer.mozilla.org/en-US/docs/Glossary/Percent-encoding) the values. For example, for a 
password of `sp@r:k`, you would use the following string:

    --connection-string user:sp%40r%3Ak@host:8000

For other authentication mechanisms, you must use the `--host` and `--port` options to define the host and port for 
your MarkLogic app server. 

### Determining the connection type

The `--connection-type` option determines which of the following approaches Flux uses for connecting to MarkLogic:

- `GATEWAY` = the default value; Flux assumes that it cannot directly connect to each host in the MarkLogic cluster, most
likely due to the value of `--host` or the host value found in `--connection-string` being that of a load balancer that
controls access to MarkLogic.
- `DIRECT` = Flux will try to connect to each host in the MarkLogic cluster. 

If you do not have a load balancer in front of MarkLogic, and if Flux is able to connect to each host that hosts one
or more forests for the database you wish to access, then you can set `--connection-type` to a value of `DIRECT`. This
will often improve performance as Flux will be able to both connect to multiple hosts, thereby utilizing the app server
threads available on each host, and also write directly to a forest on the host that it connects to. 

### Connection options

All available connection options are shown in the table below:

| Option | Description | 
| --- | --- |
| `--auth-type` | Type of authentication to use. Possible values are `BASIC`, `DIGEST`, `CLOUD`, `KERBEROS`, `CERTIFICATE`, and `SAML`.|
| `--base-path` | Path to prepend to each call to a MarkLogic [REST API app server](https://docs.marklogic.com/guide/rest-dev). |
| `--certificate-file` | File path for a keystore to be used for `CERTIFICATE` authentication. |
| `--certificate-password` | Password for the keystore referenced by `--certificate-file`. |
| `--connection-string` |  Defines a connection string as user:password@host:port/optionalDatabaseName; only usable when using `DIGEST` or `BASIC` authentication. |
| `--cloud-api-key` | API key for authenticating with a MarkLogic Cloud cluster when authentication type is `CLOUD`. |
| `--connection-type` |  Set to `DIRECT` if connections can be made directly to each host in the MarkLogic cluster. Defaults to `GATEWAY`. Possible values are `DIRECT` and `GATEWAY`. |
| `--database` | Name of a database to connect if it differs from the one associated with the app server identified by `--port`. |
| `--disable-gzipped-responses` | If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small.
| `--host` | The MarkLogic host to connect to. |
| `--kerberos-principal` | Principal to be used with `KERBEROS` authentication. |
| `--keystore-algorithm` |  Algorithm of the keystore identified by `--keystore-path`; defaults to `SunX509`. |
| `--keystore-password` | Password for the keystore identified by `--keystore-path`. |
| `--keystore-path` | File path for a keystore for two-way SSL connections. |
| `--keystore-type` | Type of the keystore identified by `--keystore-path`; defaults to `JKS`. |
| `--password` | Password when using `DIGEST` or `BASIC` authentication. |
| `--port` | Port of the [REST API app server](https://docs.marklogic.com/guide/rest-dev) to connect to. |
| `--saml-token` | Token to be used with `SAML` authentication. |
| `--ssl-hostname-verifier` | Hostname verification strategy when connecting via SSL. Possible values are `ANY`, `COMMON`, and `STRICT`. |
| `--ssl-protocol` | SSL protocol to use when the MarkLogic app server requires an SSL connection. If a keystore or truststore is configured, defaults to `TLSv1.2`. |
| `--truststore-algorithm` | Algorithm of the truststore identified by `--truststore-path`; defaults to `SunX509`. |
| `--truststore-password` | Password for the truststore identified by `--truststore-path`. |
| `--truststore-path` | File path for a truststore for establishing trust with the certificate used by the MarkLogic app server. |
| `--truststore-type` | Type of the truststore identified by `--truststore-path`; defaults to `JKS`. |
| `--username` | Username when using `DIGEST` or `BASIC` authentication. |


## Previewing data

The `--preview` option works on every command in Flux. For example, given a set of Parquet files in a directory, 
you can preview an import without writing any of the data to MarkLogic:

```
./bin/flux import-parquet-files \
    --path export/parquet \
    --preview 10
```

The number after `--preview` specifies how many records to show. You can use `--preview-drop` to specify potentially
verbose columns to drop from the preview. And you can use `--preview-list` to see the results in a list
instead of in a table:

```
./bin/flux import-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --path export/parquet \
    --preview 10 \
    --preview-drop job_title,department \
    --preview-list
```

Note that in the case of previewing an import, Flux will show the data as it has been read, which consists of a set of
Spark rows with columns. The data is not shown as a set of documents yet, as the transformation of rows to documents 
occurs when the data is written to MarkLogic.

For some commands, it may be helpful to see the schema of the data read from the command's data source. For example, 
when exporting data with a MarkLogic Optic query, you may wish to verify that the datatypes of each column are what you
expect before writing the data to a Parquet file or relational database. Use the `--preview-schema` option to request 
that Flux log the schema and not write any data:

```
./bin/flux export-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees')" \
    --path export/parquet \
    --preview-schema
```

## Applying a limit

For many use cases, it can be useful to only process a small subset of the source data to ensure that the results
are correct. The `--limit` option will achieve this by limiting the number of rows read by a command.

The following shows an example of only importing the first 10 rows from a delimited file:

```
./bin/flux import-delimited-files \
    --path ../data/employees.csv.gz \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update \
    --collections employee \
    --uri-template "/employee/{id}.json" \
    --limit 10
```

## Counting records to process

Instead of viewing a preview of data or limiting how much data is processed by Flux, you may want to first obtain a 
count of records that will be read from the data source associated with the Flux command you are executing. This can be 
useful when dealing with a data source where it can be difficult to know how much data will be read, such as with very 
large aggregate XML or Parquet files. 

To view a count, include the `--count` option, which results in Flux reading all the data but not writing any of it
to the destination associated with the command:

```
./bin/flux import-delimited-files \
    --path ../data/employees.csv.gz \
    --connection-string "flux-example-user:password@localhost:8004" \
    --count
```

Flux will then log the count:

```
[main] INFO com.marklogic.flux: Count: 1000
```

The performance of using `--count` is dependent on how quickly data can be read from the command's data source. For 
example, commands that export documents from MarkLogic must read every document to be exported in order to calculate 
a count. For a scenario like that, you can likely much more quickly determine a count by running your query in 
MarkLogic's qconsole application.

## Viewing progress

Each Flux command supports a `--log-progress` option that you can use to specify how often you want a message logged
indicating progress. Import commands will show progress in terms of writing data to MarkLogic, while export 
commands will show progress in terms of reading data from MarkLogic. The `copy` command can show both types of progress, 
with `--log-progress` specifying an interval for logging data that has been read and `--output-log-progress` 
specifying an interval for logging data that has been written. The `reprocess` command is similar to import commands
in that it shows progress in terms of processing data that has been read from MarkLogic.

The `--log-progress` option defaults to a value of 10,000, with one exception - export commands that read rows from 
MarkLogic defaults `--log-progress` to a value of 100,000. For an import command with a value of 10,000, 
Flux will log a message at the `INFO` level every time it reads 10,000 documents - so at 10,000, 20,000, etc. 
You can change this value to suit your needs, though you typically will want it to be significantly larger than 
the value of `--batch-size` if the command supports that option.

## Viewing a stacktrace

When a command fails, Flux will stop execution of the command and display an error message. If you wish to see the 
underlying stacktrace associated with the error, run the command with the `--stacktrace` option included. This is 
included primarily for debugging purposes, as the stacktrace may be fairly long with only a small portion of it 
potentially being helpful. The initial error message displayed by Flux is intended to be as helpful as possible. 

## Configuring logging

Flux uses a [Log4J2 properties file](https://logging.apache.org/log4j/2.x/manual/configuration.html#Properties) to 
configure its logging. The file is located in a Flux installation at `./conf/log4j2.properties`. You are free to 
customize this file to meet your needs for logging.

## Advanced Spark options

Flux is built on top of [Apache Spark](https://spark.apache.org/) and provides a number of command line options for 
configuring the underlying Spark runtime environment used by Flux. 

### Configuring the number of partitions 

Flux uses Spark partitions to allow for data to be read and written in parallel. Each partition can be thought of as 
a separate worker, operating in parallel with each other worker. 

A number of partitions will be determined by the command that you run before it reads data. The nature of the data 
source directly impacts the number of partitions that will be created. 

If you find that an insufficient number of partitions are created - i.e. the writer phase of your Flux command is not
sending as much data to MarkLogic as it could - consider using the `--repartition` option to force a number of 
partitions to be created after the data has been read. The downside to using `--repartition` is that all the data must
be read first. Generally, this option will help when data can be read quickly and the performance of writing can be 
improved by using more partitions than were created when reading data.

### Configuring a Spark URL

By default, Flux creates a Spark session with a master URL of `local[*]`. You can change this via the 
`--spark-master-url` option; please see 
[the Spark documentation](https://spark.apache.org/docs/latest/submitting-applications.html#master-urls) for examples
of valid values. If you are looking to run a Flux command on a remote Spark cluster, please instead see the 
[Spark Integration guide](spark-integration.md) for details on integrating Flux with `spark-submit`.

### Configuring the Spark runtime

Some Flux commands reuse [Spark data sources](https://spark.apache.org/docs/latest/sql-data-sources.html) that 
accept configuration items via the Spark runtime. You can provide these configuration items via the `-C` option. 
For example, the [Spark Avro data source](https://spark.apache.org/docs/latest/sql-data-sources-avro.html#configuration)
identifies several configuration items, such as `spark.sql.avro.compression.codec`. You can set this value by 
including `-Cspark.sql.avro.compression.codec=snappy` as a command line option. 

Note that the majority of [Spark cluster configuration properties](https://spark.apache.org/docs/latest/configuration.html)
cannot be set via the `-C` option as those options must be set before a Spark session is created. For further control 
over the Spark session, please see the [Spark Integration guide](spark-integration.md) for details on integrating Flux
with `spark-submit`.

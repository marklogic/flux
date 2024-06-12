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

## Connecting to MarkLogic

Every command in Flux will need to connect to a MarkLogic database, either for reading data or writing data or both. 
Generally, you must include at least the following information for each command:

- The name of a MarkLogic host.
- The port of a [MarkLogic REST API app server](https://docs.marklogic.com/guide/rest-dev) connected to the database you wish to interact with.
- Authentication information.

For the common use case of using digest or basic authentication with a MarkLogic app server, you can use the 
`--connection-string` option to specify the host, port, username, and password in a single concise option:

    --connection-string user:password@host:port

For other authentication mechanisms, you must use the `--host` and `--port` options to define the host and port for 
your MarkLogic app server. 

All available connection options are shown in the table below:

| Option | Description | 
| --- | --- |
| --auth-type | Type of authentication to use. Possible values are `BASIC`, `DIGEST`, `CLOUD`, `KERBEROS`, `CERTIFICATE`, and `SAML`.|
| --base-path | Path to prepend to each call to a MarkLogic REST API app server. |
| --certificate-file | File path for a keystore to be used for 'CERTIFICATE' authentication. |
| --certificate-password | Password for the keystore referenced by '--certificate-file'. |
| --connection-string |  Defines a connection string as user:password@host:port; only usable when using `DIGEST` or `BASIC` authentication. |
| --cloud-api-key | API key for authenticating with a MarkLogic Cloud cluster. |
| --connection-type |  Defines whether connections can be made directly to each host in the MarkLogic cluster. Possible values are `DIRECT` and `GATEWAY`. |
| --database | Name of a database to connect if it differs from the one associated with the app server identified by '--port'. |
| --disable-gzipped-responses | If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small.
| --host | The MarkLogic host to connect to. |
| --kerberos-principal | Principal to be used with `KERBEROS` authentication. |
| --keystore-algorithm |  Algorithm of the keystore identified by '--keystore-path'; defaults to `SunX509`. |
| --keystore-password | Password for the keystore identified by '--keystore-path'. |
| --keystore-path | File path for a keystore for two-way SSL connections. |
| --keystore-type | Type of the keystore identified by '--keystore-path'; defaults to `JKS`. |
| --password | Password when using `DIGEST` or `BASIC` authentication. |
| --port | Port of the [REST API app server](https://docs.marklogic.com/guide/rest-dev) to connect to. |
| --saml-token | Token to be used with `SAML` authentication. |
| --ssl-hostname-verifier | Hostname verification strategy when connecting via SSL. Possible values are `ANY`, `COMMON`, and `STRICT`. |
| --ssl-protocol | SSL protocol to use when the MarkLogic app server requires an SSL connection. If a keystore or truststore is configured, defaults to `TLSv1.2`. |
| --truststore-algorithm | Algorithm of the truststore identified by '--truststore-path'; defaults to `SunX509`. |
| --truststore-password | Password for the truststore identified by '--truststore-path'. |
| --truststore-path | File path for a truststore for establishing trust with the certificate used by the MarkLogic app server. |
| --truststore-type | Type of the truststore identified by '--truststore-path'; defaults to `JKS`. |
| --username | Username when using `DIGEST` or `BASIC` authentication. |


## Reading options from a file

Flux supports reading options from a file, similar to MLCP. In a text file, put each option name and value on separate
lines:

```
--host
localhost
--port
8000
etc...
```

You then reference the file via the `@` symbol followed by a filename:

    ./bin/flux import-files @my-options.txt

You can reference multiple files this way and also include options on the command line too.

## Previewing data

The `--preview` option works on every command in Flux. For example, given a set of Parquet files in a directory, 
you can preview an import without writing any of the data to MarkLogic:

```
./bin/flux import-parquet-files \
    --path export/parquet \
    --preview 10
```

For commands that read from a source other than MarkLogic, you are not required to specify any MarkLogic connection
information when including `--preview` since no connection needs to be made to MarkLogic.

The number after `--preview` specifies how many records to show. You can use `--preview-drop` to specify potentially
verbose columns to drop from the preview. And you can use `--preview-vertical` to see the results a vertical display
instead of in a table:

```
./bin/flux import-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --path export/parquet \
    --preview 10 \
    --preview-drop job_title,department
    --preview-vertical
```

Note that in the case of previewing an import, Flux will show the data as it has been read, which consists of a set of
Spark rows with columns. The data is not shown as a set of documents yet, as the transformation of rows to documents 
occurs when the data is written to MarkLogic.

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

## Repartitioning

When Flux reads data from a data source, it does so via one or more "partitions" that allows for data to be read in 
parallel. The number of partitions depends on the type of data source and may be configurable depending on which 
command you are running. 

When Flux writes the data it has read, a separate worker is created for each partition, thereby allowing for data to be
written in parallel. 

You can adjust the number of partitions and thus the number of workers used for writing data via the `--repartition` 
input, which accepts a number greater than zero. You may find better performance by increasing the number of partitions
for writing data to MarkLogic. In other cases, such as exporting data to ZIP files, you can control how many ZIP files
are written via `--repartition`. 

## Viewing a stacktrace

When a command fails, Flux will stop execution of the command and display an error message. If you wish to see the 
underlying stacktrace associated with the error, run the command with the `--stacktrace` option included. This is 
included primarily for debugging purposes, as the stacktrace may be fairly long with only a small portion of it 
potentially being helpful. The initial error message displayed by Flux is intended to be as helpful as possible. 

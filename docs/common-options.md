---
layout: default
title: Common Options
nav_order: 3
---

This guide describes options common to every command in NT.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Connecting to MarkLogic

Every command in NT will need to connect to a MarkLogic database, either for reading data or writing data or both. 
Generally, you must include at least the following information for each command:

- The name of a MarkLogic host.
- The port of a MarkLogic app server connected to the database you wish to interact with.
- Authentication information.

For the common use case of using digest or basic authentication with a MarkLogic app server, you can use the 
`--connectionString` option to specify the host, port, username, and password in a single concise option:

    --connectionString user:password@host:port

For other authentication mechanisms, you must use the `--host` and `--port` options to define the host and port for 
your MarkLogic app server. 

All available connection options are shown in the table below:

| Option | Description | 
| --- | --- |
| --authType | Type of authentication to use. Possible values are `BASIC`, `DIGEST`, `CLOUD`, `KERBEROS`, `CERTIFICATE`, and `SAML`.|
| --basePath | Path to prepend to each call to a MarkLogic REST API app server. |
| --certificateFile | File path for a key store to be used for 'CERTIFICATE' authentication. |
| --certificatePassword | Password for the key store referenced by '--certificateFile'. |
| --connectionString |  Defines a connection string as user:password@host:port; only usable when using `DIGEST` or `BASIC` authentication. |
| --cloudApiKey | API key for authenticating with a MarkLogic Cloud cluster. |
| --connectionType |  Defines whether connections can be made directly to each host in the MarkLogic cluster. Possible values are `DIRECT` and `GATEWAY`. |
| --database | Name of a database to connect if it differs from the one associated with the app server identified by '--port'. |
| --disableGzippedResponses | If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small.
| --host | The MarkLogic host to connect to. |
| --kerberosPrincipal | Principal to be used with `KERBEROS` authentication. |
| --keyStoreAlgorithm |  Algorithm of the key store identified by '--keyStorePath'; defaults to `SunX509`. |
| --keyStorePassword | Password for the key store identified by '--keyStorePath'. |
| --keyStorePath | File path for a key store for two-way SSL connections. |
| --keyStoreType | Type of the key store identified by '--keyStorePath'; defaults to `JKS`. |
| --password | Password when using `DIGEST` or `BASIC` authentication. |
| --samlToken | Token to be used with `SAML` authentication. |
| --sslHostnameVerifier | Hostname verification strategy when connecting via SSL. Possible values are `ANY`, `COMMON`, and `STRICT`. |
| --sslProtocol | SSL protocol to use when the MarkLogic app server requires an SSL connection. If a key store or trust store is configured, defaults to `TLSv1.2`. |
| --trustStoreAlgorithm | Algorithm of the trust store identified by '--trustStorePath'; defaults to `SunX509`. |
| --trustStorePassword | Password for the trust store identified by '--trustStorePath'. |
| --trustStorePath | File path for a trust store for establishing trust with the certificate used by the MarkLogic app server. |
| --trustStoreType | Type of the trust store identified by '--trustStorePath'; defaults to `JKS`. |
| --username | Username when using `DIGEST` or `BASIC` authentication. |


## Reading options from a file

NT supports reading options from a file, similar to MLCP. In a text file, put each option name and value on separate
lines:

```
--host
localhost
--port
8000
etc...
```

You then reference the file via the `@` symbol followed by a filename:

    ./bin/nt import_files @my-options.txt

You can reference multiple files this way and also include options on the command line too.

## Previewing data

The `--preview` option works on every command in NT. For example, given a set of Parquet files in a directory, 
you can preview an import without writing any of the data to MarkLogic:

```
./bin/nt import_parquet_files \
    --path export/parquet \
    --preview 10
```

For commands that read from a source other than MarkLogic, you are not required to specify any MarkLogic connection
information when including `--preview` since no connection needs to be made to MarkLogic.

The number after `--preview` specifies how many records to show. You can use `--previewDrop` to specify potentially
verbose columns to drop from the preview. And you can use `--previewVertical` to see the results a vertical display
instead of in a table:

```
./bin/nt import_parquet_files \
    --connectionString "nt-user:password@localhost:8004" \
    --path export/parquet \
    --preview 10 \
    --previewDrop job_title,department
    --previewVertical
```

Note that in the case of previewing an import, NT will show the data as it has been read, which consists of a set of
Spark rows with columns. The data is not shown as a set of documents yet, as the transformation of rows to documents 
occurs when the data is written to MarkLogic.

## Applying a limit

For many use cases, it can be useful to only process a small subset of the source data to ensure that the results
are correct. The `--limit` option will achieve this by limiting the number of rows read by a command.

The following shows an example of only importing the first 10 rows from a delimited file:

```
./bin/nt import_delimited_files \
    --path ../data/employees.csv.gz \
    --connectionString "nt-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections employee \
    --uriTemplate "/employee/{id}.json" \
    --limit 10
```

## Repartitioning

When NT reads data from a data source, it does so via one or more "partitions" that allows for data to be read in 
parallel. The number of partitions depends on the type of data source and may be configurable depending on which 
command you are running. 

When NT writes the data it has read, a separate worker is created for each partition, thereby allowing for data to be
written in parallel. 

You can adjust the number of partitions and thus the number of workers used for writing data via the `--repartition` 
input, which accepts a number greater than zero. You may find better performance by increasing the number of partitions
for writing data to MarkLogic. In other cases, such as exporting data to ZIP files, you can control how many ZIP files
are written via `--repartition`. 

## Viewing a stacktrace

When a command fails, NT will stop execution of the command and display an error message. If you wish to see the 
underlying stacktrace associated with the error, run the command with the `--stacktrace` option included. This is 
included primarily for debugging purposes, as the stacktrace may be fairly long with only a small portion of it 
potentially being helpful. The initial error message displayed by NT is intended to be as helpful as possible. 

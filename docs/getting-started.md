---
layout: default
title: Getting Started
nav_order: 2
---

This guide describes how to get started with Flux with some examples demonstrating its functionality. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}
 
## Setup

The examples in this guide, along with examples found throughout this documentation, depend on a small application in the 
`./examples/getting-started` directory in this repository. The examples assume that the application will be deployed to 
a MarkLogic instance and that Flux will be run from the `./examples/getting-started` directory as well. However, you are 
free to install Flux anywhere and use the examples as a reference for running Flux on your own data. 

### Obtaining Flux

Flux can either be downloaded from [its GitHub releases page](https://github.com/marklogic/spark-etl/releases), 
from the [internal Wiki](https://wiki.marklogic.com/display/PM/Spark+ETL+0.1+Release), or built locally. 
If you wish to build it locally, please see the `CONTRIBUTING.md` file in this repository for instructions.

After downloading or building Flux, extract it into the `./examples/getting-started` directory in this repository to 
test it with the examples. 

### Deploying the example application

Follow these steps to run the examples in this guide:

1. Clone this repository if you have not already.
2. `cd examples/getting-started`
3. Create the file `gradle-local.properties` and add `mlPassword=your admin user password` to it.
4. Change `mlHost` and `mlRestPort` as needed based on your MarkLogic installation.
5. Run `./gradlew -i mlDeploy` to deploy the example application.

This will create a new REST API app server on port 8004 in your local MarkLogic installation, unless you modified the
values of `mlHost` and `mlRestPort`. It also creates an "nt-user" MarkLogic user that has the necessary MarkLogic roles
and privileges for running the examples in this guide. Finally, the application includes a 
[MarkLogic TDE template](https://docs.marklogic.com/guide/app-dev/TDE) that creates a view in MarkLogic for the purpose
of demonstrating commands that utilize a [MarkLogic Optic query](https://docs.marklogic.com/guide/app-dev/OpticAPI).

## Usage

You can run the tool without any options to see the list of available commands (all examples will assume the use of
Unix; if you are on Windows, substitute `./bin/flux` with `bin/flux`):

    cd nt
    ./bin/flux

As shown in the usage, every command is invoked by specifying its name and one or more options required to run the
command. To see the usage for a particular command, such as `import-files`, run:

    ./bin/flux help import-files

Required options are marked with an asterisk - "*". Additionally, every command requires that either `--connection-string`
or `--host` and `--port` be specified so that the tool knows which MarkLogic cluster to connect to. 

The `--connection-string` option provides a succinct way of defining the host, port, username, and password when the MarkLogic
app server you connect to requires basic or digest authentication. Its value is of the form 
`(user):(password)@(host):(port)`. For example:

    ./bin/flux import-files --connection-string "my-user:my-secret@localhost:8000" ...

Options can also be read from a file; see the [Common Options](common-options.md) guide for more information.

## Importing data

Flux allows for data to be imported from a variety of data sources, such as a local filesystem, S3, or any database 
accessible via a JDBC driver. The example project contains a gzipped CSV file generated via 
[Mockaroo](https://mockaroo.com/). Run the following to load this file, allowing for other Flux capabilities to be 
demonstrated:

```
./bin/flux import-delimited-files \
    --path ../data/employees.csv.gz \
    --connection-string "nt-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections employee \
    --uri-template "/employee/{id}.json"
```

By accessing your [MarkLogic qconsole](https://docs.marklogic.com/guide/qconsole), you can see that the `employee`
collection in the `nt-example-content` database now has 1000 JSON documents, one for each line in the gzipped CSV file. 

### Importing via JDBC

The `import-jdbc` command in Flux supports reading rows from any database with a supported JDBC driver. Similar to other
tools that support JDBC access, you must first add your database's JDBC driver to the Flux classpath by adding the JDBC
driver jar to the `./ext` directory in the Flux installation. 

The following shows a notional example of reading rows from a Postgres database (this example will not work as it 
requires a separate Postgres database; it is only included for reference):

```
./bin/flux import-jdbc \
    --jdbc-url "jdbc:postgresql://localhost/dvdrental?user=postgres&password=postgres" \
    --jdbc-driver "org.postgresql.Driver" \
    --query "select * from customer" \
    --connection-string "flux-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections customer
```

See the [Import guide](import/import.md) for further details, including how you can aggregate rows together via a SQL join, 
thus producing hierarchical documents with nested data structures.

## Exporting data 

Flux supports several commands for exporting data from MarkLogic, either as documents or rows, to a variety of 
destinations. Commands that export documents support a variety of queries, while commands that export rows use the
[MarkLogic Optic API](https://docs.marklogic.com/guide/app-dev/OpticAPI)
to select rows. The following shows an example of exporting the 1000 employee documents to a single zip file:

```
mkdir export
./bin/flux export-files \
    --connection-string "nt-user:password@localhost:8004" \
    --collections employee \
    --path export \
    --compression zip \
    --zip-file-count 1
```

The above command specifies a collection of documents to export. You can also use the `--query` option to specify a
[structured query](https://docs.marklogic.com/guide/search-dev/structured-query), 
[serialized CTS query](https://docs.marklogic.com/guide/rest-dev/search#id_30577), or 
[complex query](https://docs.marklogic.com/guide/rest-dev/search#id_69918), either as JSON or XML. You can also use 
`--string-query` to leverage MarkLogic's 
[search grammar](https://docs.marklogic.com/guide/search-dev/string-query) for selecting documents. 

The following command shows a collection, a string query, and a structured query used together, resulting 
in 4 JSON documents being written to `./export/employee`:

```
./bin/flux export-files \
    --connection-string "nt-user:password@localhost:8004" \
    --collections employee \
    --string-query Engineering \
    --query '{"query": {"value-query": {"json-property": "job_title", "text": "Junior Executive"}}}' \
    --path export \
    --pretty-print
```

See [the Export guide](export/export.md) for more information.

### Exporting to S3

Flux allows for data to be exported to S3, with the same approach working for importing data as well. You can 
reference an S3 bucket path via the `s3a://` prefix. The `--s3-add-credentials` option will then use the AWS SDK to access your
AWS credentials; please see the 
[AWS documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for information on how to configure your credentials. 

The following shows an example of exporting to S3 with a fictitious bucket name. You can use this with your own S3 
bucket, ensuring that your AWS credentials give you access to writing to the bucket:

```
./bin/flux export-files \
    --connection-string "nt-user:password@localhost:8004" \
    --collections employee \
    --compression zip \
    --zip-file-count 1 \
    --path s3a://bucket-name-changeme \
    --s3-add-credentials
```

### Exporting rows

Flux allows for exporting rows from MarkLogic via an Optic query and writing the data to a variety of row-oriented 
destinations, such as Parquet files or an RDBMS. The following demonstrates writing rows to Parquet files: 

```
mkdir export/parquet
./bin/flux export-parquet-files \
    --connection-string "nt-user:password@localhost:8004" \
    --path export/parquet \
    --query "op.fromView('Example', 'Employees', '')" 
```

You can also export rows via JDBC. Like the example above for importing via JDBC, this is a notional example only. 
Change the details in it to match your database and JDBC driver, ensuring that the JDBC driver jar is in the 
`./ext` directory of your Flux installation:

```
./bin/flux export-jdbc \
    --connection-string "nt-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees', '')" \
    --jdbc-url "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" \
    --jdbc-driver "org.postgresql.Driver" \
    --table employees \
    --mode overwrite
```

## Previewing commands

For many data movement use cases, it can be helpful to see a preview of the data read from a particular source before
any processing occurs to write that data to a destination. Flux supports this via a `--preview` option that accepts a 
number of records to read and display, but without writing the data anywhere. The following example shows how an export 
command can preview 10 rows read from MarkLogic without writing any data to files:

```
./bin/flux export-parquet-files \
    --connection-string "nt-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees', '')" \
    --path export/parquet \
    --preview 10
```

See the [Common Options guide](common-options.md) for more information.

## Reprocessing data

The `reprocess` command in Flux allows for custom code - either JavaScript or XQuery - to be executed for selecting and
processing data in MarkLogic. The following shows an example of adding a new collection to each of the employee
documents:

```
./bin/flux reprocess \
    --connection-string "nt-user:password@localhost:8004" \
    --read-javascript "cts.uris(null, null, cts.collectionQuery('employee'))" \
    --write-javascript "declareUpdate(); xdmp.documentAddCollections(URI, 'reprocessed')" 
```

In qconsole, you can see that the 1000 employee documents are now also in the `reprocessed` collection. 

For more information, please see the [Reprocessing guide](reprocess.md).

## Copying data

The `copy` command in Flux is similar to the commands for exporting data, but instead allows you to read documents
from one database and write them to another. When copying, you may want to include metadata for each document - 
collections, permissions, quality, properties, and metadata values. This is accomplished via the `--categories`
option, with the default value of `content,metadata` returning both the document and all of its metadata.

The following shows how to copy the 1000 employee documents to the out-of-the-box Documents database in your 
MarkLogic instance:

```
./bin/flux copy \
    --connection-string "nt-user:password@localhost:8004" \
    --collections employee \
    --output-connection-string "nt-user:password@localhost:8000"
```

For more information, please see the [Copying guide](copy.md).

---
layout: default
title: Getting Started
nav_order: 2
---

This guide describes how to get started with NT with some examples demonstrating its functionality. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}
 
## Setup

The examples in this guide, along with examples found throughout this documentation, depend on a small application in the 
`./examples/getting-started` directory in this repository. The examples assume that the application will be deployed to 
a MarkLogic instance and that NT will be run from the `./examples/getting-started` directory as well. However, you are 
free to install NT anywhere and use the examples as a reference for running NT on your own data. 

### Obtaining NT

NT can either be downloaded from [its GitHub releases page](https://github.com/marklogic/spark-etl/releases) or 
built locally. If you wish to build it locally, please see the `CONTRIBUTING.md` file in this repository for instructions.

After downloading or building NT, extract it into the `./examples/getting-started` directory in this repository to 
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
Unix; if you are on Windows, substitute `./bin/nt` with `bin/nt`):

    cd nt
    ./bin/nt

As shown in the usage, every command is invoked by specifying its name and one or more options required to run the
command. To see the usage for a particular command, such as `import_files`, run:

    ./bin/nt help import_files

Required options are marked with an asterisk - "*". Additionally, every command requires that either `--clientUri`
or `--host` and `--port` be specified so that the tool knows which MarkLogic cluster to connect to. 

The `--clientUri` option provides a succinct way of defining the host, port, username, and password when the MarkLogic
app server you connect to requires basic or digest authentication. Its value is of the form 
`(user):(password)@(host):(port)`. For example:

    ./bin/nt import_files --clientUri "my-user:my-secret@localhost:8000" ...

Options can be read from a file; see the [Common Options](common-options.md) guide for more information.

As the documentation for NT is built out, you will typically be able to find a number of features in the list of 
command options that are not yet documented. Documentation will eventually encompass all options, but prior to 
the 1.0 release, you should expect to use `help name_of_command` to understand everything you can do with a particular
command.

## Importing data

NT allows for data to be imported from a variety of data sources, such as a local filesystem, S3, or any database 
accessible via a JDBC driver. The example project contains a gzipped CSV file generated via 
[Mockaroo](https://mockaroo.com/). Run the following to load this file, allowing for other NT capabilities to be 
demonstrated:

```
./bin/nt import_delimited_files \
    --path ../data/employees.csv.gz \
    --clientUri "nt-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections employee \
    --uriTemplate "/employee/{id}.json"
```

By accessing your [MarkLogic qconsole](https://docs.marklogic.com/guide/qconsole), you can see that the `employee`
collection in the `nt-example-content` database now has 1000 JSON documents, one for each line in the gzipped CSV file. 

### Importing via JDBC

The `import_jdbc` command in NT supports reading rows from any database with a supported JDBC driver. Similar to other
tools that support JDBC access, you must first add your database's JDBC driver to the NT classpath by adding the JDBC
driver jar to the `./ext` directory in the NT installation. 

The following shows a notional example of reading rows from a Postgres database (this example will not work as it 
requires a separate Postgres database; it is only included for reference):

```
./bin/nt import_jdbc \
    --jdbcUrl "jdbc:postgresql://localhost/dvdrental?user=postgres&password=postgres" \
    --jdbcDriver "org.postgresql.Driver" \
    --query "select * from customer" \
    --clientUri "new-tool-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections customer
```

See the [Import guide](import.md) for further details, including how you can aggregate rows together via a SQL join, 
thus producing hierarchical documents with nested data structures.

## Exporting data 

NT supports several commands for exporting data from MarkLogic, either as documents or rows, to a variety of 
destinations. Commands that export documents support a variety of queries, while commands that export rows use Optic
to select rows. The following shows an example of exporting the 1000 employee documents to a single zip file:

```
mkdir export
./bin/nt export_files \
    --clientUri "nt-user:password@localhost:8004" \
    --collections employee \
    --path export \
    --compression zip \
    --repartition 1

mkdir export
./bin/nt export_files \
    --clientUri "nt-user:password@localhost:8004" \
    --path export \
    --compression zip \
    --repartition 1
```

The above command specifies a collection of documents to export. You can also use the `--query` option to specify a
[structured query](https://docs.marklogic.com/guide/search-dev/structured-query), 
[serialized CTS query](https://docs.marklogic.com/guide/rest-dev/search#id_30577), or 
[complex query](https://docs.marklogic.com/guide/rest-dev/search#id_69918), either as JSON or XML. You can also use 
`--stringQuery` to leverage MarkLogic's search grammar for selecting documents. 

The following command shows a collection, a string query, and a structured query used together, resulting 
in 4 JSON documents being written to `./export/employee`:

```
./bin/nt export_files \
    --clientUri "nt-user:password@localhost:8004" \
    --collections employee \
    --stringQuery Engineering \
    --query '{"query": {"value-query": {"json-property": "job_title", "text": "Junior Executive"}}}' \
    --path export
```

See [the Export guide](export.md) for more information.

### Exporting to S3

NT allows for data to be exported easily to S3, with the same approach working for importing data as well. You can 
reference an S3 bucket path via the "s3a://" prefix. The `--s3AddCredentials` option will then use the AWS SDK to access your
AWS credentials; please see the 
[AWS documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for information on how to configure your credentials. 

The following shows an example of exporting to S3 with a fictitious bucket name. You can use this with your own S3 
bucket, ensuring that your AWS credentials give you access to writing to the bucket:

```
./bin/nt export_files \
    --clientUri "nt-user:password@localhost:8004" \
    --collections employee \
    --compression zip \
    --repartition 1 \
    --path s3a://rudin-public-bucket \
    --s3AddCredentials
```

### Exporting rows

NT allows for exporting rows from MarkLogic via an Optic query and writing the data to a variety of row-oriented 
destinations, such as Parquet files or an RDBMS. The following demonstrates writing rows to Parquet files: 

```
mkdir export/parquet
./bin/nt export_parquet_files \
    --clientUri "nt-user:password@localhost:8004" \
    --path export/parquet \
    --query "op.fromView('Example', 'Employees', '')" 
```

You can also export rows via JDBC. Like the example above for importing via JDBC, this is a notional example only. 
Change the details in it to match your database and JDBC driver, ensuring that the JDBC driver jar is in the 
`./ext` directory of your NT installation:

```
./bin/nt export_jdbc \
    --clientUri "nt-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees', '')" \
    --jdbcUrl "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" \
    --jdbcDriver "org.postgresql.Driver" \
    --table employees \
    --mode overwrite
```

## Previewing commands

For many data movement use cases, it can be helpful to see a preview of the data read from a particular source before
any processing occurs to write that data to a destination. NT supports this via a `--preview` option that accepts a 
number of records to read and display, but without writing the data anywhere. The following example shows how an export 
command can preview 10 rows read from MarkLogic without writing any data to files:

```
./bin/nt export_parquet_files \
    --clientUri "nt-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees', '')" \
    --path export/parquet \
    --preview 10
```

See the [Common Options guide](common-options.md) for more information.

## Reprocessing data

The `reprocess` command in NT allows for custom code - either JavaScript or XQuery - to be executed for selecting and
processing data in MarkLogic. The following shows an example of adding a new collection to each of the employee
documents:

```
./bin/nt reprocess \
    --clientUri "nt-user:password@localhost:8004" \
    --readJavascript "cts.uris(null, null, cts.collectionQuery('employee'))" \
    --writeJavascript "declareUpdate(); xdmp.documentAddCollections(URI, 'reprocessed')" 
```

In qconsole, you can see that the 1000 employee documents are now also in the `reprocessed` collection. 

For more information, please see the [Reprocessing guide](reprocess.md).

## Copying data

The 'copy' command in NT is similar to the commands for exporting data, but instead allows you to read documents
from one database and write them to another. When copying, you may want to include metadata for each document - 
collections, permissions, quality, properties, and metadata values. This is accomplished via the `--categories`
option, with `--categories=content,metadata` returning both the document and all of its metadata.

The following shows how to copy the 1000 employee documents to the out-of-the-box Documents database in your 
MarkLogic instance:

```
./bin/nt copy \
    --clientUri "nt-user:password@localhost:8004" \
    --collections employee \
    --categories content,metadata \
    --outputClientUri "nt-user:password@localhost:8000"
```

For more information, please see the [Copying guide](copy.md).

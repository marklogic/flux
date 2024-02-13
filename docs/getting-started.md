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
`examples/getting-started` directory in this repository. The examples assume that the application will be deployed to 
a MarkLogic instance and that the NT tool will be run from that directory as well. However, you are free to install 
NT anywhere and use the examples simply as a reference for running NT on your own data. 

### Obtaining NT

NT can either be downloaded from a TBD location or built locally. Given the size of the 
application - the distribution is over 500mb, primarily due to the inclusion of the AWS SDK to support S3 access - the file may not
yet be available at an internal location, and thus you will need to build the tool locally.

Perform these steps to build the tool locally:

1. Clone this repository.
2. Ensure you are using Java 11 or higher.
3. From the root directory of the repository, run `./gradlew distTar`, or `./gradlew distZip` if on Windows.

This will produce a file at `./new-tool-cli/build/distributions`. To use NT with the examples in the guide, move or copy
this file to the `examples/getting-started` directory and then extract it. Otherwise, you can extract the file to any
location you choose. 

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

You can run the tool without any arguments to see the list of available commands (all examples will assume the use of
Unix; if you are on Windows, substitute `./bin/nt` with `bin/nt`):

    ./bin/nt

As shown in the usage, every command is invoked by specifying its name and one or more options required to run the
command. To see the usage for a particular command, such as `import_files`, run:

    ./bin/nt help import_files

Required parameters are marked with an asterisk - "*". Additionally, every command requires that either `--clientUri`
or `--host` and `--port` be specified so that the tool known which MarkLogic cluster to connect to. 

As the documentation for NT is built out, you will typically be able to find a number of features in the list of 
command arguments that are not yet documented. Documentation will eventually encompass all arguments, but prior to 
the 1.0 release, you should expect to use `help name_of_command` to understand everything you can do with a particular
command.

## Importing data

NT allows for data to be imported from a variety of data sources, such as a local filesystem, S3, or any database 
accessible via a JDBC driver. The example project contains a gzipped CSV file generated via 
[Mockaroo](https://mockaroo.com/). Run the following to load this file, allowing for other NT capabilities to be 
demonstrated:

```
./bin/nt import_delimited_files --path data/employees.csv.gz \
    --clientUri nt-user:password@localhost:8004 \
    --permissions nt-role,read,nt-role,update --collections employee \
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
./bin/nt import_jdbc --jdbcUrl "jdbc:postgresql://localhost/dvdrental?user=postgres&password=postgres" \
  --jdbcDriver "org.postgresql.Driver" \
  --query "select * from customer" \
  --clientUri "new-tool-user:password@localhost:8004" \
  --permissions nt-role,read,nt-role,update --collections customer
```

## Exporting data 

NT supports several commands for exporting data from MarkLogic, either as documents or rows, to a variety of 
destinations. Commands that export documents support a variety of queries, while commands that export rows use Optic
to select rows. The following shows an example of exporting the 1000 employee documents to a single zip file:

```
mkdir export
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip --repartition 1
```

The `./export` directory will contain a single zip file. The `--repartition 1` argument causes a single file to be 
written. NT leans heavily into the Spark concept of "partitions", which can be thought of as "workers" when data is
being read or written. The number of partitions used by a command can have a significant impact on performance. 

Try running the same command, but without the `--repartition` argument:

```
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee \
    --path export --compression zip
```

The `./export` directory will have 12 new zip files in it. This count is due to how NT reads data from MarkLogic, 
which involves creating 4 partitions per forest in the MarkLogic database. The example application has 3 forests in its
content database, and thus 3 * 4 = 12 partitions are created, resulting in 12 separate zip files. The `--repartition`
argument - available on every command - thus gives you a way to control how many files are written. 

Note as well that while the above command specifies a collection of documents to export, the `--query` argument accepts
structured queries, serialized CTS queries, and complex queries, either as JSON or XML. You can also use `--stringQuery`
to leverage MarkLogic's search grammar for selecting documents. See (TODO link for exporting docs) for more information.

### Exporting to S3

NT allows for data to be exported easily to S3, with the same approach working for importing data as well. You can 
reference an S3 bucket path via the "s3a://" prefix. The `--s3AddCredentials` argument will then use the AWS SDK to access your
AWS credentials; please see the 
[AWS documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for information on how
to specify your credentials. 

The following shows an example of exporting to S3 with a fictitious bucket name. You can use this with your own S3 
bucket, ensuring that your AWS credentials give you access to writing to the bucket:

```
./bin/nt export_files --clientUri nt-user:password@localhost:8004 \
    --collections employee --compression zip --repartition 1 \
    --path s3a://bucket-name-changeme --s3AddCredentials
```

### Exporting rows

NT allows for exporting rows from MarkLogic via an Optic query and writing the data to a variety of row-oriented 
destinations, such as Parquet files or an RDBMS. The following demonstrates writing rows to Parquet files: 

```
mkdir export/parquet
./bin/nt export_parquet_files --clientUri nt-user:password@localhost:8004 \
    --path export/parquet --query "op.fromView('Example', 'Employees', '')" 
```

You can also export rows via JDBC. Like the example above for importing via JDBC, this is a notional example only. 
Change the details in it to match your database and JDBC driver:

```
./bin/nt export_jdbc --clientUri nt-user:password@localhost:8004 \
    --query "op.fromView('Example', 'Employees', '')" \
    --jdbcUrl "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" \
    --jdbcDriver "org.postgresql.Driver" \
    --table employees
```

## Previewing commands

For many data movement use cases, it can be helpful to see a preview of the data read from a particular source before
any processing occurs to write that data to a destination. NT supports this a `--preview` argument that accepts a 
number of records - technically, Spark rows - to read and display. The following example shows how an export command
can preview 10 rows read from MarkLogic:

```
./bin/nt export_parquet_files --clientUri nt-user:password@localhost:8004 \
    --path export/parquet --query "op.fromView('Example', 'Employees', '')" \
    --preview 10
```

The `--preview` argument works on every command in NT. For example, you can preview an import from a set of Parquet files 
written above without writing any of the data to MarkLogic:

```
mkdir export/parquet
./bin/nt export_parquet_files --clientUri nt-user:password@localhost:8004 \
    --path export/parquet --query "op.fromView('Example', 'Employees', '')" 

./bin/nt import_parquet_files --clientUri nt-user:password@localhost:8004 \
    --path export/parquet --preview 10
```

Note that in the case of previewing an import, NT will show the data as it has been read, which consists of a set of 
Spark rows. The data is not shown as a set of documents yet, as the transformation of rows to documents occurs when 
the data is written to MarkLogic. 

## Reprocessing data

The `reprocess` command in NT allows for custom code - either JavaScript or XQuery - to be executed for selecting and
processing data in MarkLogic. The following shows an example of adding a new collection to each of the employee
documents:

```
./bin/nt reprocess --clientUri nt-user:password@localhost:8004 \
    --readJavascript "cts.uris(null, null, cts.collectionQuery('employee'))" \
    --writeJavascript "declareUpdate(); xdmp.documentAddCollections(URI, 'reprocessed')" 
```

In qconsole, you can see that the 1000 employee documents are now also in the `reprocessed` collection. 

## Copying data

The 'copy' command in NT is similar to the commands for exporting data, but instead allows you to read documents
from one database and write them to another. When copying, you may want to include metadata for each document - 
collections, permissions, quality, properties, and metadata values. This is accomplished via the `--categories`
argument, with `--categories=content,metadata` returning both the document and all of its metadata.

The following shows how to copy the 1000 employee documents to the out-of-the-box Documents database in your 
MarkLogic instance:

```
./bin/nt copy --clientUri nt-user:password@localhost:8004 \
  --collections employee --categories content,metadata \
  --outputClientUri nt-user:password@localhost:8000
```

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

You can download the latest release of the Flux application zip from [the latest Flux release page](https://github.com/marklogic/flux/releases). 
The Flux application zip is titled `marklogic-flux-1.0.0.zip`. You can extract this zip to any location on your 
filesystem that you prefer.

### Deploying the example application

The examples in this guide, along with examples found throughout this documentation, depend on a small MarkLogic 
application that can be deployed to your own instance of MarkLogic server. The application can be downloaded from 
[the latest Flux release page](https://github.com/marklogic/flux/releases) in a zip titled 
`marklogic-flux-getting-started-1.0.0.zip`. To use Flux with this example application, perform the following steps:

1. Extract the `marklogic-flux-getting-started-1.0.0.zip` file to any location on your local filesystem.
2. Run `cd marklogic-flux-getting-started-1.0.0` to change to the directory created by extracting the zip file.
3. Create a file named `gradle-local.properties` and add `mlPassword=your MarkLogic admin user password` to it.
4. Examine the contents of the `gradle.properties` file to ensure that the value of `mlHost` points to your MarkLogic 
server and that the value of `mlRestPort` is a port available for a new MarkLogic app server to use.
5. Run `./gradlew -i mlDeploy` to deploy the example application.

The example application consists of a REST API app server on port 8004 in your MarkLogic installation. 
The application also includes a "flux-example-user" MarkLogic user that has the necessary MarkLogic roles and 
privileges for running the examples in this guide. Finally, the application includes a
[MarkLogic TDE template](https://docs.marklogic.com/guide/app-dev/TDE) that creates a view in MarkLogic for the purpose
of demonstrating commands that utilize a [MarkLogic Optic query](https://docs.marklogic.com/guide/app-dev/OpticAPI).

It is recommended to extract the Flux application zip into the `marklogic-flux-getting-started-1.0.0` directory so that 
you can easily execute the examples in this guide. After extracting the application zip, the directory should have a 
structure similar to this (not all files may be shown):

```
./marklogic-flux-getting-started-1.0.0
    build.gradle
    ./data
    ./marklogic-flux-1.0.0
    ./gradle
    gradle.properties
    gradlew
    gradlew.bat
    ./src  
```

## Usage

You can run Flux without any options to see the list of available commands. If you are using Flux to run these examples, 
first change your current directory to where you extract Flux:

    cd marklogic-flux-1.0.0

And then run the Flux executable without any options:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux
```
{% endtab %}
{% tab log Windows %}
```
bin\flux
```
{% endtab %}
{% endtabs %}

As shown in the usage, every command is invoked by specifying its name and one or more options required to run the
command. To see the usage for a particular command, such as `import-files`, run:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux help import-files
```
{% endtab %}
{% tab log Windows %}
```
bin\flux help import-files
```
{% endtab %}
{% endtabs %}

Required options are marked with an asterisk - "*". Additionally, every command requires that either `--connection-string`
or `--host` and `--port` be specified so that Flux knows which MarkLogic cluster to connect to. 

The `--connection-string` option provides a succinct way of defining the host, port, username, and password when the MarkLogic
app server you connect to requires basic or digest authentication. Its value is of the form 
`(user):(password)@(host):(port)/(optionalDatabaseName)`. For example:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files --connection-string "my-user:my-secret@localhost:8000" ...
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files --connection-string "my-user:my-secret@localhost:8000" ...
```
{% endtab %}
{% endtabs %}

Options can also be read from a file; see the [Common Options](common-options.md) guide for more information.

## Importing data

Flux allows for data to be imported from a variety of data sources, such as a local filesystem, S3, or any database 
accessible via a [JDBC driver](https://docs.oracle.com/javase/tutorial/jdbc/basics/index.html). 
The example project contains a gzipped CSV file generated via 
[Mockaroo](https://mockaroo.com/). Run the below command to load this file; the data loaded will then be used to demonstrate other Flux 
capabilities:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path ../data/employees.csv.gz \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update \
    --collections employee \
    --uri-template "/employee/{id}.json"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path ..\data\employees.csv.gz ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --collections employee ^
    --uri-template "/employee/{id}.json"
```
{% endtab %}
{% endtabs %}

By accessing your [MarkLogic qconsole](https://docs.marklogic.com/guide/qconsole), you can see that the `employee`
collection in the `flux-example-content` database now has 1000 JSON documents, one for each line in the gzipped CSV file. 
Each JSON document has a URI based on the value of the "id" row used to construct the document.

### Importing via JDBC

The `import-jdbc` command in Flux supports reading rows from any database with a supported JDBC driver. Similar to other
tools that support JDBC access, you must first add your database's JDBC driver to the Flux classpath by adding the JDBC
driver jar to the `./ext` directory in the Flux installation. 

The following shows a notional example of reading rows from a Postgres database (this example will not work as it 
requires a separate Postgres database; it is only included for reference):

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-jdbc \
    --jdbc-url "jdbc:postgresql://localhost/dvdrental?user=postgres&password=postgres" \
    --jdbc-driver "org.postgresql.Driver" \
    --query "select * from customer" \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update \
    --collections customer
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-jdbc ^
    --jdbc-url "jdbc:postgresql://localhost/dvdrental?user=postgres&password=postgres" ^
    --jdbc-driver "org.postgresql.Driver" ^
    --query "select * from customer" ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --collections customer
```
{% endtab %}
{% endtabs %}


See the [Import guide](import/import.md) for further details, including how you can aggregate rows together via a SQL join, 
thus producing hierarchical documents with nested data structures.

## Exporting data 

Flux supports several commands for exporting data from MarkLogic, either as documents or rows, to a variety of 
destinations. Commands that export documents support a variety of queries, while commands that export rows use the
[MarkLogic Optic API](https://docs.marklogic.com/guide/app-dev/OpticAPI)
to select rows. The following shows an example of exporting the 1000 employee documents to a single zip file:

{% tabs log %}
{% tab log Unix %}
```
mkdir export
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --path export \
    --compression zip \
    --zip-file-count 1
```
{% endtab %}
{% tab log Windows %}
```
mkdir export
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --path export ^
    --compression zip ^
    --zip-file-count 1
```
{% endtab %}
{% endtabs %}


The above command specifies a collection of documents to export. You can also use the `--query` option to specify a
[structured query](https://docs.marklogic.com/guide/search-dev/structured-query), 
[serialized CTS query](https://docs.marklogic.com/guide/rest-dev/search#id_30577), or 
[complex query](https://docs.marklogic.com/guide/rest-dev/search#id_69918), either as JSON or XML. You can also use 
`--string-query` to leverage MarkLogic's 
[search grammar](https://docs.marklogic.com/guide/search-dev/string-query) for selecting documents. 

The following command shows a collection, a string query, and a structured query used together, resulting 
in 4 JSON documents being written to `./export/employee`:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --string-query Engineering \
    --query '{"query": {"value-query": {"json-property": "job_title", "text": "Junior Executive"}}}' \
    --path export \
    --pretty-print
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --string-query Engineering ^
    --query "{\"query\": {\"value-query\": {\"json-property\": \"job_title\", \"text\": \"Junior Executive\"}}}" ^
    --path export ^
    --pretty-print
```
{% endtab %}
{% endtabs %}


See [the Export guide](export/export.md) for more information.

### Exporting to S3

Flux allows for data to be exported to S3, with the same approach working for importing data as well. You can 
reference an S3 bucket path via the `s3a://` prefix. The `--s3-add-credentials` option will then use the AWS SDK to access your
AWS credentials; please see the 
[AWS documentation](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) for information on how to configure your credentials. 

The following shows an example of exporting to S3 with a fictitious bucket name. You can use this with your own S3 
bucket, ensuring that your AWS credentials give you access to writing to the bucket:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --compression zip \
    --zip-file-count 1 \
    --path s3a://bucket-name-changeme/ \
    --s3-add-credentials
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --compression zip ^
    --zip-file-count 1 ^
    --path s3a://bucket-name-changeme/ ^
    --s3-add-credentials
```
{% endtab %}
{% endtabs %}


### Exporting rows

Flux allows for exporting rows from MarkLogic via an Optic query and writing the data to a variety of row-oriented 
destinations, such as Parquet files or an RDBMS. The following demonstrates writing rows to Parquet files: 

{% tabs log %}
{% tab log Unix %}
```
mkdir export/parquet
./bin/flux export-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --path export/parquet \
    --query "op.fromView('Example', 'Employees', '')" 
```
{% endtab %}
{% tab log Windows %}
```
mkdir export\parquet
bin\flux export-parquet-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --path export/parquet ^
    --query "op.fromView('Example', 'Employees', '')" 
```
{% endtab %}
{% endtabs %}


You can also export rows via JDBC. Like the example above for importing via JDBC, this is a notional example only. 
Change the details in it to match your database and JDBC driver, ensuring that the JDBC driver jar is in the 
`./ext` directory of your Flux installation:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-jdbc \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees', '')" \
    --jdbc-url "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" \
    --jdbc-driver "org.postgresql.Driver" \
    --table employees \
    --mode overwrite
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-jdbc ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --query "op.fromView('Example', 'Employees', '')" ^
    --jdbc-url "jdbc:postgresql://localhost/postgres?user=postgres&password=postgres" ^
    --jdbc-driver "org.postgresql.Driver" ^
    --table employees ^
    --mode overwrite
```
{% endtab %}
{% endtabs %}

## Previewing commands

For many data movement use cases, it can be helpful to see a preview of the data read from a particular source before
any processing occurs to write that data to a destination. Flux supports this via a `--preview` option that accepts a 
number of records to read and display, but without writing the data anywhere. The following example shows how an export 
command can preview 10 rows read from MarkLogic without writing any data to files:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('Example', 'Employees')" \
    --path export/parquet \
    --preview 10
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-parquet-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --query "op.fromView('Example', 'Employees')" ^
    --path export/parquet ^
    --preview 10
```
{% endtab %}
{% endtabs %}


See the [Common Options guide](common-options.md) for more information.

## Reprocessing data

The `reprocess` command in Flux allows for custom code - either JavaScript or XQuery - to be executed for selecting and
processing data in MarkLogic. The following shows an example of adding a new collection to each of the employee
documents:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux reprocess \
    --connection-string "flux-example-user:password@localhost:8004" \
    --read-javascript "cts.uris(null, null, cts.collectionQuery('employee'))" \
    --write-javascript "declareUpdate(); xdmp.documentAddCollections(URI, 'reprocessed')" 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux reprocess ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --read-javascript "cts.uris(null, null, cts.collectionQuery('employee'))" ^
    --write-javascript "declareUpdate(); xdmp.documentAddCollections(URI, 'reprocessed')" 
```
{% endtab %}
{% endtabs %}


In qconsole, you can see that the 1000 employee documents are now also in the `reprocessed` collection. You can also
use Flux and its `--count` option, which allows you to get a count of all the data read by a command without processing or 
writing any of the data:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --path export \
    --collections reprocessed \
    --count 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --path export ^
    --collections reprocessed ^
    --count 
```
{% endtab %}
{% endtabs %}


For more information, please see the [Reprocessing guide](reprocess.md).

## Copying data

The `copy` command in Flux is similar to the commands for exporting data, but instead allows you to read documents
from one MarkLogic database and write them to another MarkLogic. When copying, you may want to include different categories 
of metadata for each document - 
collections, permissions, quality, properties, and metadata values. This is accomplished via the `--categories`
option, with the default value of `content,metadata` returning both the document and all of its metadata.

The following shows how to copy the 1000 employee documents to the out-of-the-box Documents database in your 
MarkLogic instance via the App-Services app server assumed to be listening on port 8000:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux copy \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --output-connection-string "flux-example-user:password@localhost:8000"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux copy ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --output-connection-string "flux-example-user:password@localhost:8000"
```
{% endtab %}
{% endtabs %}

For more information, please see the [Copying guide](copy.md).

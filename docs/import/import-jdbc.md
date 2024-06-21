---
layout: default
title: Importing from JDBC
parent: Importing Data
nav_order: 3
---

Flux supports importing data from any database that offers a JDBC driver. You can select rows from a table or via a 
SQL query, and each row will be written as a new document in MarkLogic. Additionally, you can aggregate related rows
together to form hierarchical documents that are written to MarkLogic. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## JDBC driver installation

To import data from a database, you must obtain the database's JDBC driver JAR file and add it to the `./ext` directory
location in the Flux installation directory. Any JAR file placed in the `./ext` directory is added to the classpath of
Flux.

## Configuring a JDBC connection

The `import-jdbc` command requires that you specify connection details for the database you wish to read from via JDBC.
Connection details are specified via the following options:

- `--jdbc-url` is required and specifies the JDBC connection URL.
- `--jdbc-driver` is required specifies the main class name of the JDBC driver.
- `--jdbc-user` specifies an optional user to authenticate as (this may already be specified via `--jdbc-url`).
- `--jdbc-password` specifies an optional password to authenticate with (this may already be specified via `--jdbc-url`).

## Importing data

To import all rows in a table, use the `--query` option with a SQL query selecting all rows (connection details for 
MarkLogic are omitted for brevity):

    ./bin/flux import-jdbc --query "SELECT * FROM customer" 

The SQL query can contain any syntax supported by your database. 

## Specifying a JSON root name

By default, each column a row will become a top-level field in the JSON document written to
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

## Creating XML documents

To create an XML document for each row instead of a JSON document, include the `--xml-root-name`
option to specify the name of the root element in each XML document. You can optionally include `--xml-namespace` to
specify a namespace for the root element that will then be inherited by every child element as well.

## Aggregating rows

The `import-jdbc` command supports aggregating related rows together to produce hierarchical documents. See
[Aggregating rows](aggregating-rows.md) for more information.

## Advanced options

The `import-jdbc` command reuses Spark's support for reading via a JDBC driver. You can include any of
the [Spark JDBC options](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html) via the `-P` option
to control how JDBC is used. These options are expressed as `-PoptionName=optionValue`.

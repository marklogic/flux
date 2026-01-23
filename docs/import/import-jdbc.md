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

To select data to import, use either the `--query` option with a SQL query, or the `--table` option with a table name.
Support for the `--table` option was added in Flux 1.4.0. 

The first example below shows usage of the `--query` option, which can be any valid SQL query that your database supports:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-jdbc \
    --query "SELECT * FROM customer" \
    --jdbc-url "..." \
    --jdbc-driver "..." \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-jdbc ^
    --query "SELECT * FROM customer" ^
    --jdbc-url "..." ^
    --jdbc-driver "..." ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

The next example shows usage of the `--table` option, which will select all rows from the specified table:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-jdbc \
    --table "customer" \
    --jdbc-url "..." \
    --jdbc-driver "..." \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-jdbc ^
    --table "customer" ^
    --jdbc-url "..." ^
    --jdbc-driver "..." ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

The `--table` option maps to the `dbtable` option for 
[the Spark JDBC data source](https://spark.apache.org/docs/3.5.6/sql-data-sources-jdbc.html). Per the Spark documentation, 
in addition to a table name, you can also specify "anything that is valid in the `FROM` clause of a SQL query... 
such as a subquery in parentheses".

## Specifying a JSON root name

By default, each column a row will become a top-level field in the JSON document written to
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

## Creating XML documents

To create an XML document for each row instead of a JSON document, include the `--xml-root-name`
option to specify the name of the root element in each XML document. You can optionally include `--xml-namespace` to
specify a namespace for the root element that will then be inherited by every child element as well.

## Ignoring null fields

By default, Flux will include any fields in a data source that have a null value 
when creating JSON or XML documents. You can instead ignore fields with a null value
via the `--ignore-null-fields` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-jdbc \
    --ignore-null-fields \
    --query "..." \ 
    etc...
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-jdbc ^
    --ignore-null-fields ^
    --query "..." ^
    etc...
```
{% endtab %}
{% endtabs %}

The decision on whether to include null fields will depend on your application requirements. For example, if your
documents have large numbers of null fields, you may find them to be noise and decide to ignore them. In another case,
it may be important to query for documents that have a particular field with a value of null.

## Aggregating rows

The `import-jdbc` command supports aggregating related rows together to produce hierarchical documents. See
[Aggregating rows](aggregating-rows.md) for more information.

## Generating a TDE template

The `import-jdbc` command supports generating TDE templates to make imported data immediately available for relational
queries. See [TDE template generation](tde-generation.md) for more information.

## Advanced options

The `import-jdbc` command reuses Spark's support for reading via a JDBC driver. You can include any of
the [Spark JDBC options](https://spark.apache.org/docs/3.5.6/sql-data-sources-jdbc.html) via the `--spark-prop` option
to control how JDBC is used. These options are expressed as `--spark-prop optionName=optionValue`.

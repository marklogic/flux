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

In many scenarios, simply reading rows from a single table and creating a document for each row in MarkLogic does not
best utilize the variety of indexing options in MarkLogic. Nor does it allow for data to be stored in hierarchical
structures that better represent complex entities. 

To facilitate producing hierarchical documents with multiple sets of related data, the following options can be used
to combine multiple rows from a SQL query (which typically will include one or more joins) into hierarchical documents:

- `--group-by` specifies a column name to group rows by; this is typically the column used in a join.
- `--aggregate` specifies a string of the form `new_column_name=column1,column2,column3`. The `new_column_name` column
  will contain an array of objects, with each object having columns of `column`, `column2`, and `column3`.

For example, consider the [Postgres tutorial database](https://www.postgresqltutorial.com/postgresql-getting-started/postgresql-sample-database/)
that features 15 tables with multiple relationships. One such relationship is between customers and payments. Depending
on the needs of an application, it may be beneficial for payments to be stored in the related customer document. The
following options would be used to achieve that (connection details are omitted for brevity):

```
./bin/flux import-jdbc \
    --query "select c.*, p.payment_id, p.amount, p.payment_date from customer c inner join payment p on c.customer_id = p.customer_id" \
    --group-by customer_id \
    --aggregate "payments=payment_id,amount,payment_date"
```

The options above result in the following aggregation being performed:

1. Rows retrieved from the Postgres are grouped together based on values in the `customer_id` column.
2. For each payment for a given customer, the values in a payment row are added to a struct that is then added to an array 
in a new column named `payments`.
3. The `payment_id`, `amount`, and `payment_date` columns are removed.

Each customer JSON document will as a result have a top-level `payments` array containing one object for each related
payment. The approach can be used for many joins as well, thus producing multiple top-level array fields containing
related objects.

## Advanced options

The `import-jdbc` command reuses Spark's support for reading via a JDBC driver. You can include any of
the [Spark JDBC options](https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html) via the `-P` option
to control how JDBC is used. These options are expressed as `-PoptionName=optionValue`.

---
layout: default
title: Importing from JDBC
parent: Importing Data
nav_order: 3
---

NT supports importing data from any database that offers a JDBC driver. You can select rows from a table or via a 
SQL query, and each row will be written as a new document in MarkLogic. Additionally, you can aggregate related rows
together to form hierarchical documents that are written to MarkLogic. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## JDBC driver installation

To import data from a database, you must obtain the database's JDBC driver JAR file and add it to the `./ext` directory
location in the NT installation directory. Any JAR file placed in the `./ext` directory is added to the classpath of 
NT.

## Importing from a table

## Importing via a query

## Aggregating rows

TODO, but the options:

- `--groupBy` specifies a column name to group rows by; this is typically the column used in a join.
- `--aggregate` specifies a string of the form `new_column_name=column1;column2;column3`. The `new_column_name` column
  will contain an array of objects, with each object having columns of `column`, `column2`, and `column3`.

Example:

```
./bin/nt import_jdbc \
    --jdbcUrl "jdbc:postgresql://localhost/dvdrental?user=postgres&password=postgres" \
    --jdbcDriver "org.postgresql.Driver" \
    --query "select c.customer_id, c.first_name, p.payment_id, p.amount, p.payment_date from customer c inner join public.payment p on c.customer_id = p.customer_id" \
    --groupBy customer_id \
    --aggregate "payments=payment_id;amount;payment_date" \
    --clientUri "new-tool-user:password@localhost:8004" \
    --permissions nt-role,read,nt-role,update \
    --collections customer
```

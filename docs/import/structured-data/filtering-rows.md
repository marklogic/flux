---
layout: default
title: Filtering rows
parent: Structured data sources
grand_parent: Importing Data
nav_order: 1
---

When importing from structured data sources, you can filter which rows are imported using SQL-like WHERE expressions. 
This allows you to selectively import data without modifying your source files or database queries.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Supported commands

Row filtering is available for the following commands:

- [`import-avro-files`](../import-files/avro.md)
- [`import-delimited-files`](../import-files/delimited-text.md)
- [`import-jdbc`](../import-jdbc.md)
- [`import-orc-files`](../import-files/orc.md)
- [`import-parquet-files`](../import-files/parquet.md)

## Usage

Use the `--where` option with a SQL-like expression to filter rows. The expression is evaluated against each row, and 
only rows where the expression evaluates to `true` are imported.

For example, to import only customers with a specific status:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path customers.csv \
    --where "status = 'active'" \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path customers.csv ^
    --where "status = 'active'" ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

## Expression syntax

The `--where` option accepts any valid SQL WHERE clause expression supported by Apache Spark, including comparison operators, logical operators, pattern matching, null checks, and more.

For example, to filter by a numeric range:

```
--where "age >= 18 AND age < 65"
```

For complete SQL expression syntax and additional examples, see the [Apache Spark SQL WHERE clause reference](https://spark.apache.org/docs/latest/sql-ref-syntax-qry-select-where.html).

## Combining with other features

The `--where` filter is applied before [row aggregation](aggregating-rows.md), allowing you to filter data before grouping. This is useful when you want to aggregate only a subset of your source data.

For example:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-jdbc \
    --query "SELECT c.*, p.payment_id, p.amount FROM customer c INNER JOIN payment p ON c.customer_id = p.customer_id" \
    --where "amount > 10.0" \
    --group-by customer_id \
    --aggregate "payments=payment_id,amount" \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-jdbc ^
    --query "SELECT c.*, p.payment_id, p.amount FROM customer c INNER JOIN payment p ON c.customer_id = p.customer_id" ^
    --where "amount > 10.0" ^
    --group-by customer_id ^
    --aggregate "payments=payment_id,amount" ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

In this example, only payments with an amount greater than $10 are included in each customer's `payments` array.

## Performance considerations

Filtering rows with `--where` is processed by Apache Spark after reading the data from the source. For optimal performance:

- When using `import-jdbc`, consider using SQL WHERE clauses in your `--query` option to filter data at the database level before Spark processes it.
- For file-based sources (Avro, ORC, Parquet), the `--where` filter is applied during Spark's data processing phase and can take advantage of file-level optimizations depending on the file format.

## Reference

For complete SQL expression syntax, see the [Apache Spark SQL reference](https://spark.apache.org/docs/latest/sql-ref-syntax-qry-select-where.html).

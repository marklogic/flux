---
layout: default
title: Aggregating rows
parent: Importing Data
nav_order: 5
---

When importing from tabular data sources, Flux provides support for aggregating rows together to produce hierarchical
JSON or XML documents that often better resemble the logical entities associated with the documents. Flux supports
this capability for each of the following data sources:

- [Avro](import-files/avro.md)
- [Delimited text](import-files/delimited-text.md)
- [JDBC](import-jdbc.md)
- [ORC](import-files/orc.md)
- [Parquet](import-files/parquet.md)

As an example, consider a scenario where you are importing customer data from a relational database via JDBC. Each 
customer record can have one or more related address records, which are stored in a separate table. You could simply
import all customer rows and all address rows as separate documents in MarkLogic. But for a variety of use cases - including
searching and security - you may want each customer document to contain all of its related addresses. Flux allows you
to achieve that goal by aggregating related data such that hierarchical documents are written to MarkLogic containing 
one or more arrays of related records. 

## Usage

To aggregate rows together when using `import-avro-files`, `import-delimited-files`, `import-jdbc`, `import-orc-files`, 
or `import-parquet-files`, use the following options:

- `--group-by` specifies a column name to group rows by; this is typically the column used in a join.
- `--aggregate` specifies a string of the form `new_column_name=column1,column2,column3`. The `new_column_name` column
  will contain an array of objects, with each object having columns of `column1`, `column2`, and `column3`.

For example, consider the [Postgres tutorial database](https://www.postgresqltutorial.com/postgresql-getting-started/postgresql-sample-database/)
that features 15 tables with multiple relationships. One such relationship is between customers and payments. The
following options would be used to write customer documents with each customer document containing an array of 
its related payments:

```
./bin/flux import-jdbc \
    --query "select c.*, p.payment_id, p.amount, p.payment_date from customer c inner join payment p on c.customer_id = p.customer_id" \
    --group-by customer_id \
    --aggregate "payments=payment_id,amount,payment_date"
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```

The options above result in the following aggregation being performed:

1. Rows retrieved from the Postgres database are grouped together based on values in the `customer_id` column.
2. For each payment for a given customer, the values in a payment row - `payment_id`, `amount`, and `payment_date` - 
are added to a struct that is then added to an array in a new column named `payments`.
3. The `payment_id`, `amount`, and `payment_date` columns are removed.

Each customer JSON document will as a result have a top-level `payments` array containing one object for each related
payment, as shown in the example below:

```
{
  "customer_id": 1, 
  "first_name": "Mary", 
  "payments": [
    {
      "payment_id": 18495, 
      "amount": 5.99, 
      "payment_date": "2007-02-15T04:22:38.996Z"
    }, 
    {
      "payment_id": 18496, 
      "amount": 0.99, 
      "payment_date": "2007-02-15T21:31:19.996Z"
    }
  ]
}
```

The approach can be used for many joins as well, thus producing multiple top-level array fields containing
related objects. You are restricted to a single `--group-by`, but you can include many `--aggregate` options, one for
each join that you wish to aggregate rows from. 

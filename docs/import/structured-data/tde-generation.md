---
layout: default
title: Generating a TDE template
parent: Structured data sources
grand_parent: Importing Data
nav_order: 3
redirect_from:
  - /import/tde-generation.html
---

Flux 1.4.0 supports automatically generating [Template Driven Extraction (TDE)](https://docs.marklogic.com/guide/app-dev/TDE) templates during data import operations. TDE templates enable MarkLogic to extract row data from documents, making that data available through
[SQL queries](https://docs.progress.com/bundle/marklogic-server-model-relational-data-12/page/topics/intro.html)
or via the [MarkLogic Optic API](https://docs.marklogic.com/guide/app-dev/OpticAPI).

When importing structured data (such as CSV files, database tables, Parquet files, etc.), Flux can automatically analyze the data schema and generate a corresponding TDE template. This eliminates the manual process of creating and loading TDE templates and ensures your data is immediately available for relational querying.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Supported commands

TDE generation is available for the following import commands that work with structured data:

- [`import-avro-files`](import-files/avro.md)
- [`import-delimited-files`](import-files/delimited-text.md)
- [`import-jdbc`](import-jdbc.md)
- [`import-orc-files`](import-files/orc.md)
- [`import-parquet-files`](import-files/parquet.md)

TDE generation is also available for the `custom-import` command, though its usefulness will be determined 
by the data imported from your custom data source. 

## Security requirements

Flux uses the MarkLogic [tde.templateBatchInsert](https://docs.marklogic.com/tde.templateBatchInsert) function 
to load a TDE into the schemas database associated with your application. 

If you are not connecting as a user with the MarkLogic `admin` role, your MarkLogic user must meet the following requirements in order to generate and load a TDE template:

- Must have the `xdmp-eval`, `xdmp-eval-in`, and `xdbc-eval` privileges in order to use the [v1/eval REST API endpoint](https://docs.marklogic.com/REST/POST/v1/eval).
- Must have the `unprotected-uri` or `any-uri` privilege in order to write a TDE document.
- Must have either the `tde-admin` role or the `any-collection` privilege in order to write a TDE document into the required collection.

Additionally, if your user does not have the `admin` role, you must specify at least one update permission via the 
`--tde-permissions` option described below.

The examples in this guide use the `flux-example-user` from the [Getting Started guide](../getting-started.md), and 
thus each example includes the `--tde-permissions` option.

## Basic usage

To generate and load a TDE template, you must specify both a schema name and view name using the `--tde-schema` and `--tde-view` options. 

The generated template defaults to a TDE context path of `/`. MarkLogic requires additional scope to be defined, which 
must be done via one of the following options:

- `--tde-collections` - comma-delimited list of collection names to add to the template.
- `--tde-directory` - database directory path to add to the template.
- `--tde-context` - an XPath expression defining a custom context, thus overriding the default context of `/`. 

The following example shows a basic usage of the TDE generation feature:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
  --path ../data/employees.csv.gz \
  --connection-string "flux-example-user:password@localhost:8004" \
  --collections employees \
  --permissions flux-example-role,read,flux-example-role,update \
  --tde-schema hr \
  --tde-view employees \
  --tde-collections employees \
  --tde-permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
  --path ..\data\employees.csv.gz ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --collections employees ^
  --permissions flux-example-role,read,flux-example-role,update ^
  --tde-schema hr ^
  --tde-view employees ^
  --tde-collections employees ^
  --tde-permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

The above command will:

1. Import the CSV data as JSON documents.
2. Generate a TDE template based on the CSV column structure.
3. Load the template with a URI of `/tde/hr.employees.json` into your application's schemas database.
4. Make the imported data that matches the TDE template immediately available for relational queries.

### Preview mode

You can include the `--tde-preview` option to generate and display the TDE template without loading it into MarkLogic.
The generated template will be displayed in the console output, allowing you to review and customize it as needed. 
Additionally, no data will be imported when using this option. 

## Incremental write support

When using [incremental write](../incremental-write.md) to efficiently update documents, you can include 
the `--tde-support-incremental-write` option to ensure your generated TDE template includes columns for tracking 
incremental write state. This allows you to use the same TDE template for incremental write and 
for querying the data in each document via relational queries.

### Usage example

When combining TDE generation with incremental write, you will typically specify the schema and view names 
twice - once for TDE generation and once for incremental write:

```
--incremental-write \
--tde-support-incremental-write \
--tde-schema hr \
--tde-view employees \
--incremental-write-schema hr \
--incremental-write-view employees
```

The `--tde-schema` and `--tde-view` options control the generated TDE template, while 
`--incremental-write-schema` and `--incremental-write-view` tell the incremental write feature which 
TDE view to query for existing document hashes. A future release of Flux may allow for the incremental write 
feature to default to the schema and view specified for the TDE generation feature when using 
`--tde-support-incremental-write`.

### Generated columns

When `--tde-support-incremental-write` is specified, Flux will add two columns to the beginning of the generated TDE template:

1. A `uri` column that captures the document URI.
2. A hash column (defaulting to `incrementalWriteHash`) that captures the hash value stored in document metadata.

For example, a generated TDE template with incremental write support will include columns similar to:

```json
"columns" : [ {
  "name" : "uri",
  "scalarType" : "string",
  "val" : "xdmp:node-uri(.)"
}, {
  "name" : "incrementalWriteHash",
  "scalarType" : "unsignedLong",
  "val" : "xdmp:node-metadata-value(., 'incrementalWriteHash')",
  "nullable" : true
}, {
  // Your data columns...
```

The `uri` column is not nullable, ensuring every row has a document URI. The hash column is nullable since 
documents may not have been written with incremental write enabled.

### Custom hash key name

If you have specified a custom hash key name via the `--incremental-write-hash-name` option, the generated 
TDE template will use that name for both the column name and the metadata key reference. For example, 
with `--incremental-write-hash-name myCustomHash`, the hash column will be:

```json
{
  "name" : "myCustomHash",
  "scalarType" : "unsignedLong",
  "val" : "xdmp:node-metadata-value(., 'myCustomHash')",
  "nullable" : true
}
```

See the [incremental write documentation](../incremental-write.md) for more information on using incremental 
write to efficiently update documents.

## Template customization

### Document type

By default, Flux will generate a JSON TDE template. You can generate an XML TDE template instead via the following 
option:

```
--tde-document-type xml
```

### Template URI

By default, Flux will load a TDE template with the following URI:

```
/tde/(schema name).(view name).(xml or json)
```

You can specify a custom URI via the following:

```
--tde-uri /my/custom/tde.json
```

### Template permissions

As noted above, if you are not connecting as a user with the MarkLogic `admin` role, you will need
to specify at least one update permission. Permissions are applied on the TDE document in your application's
schemas database. You can specify permissions via the `--tde-permissions` option, which accepts a 
comma-delimited list of MarkLogic role names and capabilities. For example:

```
--tde-permissions rest-reader,read,rest-writer,update
```

### Template state

In some scenarios, you may want to generate and load a TDE template but not have it be enabled yet. You can load 
the template in a disabled state by including the following option:

```
--tde-template-disabled
```

When disabled, the TDE template will not be applied to any data matching its scope. 

### View customization

By default, Flux does not include a `view-layout` value, resulting in MarkLogic defaulting to 
[a view layout](https://docs.progress.com/bundle/marklogic-server-model-relational-data-12/page/topics/creating-template-views.html#) of `identical`. You can override this via the following option:

```
--tde-view-layout sparse
```

For MarkLogic 12 users, Flux does not specify a `view-virtual` value, resulting in MarkLogic defaulting to 
[a non-virtual view](https://docs.progress.com/bundle/marklogic-server-model-relational-data-12/page/topics/creating-template-views.html#). You can override this via the following option:

```
--tde-view-virtual
```

The above option will result in a "virtual" view in the TDE template where data is indexed at query time instead of 
when the data is loaded. 

### Column customization

Flux provides options for customizing individual columns in the generated TDE template. 
Please see the [MarkLogic documentation](https://docs.progress.com/bundle/marklogic-server-model-relational-data-12/page/topics/creating-template-views.html#columns)
for further details on column definitions. 

Each option can be specified multiple times and has a value of the form `(column name)=(value)`. The values includes
in the table below are examples only and should be altered based on your own column names and requirements.

| Option | Description |
| --- | --- |
| `--tde-column-val customer_id=customerId` | Override the `val` value for a column. |
| `--tde-column-type customer_id=string` | Override the `scalarType` value for a column. |
| `--tde-column-default customer_id=123` | Provide a default value for the column if null for a given row. |
| `--tde-column-virtual customer_id` | If set for a column, the TDE template will extract content at query time instead of when data is indexed. |
| `--tde-column-nullable customer_id` | Allow null values for a column. |
| `--tde-column-invalid-values customer_id=ignore` | Configure whether invalid values are ignored or rejected. |
| `--tde-column-reindexing customer_id=visible` | Configure the visibility of a column during reindexing. |
| `--tde-column-permissions customer_id=role1,role2` | Comma-delimited list of role names required for read access to column values. |
| `--tde-column-collation customer_id=http://marklogic.com/collation/codepoint` | Override the collation for a column of type `string`. |

For a column of type `vector` in MarkLogic 12.0 or higher, the following options can also be configured:

| Option | Description |
| --- | --- |
| `--tde-column-dimension vector_col=384` | Defines the size of valid vectors for the column. |
| `--tde-column-ann-compression vector_col=0.5` | Floating point value between 0.0 and 1.0 that determines the size of indexed vectors. |
| `--tde-column-ann-distance vector_col=cosine` | As of MarkLogic 12.0, only "cosine" is supported as a value, but future versions may support additional values. |
| `--tde-column-ann-indexed vector_col=true` | Controls the indexing of the vector column. |


## Integration with document structure options

When using `--json-root-name` or `--xml-root-name`, Flux will default the TDE context path based on the option's
value. In either scenario, you are not required to specify a collection or directory as the context path provides sufficient 
scope for MarkLogic. 

For JSON, an option of `--json-root-name employee` will result in a TDE template with a context value of `/employee`. 

For XML, Flux will also take into account the value of `--xml-namespace` if specified. For example, given the following 
options:

```
--xml-root-name employee --xml-namespace org:example
```

The TDE template will have a context of `/ns1:employee`. The namespace prefix `ns1` will be associated with 
the namespace `org:example` via a TDE path namespace. Each column will also have a `val` value prefixed with 
`ns1:`. 

The following shows the beginning of a TDE template based on the above options:

```
{
  "template" : {
    "context" : "/ns1:employee",
    "pathNamespace" : [ {
      "prefix" : "ns1",
      "namespaceUri" : "org:example"
    } ],
    "rows" : [ {
      "schemaName" : "hr",
      "viewName" : "employees",
      "columns" : [ {
        "name" : "employee_id",
        "scalarType" : "int",
        "val" : "ns1:employee_id"
      },
      etc...
```

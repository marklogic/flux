---
layout: default
title: Importing Data
nav_order: 2
---

This will eventually document each of the commands for importing data into MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Importing files

NT can import files as documents into MarkLogic via the `import_files` command. The command requires one or more
`--path` options to specify from where files should be read. Each file will be read as-is into MarkLogic, optionally
transformed via a MarkLogic REST server transform. 

### Path expressions

The value of the `--path` option can be any directory or specific file. 

By default, child directories of each directory specified by `--path` are included. To disable this, include the following
option:

    --recursiveFileLookup false

You can restrict which files are read from a directory by specifying a standard 
[glob expression](https://en.wikipedia.org/wiki/Glob_(programming)) via the `--filter` option:

    --filter some*.json

### Compressed files

NT can import files as documents from ZIP and GZIP files by setting the `--compression` option to `zip` or `gzip`.

For a ZIP file, NT will import every entry as a separate document, using the entry name to generate the document URI.

For a GZIP file, NT will decompress the file and import it as-is.

### Controlling how documents are written

The following sections describe the NT options for controlling how documents are written when importing from files.

#### URI generation

The URI for each document is based on the path of the file. The following options affect how a URI is generated for
each file:


| Option | Description | 
| --- | --- |
| --uriPrefix | A prefix to apply to each URI. |
| --uriSuffix | A suffix to apply to each URI. |
| --uriReplace | Comma-delimited list of regular expressions and replacement values. |
| --uriTemplate | Template for each URI containing one or more column names; only intended for row-oriented data sources. |

You will typically want to use `--uriReplace` to remove much of the file path from the URI, though this is not required.
For example, if you read files from a path of `/path/to/my/data` and you only want to include `/data` in your URIs,
you would set the following option:

    --uriReplace ".*/data,/data"

For import commands that read from row-oriented data sources including delimited files, JSON Lines files, Avro files, 
ORC files, or Parquet files, you can use `--uriTemplate` to specify a complete URI with one or more column names 
replaced with the corresponding values for each document:

    --uriTemplate "/employee/{id}/{last_name}.json"

A URI template is typically only relevant for structured data sources as NT knows the column names and values for every
row in the data source. NT does not know this information when ingesting XML and JSON files as-is, and thus a URI 
template will not be helpful.

### Document metadata

More to come, but the relevant options:

| Option | Description | 
| --- | --- |
| --collections | Comma-delimited list of collection names to add to each document. |
| --permissions | Comma-delimited list of MarkLogic role names and capabilities - e.g. rest-reader,read,rest-writer,update. |
| --temporalCollection | Name of a MarkLogic temporal collection to assign to each document. |

### Transforming document content

More to come, but the relevant options:

| Option | Description | 
| --- | --- |
| --transform | Name of a MarkLogic REST transform to apply to the document before writing it. |
| --transformParams | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| --transformParamsDelimiter | Delimiter for `--transformParams`; typically set when a value contains a comma. |

### Handling errors

If NT fails to write a batch of documents to MarkLogic, it defaults to stopping execution of the command and displaying
the error. To configure NT to only log errors and keep writing documents, set `--abortOnFailure false` as an option.

Currently, NT is only aware that a batch of documents failed. A future enhancement will allow for configuring NT to
attempt to write each document individually in a failed batch to both minimize the number of failed documents and 
identify which documents failed. 

### Specialized file types

NT can import the following "specialized" files, where each file can produce one or more - and often many - documents
in MarkLogic.

#### Aggregate XML

`import_aggregate_xml_files` and specify the `--element` and optionally `--namespace` elements. Supports ZIP and GZIP
compression as well.

#### Avro

`import_avro_files` with `-P` used to specify [Avro options](https://spark.apache.org/docs/latest/sql-data-sources-avro.html).

#### Delimited files

`import_delimited_files` with -P used to specify [CSV options](https://spark.apache.org/docs/latest/sql-data-sources-csv.html).

#### JSON Lines files

`import_json_lines_files` with -P used to specify [JSON Lines options](https://spark.apache.org/docs/latest/sql-data-sources-json.html).

#### ORC files

`import_orc_files` with `-P` used to specify [ORC options](https://spark.apache.org/docs/latest/sql-data-sources-orc.html).

#### Parquet files

`import_parquet_files` with `-P` used to specify [Parquet options](https://spark.apache.org/docs/latest/sql-data-sources-parquet.html).

### Import from S3

NT can import all the file types described above from S3 as well as a local filesystem. A path expression for 
referencing an S3 bucket must be of the form `s3a://bucket-name/optional/path`. 

For most cases, NT must use your AWS credentials to access an S3 bucket. NT uses the AWS SDK to fetch your credentials
from one of the many locations supported by the AWS SDK. To enable this, include the `--s3AddCredentials` option:

```
./bin/nt import_files \
    --path "s3a://my-bucket/some/path"
    --s3AddCredentials
```

## Importing from JDBC

TODO, but key point is your database's JDBC driver must be added to the `./ext` directory in NT.

### Aggregating rows

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

## Tuning performance

When writing to MarkLogic, the two main settings within NT that affect performance (i.e. ignoring how the MarkLogic 
cluster itself is configured, such as index settings and number of hosts) are the batch size - the number of documents 
sent in a request to MarkLogic - and the number of threads used to send requests to MarkLogic.

Batch size is configured via the `--batchSize` option, which defaults to a value of 200. Depending on the size of 
your documents, you may find improved performance by raising this value significantly for smaller documents, such as 500
or even 1000. 

For the number of threads used to send requests to MarkLogic, two factors come into play. The product of the 
number of partitions and the value of the `--threadCount` option determines how many total threads will be used to send
requests. For example, if the import command uses 4 partitions to read data and `--threadCount` is set to 4 (its 
default value), 16 total threads will send requests to MarkLogic. 

The number of partitions is determined by how data is read and differs across the various import commands. 
NT will log the number of partitions for each import command as shown below:

    INFO com.marklogic.spark: Number of partitions: 1

You can force a number of partitions via the `--repartition` option. 

### Thread count and cluster size

**You should take care** not to exceed the number of requests that your MarkLogic cluster can reasonably handle at a 
given time. A general rule of thumb is not to use more threads than the number of hosts multiplied by the number of 
threads per app server. A MarkLogic app server defaults to a limit of 32 threads. So for a 3-host cluster, you should
not exceed 96 total threads. This also assumes that each host is receiving requests. This would be accomplished either 
by placing a load balancer in front of MarkLogic or by configuring direct connections as described below. 

The rule of thumb can thus be expressed as:

    Number of partitions * Value of --threadCount <= Number of hosts * number of app server threads

### Direct connections to each host

TODO `--connectionType direct` (which then enables the underlying DMSDK support for direct connections to hosts).



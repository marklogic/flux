---
layout: default
title: Common import features
parent: Importing Data
nav_order: 1
---

Regardless from where an NT import command reads data, it will ultimately write one or more documents to MarkLogic.
The sections below detail the common features for writing documents that are available for every import command, unless
otherwise noted by the documentation for that command.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Controlling document URIs

Each import command will generate an initial URI for each document, typically based on the data source from which the 
command reads. You can use the following command line options to control the URI for each document:

| Option | Description | 
| --- | --- |
| --uriPrefix | A prefix to apply to each URI. |
| --uriSuffix | A suffix to apply to each URI. |
| --uriReplace | Comma-delimited list of regular expressions and replacement values, with each replacement value surrounded by single quotes. |
| --uriTemplate | Template for each URI containing one or more column names. |

### Replacing URI contents

When importing data from files where the initial URI is based on an absolute file path, the `--uriReplace` option can 
be used to remove much of the file path from the URI, though this is not required. For example, if you import files 
from a path of `/path/to/my/data` and you only want to include `/data` in your URIs, you would include the following 
option:

    --uriReplace ".*/data,'/data'"

### Configuring URIs via a template

The `--uriTemplate` option allows you to configure a URI based on a JSON representation of each row that a command
reads from its associated data source. This option is supported for the following commands:

- `import_avro_files`
- `import_delimited_files`
- `import_files`, but only for JSON files and JSON entries in zip files.
- `import_jdbc`
- `import_json_files`
- `import_orc_files`
- `import_parquet_files`

By default, each of the above commands will write each record that it reads as a JSON document to MarkLogic. A URI 
template is applied against that JSON representation of each record. This is true even when electing to write XML 
documents to MarkLogic instead. 

A URI template is a string consisting of one or more expressions surrounded by single braces. Each expression must refer
to either a top-level field name in the JSON representation of a record, or it must be a 
[JSON Pointer expression](https://www.rfc-editor.org/rfc/rfc6901) that points to a non-empty value in the JSON representation.

For example, consider an employee data source where the JSON representation of each record from that data source has 
top-level fields of `id` and `last_name`. You could configure a URI for each document using the following option:

    --uriTemplate "/employee/{id}/{last_name}.json"

A JSON Pointer expression is useful in conjunction with the optional `--jsonRootName` option for defining a root field
name in each JSON document. For example, using the above example, you may want each employee document to have a single
root field of "employee" so that each document is more self-describing. The URI template will be evaluated against a
JSON document with this root field applied, so you would need to use JSON Pointer expressions to refer to the `id` and 
`last_name` values:

    --jsonRootName employee --uriTemplate "/employee/{/employee/id}/{/employee/last_name}.json"

## Configuring document metadata

When writing documents, you can optionally configure any number of 
[collections](https://docs.marklogic.com/guide/search-dev/collections), any number of 
[permissions](https://docs.marklogic.com/11.0/guide/security-guide/en/protecting-documents.html), and a 
[temporal collection](https://docs.marklogic.com/guide/temporal/intro). Collections are useful for organizing documents
into related sets and provide a convenient mechanism for restricting queries. You will typically want to configure at 
least one set of `read` and `update` permissions for your documents to ensure that non-admin users can access your data.
A temporal collection is only necessary when leveraging MarkLogic's support for querying bi-temporal data. 

Each of the above types of metadata can be configured via the following options:

| Option | Description | 
| --- | --- |
| --collections | Comma-delimited list of collection names to add to each document. |
| --permissions | Comma-delimited list of MarkLogic role names and capabilities - e.g. `rest-reader,read,rest-writer,update`. |
| --temporalCollection | Name of a MarkLogic temporal collection to assign to each document. |

The following shows an example of each option:

```
--collections employees,imported-data \
--permissions my-reader-role,read,my-writer-role,update \
--temporalCollection my-temporal-data
```

## Transforming content

For each import command, you can apply a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
to each document before it is written. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| --transform | Name of a MarkLogic REST transform to apply to the document before writing it. |
| --transformParams | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| --transformParamsDelimiter | Delimiter for `--transformParams`; typically set when a value contains a comma. |

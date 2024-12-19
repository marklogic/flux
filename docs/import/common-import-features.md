---
layout: default
title: Common import features
parent: Importing Data
nav_order: 1
---

Each Flux import command will write one or more documents to MarkLogic, regardless of the data source. 
The sections below detail the common features for writing documents that are available for every import command, unless
otherwise noted by the documentation for that command.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Controlling document URIs

Each import command will generate an initial URI for each document, typically based on the data source from which the 
command reads. The following command line options offer further control over each URI:

| Option | Description | 
| --- | --- |
| `--uri-prefix` | A prefix to apply to each URI. |
| `--uri-suffix` | A suffix to apply to each URI. |
| `--uri-replace` | Comma-delimited list of regular expressions and replacement values, with each replacement value surrounded by single quotes. |
| `--uri-template` | Template for each URI containing one or more column names. |

### Replacing URI contents

The `--uri-replace` option supports replacing one or more parts of an initial URI. Each part is identified by a 
regular expression, and the replacement for each part is surrounded in single quotes. Replacing parts of the URI 
is often useful when importing data from files where the initial URI is based on an absolute file path. For example, 
if you import files from a path of `/path/to/my/data` and you only want to include `/data` in your URIs, you would 
include the following option:

    --uri-replace ".*/data,'/data'"

### Configuring URIs via a template

The `--uri-template` option supports configuring a URI based on a JSON representation of each record that a command
reads from its associated data source. This option is supported for the following commands:

- `import-aggregate-json-files`
- `import-avro-files`
- `import-delimited-files`
- `import-files`, but only for JSON files and JSON entries in ZIP files.
- `import-jdbc`
- `import-orc-files`
- `import-parquet-files`

By default, each of the above commands will write each record that it reads as a JSON document to MarkLogic. A URI 
template is applied against that JSON representation of each record. This is true even when electing to write XML 
documents to MarkLogic instead. 

A URI template is a string containing any text you wish to include in every URI along with one or more expressions 
surrounded by single braces. Each expression must refer to either a top-level field name in the JSON representation of 
a record, or it must be a 
[JSON Pointer expression](https://www.rfc-editor.org/rfc/rfc6901) that points to a non-empty value in the JSON representation.

For example, consider an employee data source where the JSON representation of each record from that data source has 
top-level fields of `id` and `last_name`. You could configure a URI for each document using the following option:

    --uri-template "/employee/{id}/{last_name}.json"

A JSON Pointer expression is useful in conjunction with the optional `--json-root-name` option for defining a root field
name in each JSON document. For example, using the above example, you may want each employee document to have a single
root field of "employee" so that each document is more self-describing. The URI template will be evaluated against a
JSON document with this root field applied, so you would need to use JSON Pointer expressions to refer to the `id` and 
`last_name` values:

    --json-root-name employee --uri-template "/employee/{/employee/id}/{/employee/last_name}.json"

The following techniques can assist you with writing a URI template:

1. Run the import command with `--limit 1` to write a single JSON document to MarkLogic. You can then see the JSON 
fields that can be referenced in your template.
2. Run the import command with `--preview 1` to see a tabular representation of a single record read from the command's 
data source. This also helps you understand the fields that can be referenced in your template.
3. Consider using an [options file](../common-options.md), as the inclusion of sequences such as `"{/` can be 
mis-interpreted by some shell environments. 

## Configuring document metadata

When writing documents, you can configure any number of 
[collections](https://docs.marklogic.com/guide/search-dev/collections), any number of 
[permissions](https://docs.marklogic.com/11.0/guide/security-guide/en/protecting-documents.html), and a 
[temporal collection](https://docs.marklogic.com/guide/temporal/intro). Collections are useful for organizing documents
into related sets and provide a convenient mechanism for queries. You will typically want to configure at 
least one set of `read` and `update` permissions for your documents to ensure that non-admin users can access your data.
A temporal collection is only necessary when leveraging MarkLogic's support for querying bi-temporal data. 

Each of the above types of metadata can be configured via the following options:

| Option | Description | 
| --- | --- |
| `--collections` | Comma-delimited list of collection names to add to each document. |
| `--permissions` | Comma-delimited list of MarkLogic role names and capabilities - e.g. `rest-reader,read,rest-writer,update`. |
| `--temporal-collection` | Name of a MarkLogic temporal collection to assign to each document. |

The following shows an example of each option:

```
--collections employees,imported-data \
--permissions my-reader-role,read,my-writer-role,update \
--temporal-collection my-temporal-data
```

## Building a RAG data pipeline

[Retrieval-augmented generation](https://www.progress.com/marklogic/solutions/generative-ai), or RAG, with MarkLogic depends on preparing data so that the most relevant 
chunks of text for a user's question can be sent to a Large Language Model, or LLM. Starting with release 1.2.0, Flux 
supports the construction of a data pipeline by splitting the text in a document into chunks and adding a vector 
embedding to each chunk while importing data. Please see [the guide on splitting text](splitting.md) and 
[the guide on adding embeddings](embedder/embedder.md) for more information.

## Transforming content

For each import command, you can apply a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
to each document before it is written. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| `--transform` | Name of a MarkLogic REST transform to apply to the document before writing it. |
| `--transform-params` | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| `--transform-params-delimiter` | Delimiter for `--transform-params`; typically set when a value contains a comma. |

The following shows an example of each option:

```
--transform my-transform
--transform-params param1;value1;param2;value2
--transform-params-delimiter ;
```

The above link for REST transforms includes instructions on manually installing a transform. If you are using
[ml-gradle to deploy an application to MarkLogic](https://github.com/marklogic/ml-gradle), you can let ml-gradle 
[automatically install your transform](https://github.com/marklogic/ml-gradle/wiki/How-modules-are-loaded).

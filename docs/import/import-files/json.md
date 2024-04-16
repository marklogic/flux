---
layout: default
title: Importing JSON
parent: Importing files
grand_parent: Importing Data
nav_order: 5
---

NT can import JSON files - both files containing JSON objects and arrays and also files conforming to the 
[JSON Lines format](https://jsonlines.org/).

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import_json_files` command is used to read JSON files and write the contents of each file as one or more JSON
documents in MarkLogic. If a file contains a single JSON object, it will be written as a single document to MarkLogic.
If a file contains an array of JSON objects, each object will be written as a separate document to MarkLogic. To avoid
this behavior for an array of JSON objects, use the `import_files` command instead.

You must specify at least one `--path` option along with connection information for the MarkLogic database you wish to write to:

    ./bin/nt import_json_files --path /path/to/files --clientUri "user:password@localhost:8000"

## Importing JSON Lines files

If your files conform to the JSON Lines format, include the `--jsonLines` option with no value. NT will then attempt
to read each line as a separate JSON object and write it to MarkLogic as a document.

## Specifying a JSON root name

It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field in the JSON documents written by
`import_json_files, use the `--jsonRootName` option with a value for the name of the root field. The data read from a 
row will then be nested under this root field.

## Advanced options

The `import_json_files` command reuses Spark's support for reading JSON files. You can include any of
the [Spark JSON options](https://spark.apache.org/docs/latest/sql-data-sources-json.html) via the `-P` dynamic option
to control how JSON content is read. Dynamic options are expressed as `-PoptionName=optionValue`.

To support the more common use case of reading files containing JSON objects and arrays, the command defaults to setting
the `multiLine` option to `true`. The `--jsonLines` option is thus effectively a shortcut for changing the `multiLine`
option to have a value of `false`.
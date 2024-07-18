---
layout: default
title: Importing JSON
parent: Importing files
grand_parent: Importing Data
nav_order: 4
---

Flux provides special handling for JSON files that either conform to the
[JSON Lines format](https://jsonlines.org/) or contain arrays of objects, where each object should become a separate 
document in MarkLogic. If you wish to import JSON files as-is, you may find it simpler to 
[import them as generic files](generic-files.md) instead. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-json-files` command defaults to writing a single document to MarkLogic if a file contains a
JSON object and writing multiple documents to MarkLogic if a file contains an array of JSON objects. If you would 
rather a file with an array of object be written as a single document, use the `import-files` command instead.

You must specify at least one `--path` option along with connection information for the MarkLogic database you wish to write to:

```
./bin/flux import-json-files \
    --path /path/to/files \
    --connection-string "user:password@localhost:8000"
```

## Importing JSON Lines files

If your files conform to the [JSON Lines format](https://jsonlines.org/), 
include the `--json-lines` option with no value. Flux will then read each line as a separate JSON object and 
write it to MarkLogic as a JSON document.

## Specifying a JSON root name

It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field in the JSON documents written by
`import-json-files`, use the `--json-root-name` option with a value for the name of the root field. The data read from a 
row will then be nested under this root field.

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your files use a different encoding, you must specify that via the `--encoding` option so that
the content can be correctly translated to UTF-8 when written to MarkLogic:

```
./bin/flux import-json-files \
    --path source \
    --encoding ISO-8859-1 \
    etc...
```

## Advanced options

The `import-json-files` command reuses Spark's support for reading JSON files. You can include any of
the [Spark JSON options](https://spark.apache.org/docs/latest/sql-data-sources-json.html) via the `-P` option
to control how JSON content is read. These options are expressed as `-PoptionName=optionValue`.

For example, if your files use a format other than `yyyy-MM-dd` values, you can specify that format via the following:

```
./bin/flux import-json-files \
    --path source \
    -PdateFormat=MM-dd-yyyy
```

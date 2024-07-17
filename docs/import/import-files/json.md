---
layout: default
title: Importing JSON
parent: Importing files
grand_parent: Importing Data
nav_order: 4
---

Flux can import JSON files - both files containing JSON objects and arrays and also files conforming to the 
[JSON Lines format](https://jsonlines.org/).

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-json-files` command reads JSON files and writes the contents of each file as one or more JSON
documents in MarkLogic. If a file contains a single JSON object, it will be written as a single document to MarkLogic.
If a file contains an array of JSON objects, each object will be written as a separate document to MarkLogic. To avoid
this behavior for an array of JSON objects, use the `import-files` command instead which loads files without any 
additional processing.

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

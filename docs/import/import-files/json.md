---
layout: default
title: Importing aggregate JSON
parent: Importing files
grand_parent: Importing Data
nav_order: 4
---

Flux provides special handling for JSON "aggregate" files that either conform to the
[JSON Lines format](https://jsonlines.org/) or contain arrays of objects. If you wish to import JSON files as-is, 
you may find it simpler to [import them as generic files](generic-files.md) instead. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-aggregate-json-files` command defaults to writing multiple documents to MarkLogic if a file contains an 
array of JSON objects and writing a single document if a file contains a JSON object. If you would 
rather a file with an array of objects be written as a single document, use the `import-files` command instead.

You must specify at least one `--path` option along with connection information for the MarkLogic database 
you wish to write to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-json-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-json-files ^
    --path path\to\files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


The URI of each document will default to a UUID followed by `.json`. To include the file path at the start of the URI,
include the `--uri-include-file-path` option. You can also make use of the
[common import features](../common-import-features.md) for controlling document URIs.

## Importing JSON Lines files

If your files conform to the [JSON Lines format](https://jsonlines.org/), 
include the `--json-lines` option with no value. Flux will then read each line in each file as a separate JSON object 
and write it to MarkLogic as a JSON document.

For example, consider a file with the following content:

```
{"first": "george", "last": "washington"}
{"id": 12345, "price": 8.99, "in-stock": true}
```

The file can be imported with the following notional command:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-json-files \
    --json-lines \
    --path path/to/file.txt \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-json-files ^
    --json-lines ^
    --path path\to\file.txt ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


Flux will write two separate JSON documents, each with a completely different schema. 

The JSON Lines format is often useful for exporting data from MarkLogic as well. Please see 
[this guide](../../export/export-rows.md) for more information on exporting data to JSON Lines files. 

## Specifying a JSON root name

It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field in the JSON documents, 
use the `--json-root-name` option with a value for the name of the root field. The data read from a 
row will then be nested under this root field.

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your files use a different encoding, you must specify that via the `--encoding` option so that
the content can be correctly translated to UTF-8 when written to MarkLogic:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-json-files \
    --path source \
    --encoding ISO-8859-1 \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-json-files ^
    --path source ^
    --encoding ISO-8859-1 ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


## Reading compressed files

Flux will automatically read files compressed with GZIP when they have a filename ending in `.gz`; you do not need to
specify a compression option. As noted in the "Advanced options" section below, you can use `-Pcompression=` to
explicitly specify a compression algorithm if Flux is not able to read your compressed files automatically.

## Advanced options

The `import-aggregate-json-files` command reuses Spark's support for reading JSON files. You can include any of
the [Spark JSON options](https://spark.apache.org/docs/latest/sql-data-sources-json.html) via the `-P` option
to control how JSON content is read. These options are expressed as `-PoptionName=optionValue`.

For example, if your files use a format other than `yyyy-MM-dd` values, you can specify that format via the following:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-json-files \
    --path source \
    -PdateFormat=MM-dd-yyyy \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-json-files ^
    --path source ^
    -PdateFormat=MM-dd-yyyy ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

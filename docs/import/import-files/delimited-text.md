---
layout: default
title: Importing delimited text
parent: Importing files
grand_parent: Importing Data
nav_order: 3
---

Flux can import rows from delimited text files, with each row being written as a JSON or XML document to MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-delimited-files` command imports delimited text files specified via one or more occurrences of the 
`--path` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path path\to\files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

The command uses a comma as the default delimiter. You can override this via `--delimiter`:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path /path/to/files \
    --delimiter ; \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path path\to\files ^
    --delimiter ; ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

The URI of each document will default to a UUID followed by `.json`. To include the file path at the start of the URI,
include the `--uri-include-file-path` option. You can also make use of the
[common import features](../common-import-features.md) for controlling document URIs.

## Specifying a JSON root name

By default, each column in a delimited text file will become a top-level field in a JSON document written to 
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

For example, including an option of `--json-root-name Customer` will produce JSON documents with the following format:

```
{
    "Customer": {
        "field1": "value1",
        etc...
    }
}
```

## Creating XML documents

To create an XML document for each row in a delimited text file instead of a JSON document, include the `--xml-root-name`
option to specify the name of the root element in each XML document. You can optionally include `--xml-namespace` to 
specify a namespace for the root element that will then be inherited by every child element as well.

For example, including `--xml-root-name Customer --xml-namespace "org:example"` in the options will produce XML 
documents with the following format:

```
<Customer xmlns="org:example">
    <field1>value1</field1>
    etc...
</Customer>
```

## Ignoring null fields

By default, Flux will include any fields in a delimited text file that have a null value (this does not include
a value that has whitespace) when creating JSON or XML documents. You can instead ignore fields with a null value
via the `--ignore-null-fields` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path /path/to/files \
    --delimiter ; \
    --ignore-null-fields \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path path\to\files ^
    --delimiter ; ^
    --ignore-null-fields ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

The decision on whether to include null fields will depend on your application requirements. For example, if your
documents have large numbers of null fields, you may find them to be noise and decide to ignore them. In another case,
it may be important to query for documents that have a particular field with a value of null.

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your delimited text files use a different encoding, you must specify that via the `--encoding` option so that
the content can be correctly translated to UTF-8 when written to MarkLogic - e.g.:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-text-files \
    --path source \
    --encoding ISO-8859-1 \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-text-files ^
    --path source ^
    --encoding ISO-8859-1 ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

## Aggregating rows

The `import-delimited-files` command supports aggregating related rows together to produce hierarchical documents. See
[Aggregating rows](../aggregating-rows.md) for more information.

## Generating a TDE template

The `import-delimited-files` command supports generating TDE templates to make imported data immediately available for relational
queries. See [TDE template generation](../tde-generation.md) for more information.


## Reading compressed files

Flux will automatically read files compressed with gzip when they have a filename ending in `.gz`; you do not need to
specify a compression option. As noted in the "Advanced options" section below, you can use `-Pcompression=` to
explicitly specify a compression algorithm if Flux is not able to read your compressed files automatically.

## Importing delimited files with different columns

If you wish to import 2 or more delimited files that have different columns, you will typically achieve better results
by importing each file one at a time via Flux. This is due to the underlying Spark file reader needing to construct
a schema based on the complete set of files. A future release of Flux may allow for this effect to be achieved by running 
Flux once and processing each file separately.

## Advanced options

The `import-delimited-files` command reuses Spark's support for reading delimited text data. You can include any of
the [Spark CSV options](https://spark.apache.org/docs/3.5.6/sql-data-sources-csv.html) via the `-P` option
to control how delimited text is read. These options are expressed as `-PoptionName=optionValue`.

A common option to include is `-PmultiLine=true` for when your files have rows with values that include newline 
symbols. 

The command defaults to setting the `header` option to `true` and the
`inferSchema` option to `true`. You can override those two options or include additional Spark CSV options - for
example:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    -Pheader=false \
    -PescapeQuotes=false \
    --connection-string "flux-example-user:password@localhost:8004" \
    --path path/to/files \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    -Pheader=false ^
    -PescapeQuotes=false ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --path path\to\files ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

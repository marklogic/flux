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

```
./bin/flux import-delimited-files \
    --path /path/to/files \
    --connection-string "user:password@localhost:8000" etc...
```

The command uses a comma as the default delimiter. You can override this via `--delimiter`:

```
./bin/flux import-delimited-files \
    --path /path/to/files --delimiter ; \
    --connection-string "user:password@localhost:8000" etc...
```

## Specifying a JSON root name

By default, each column in a delimited text file will become a top-level field in a JSON document written to 
MarkLogic. It is often useful to have a single "root" field in a JSON document so that it is more self-describing. It
can help with indexing purposes in MarkLogic as well. To include a JSON root field, use the `--json-root-name` option with
a value for the name of the root field. The data read from a row will then be nested under this root field.

For example, including an option of `--json-root-name` will produce JSON documents with the following format:

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

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your delimited text files use a different encoding, you must specify that via the `--encoding` option so that
the content can be correctly translated to UTF-8 when written to MarkLogic - e.g.:

```
./bin/flux import-delimited-text-files \
    --path source \
    --encoding ISO-8859-1 \
    etc...
```

## Advanced options

The `import-delimited-files` command reuses Spark's support for reading delimited text data. You can include any of
the [Spark CSV options](https://spark.apache.org/docs/latest/sql-data-sources-csv.html) via the `-P` option
to control how delimited text is read. These options are expressed as `-PoptionName=optionValue`.

The command defaults to setting the `header` option to `true` and the
`inferSchema` option to `true`. You can override those two options or include additional Spark CSV options - for
example:

    ./bin/flux import-delimited-files -Pheader=false -PescapeQuotes=false ....

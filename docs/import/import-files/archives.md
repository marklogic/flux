---
layout: default
title: Importing archives
parent: Importing files
grand_parent: Importing Data
nav_order: 7
---

Flux can import archive files containing documents and their associated metadata. This includes archives written via 
the [`export-archive-files` command](../../export/export-archives.md) as well as archives written by 
[MarkLogic Content Pump](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/exporting-content-from-marklogic-server/exporting-to-an-archive.html), 
which are hereafter referred to as "MLCP archives".

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-archive-files` command will import the documents and metadata files in a ZIP file produced by the 
`export-archive-files` command. You must specify at least one `--path` option along with connection information for the
MarkLogic database you wish to write to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-archive-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-archive-files ^
    --path path\to\files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

## Importing MLCP archives

You can also import 
[MLCP archives](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/exporting-content-from-marklogic-server/exporting-to-an-archive.html)
that were produced via the `EXPORT` command in MLCP. The `import-mlcp-archive-files` command is used instead, and it also
requires at least one `--path` option along with connection information for the MarkLogic database you wish to write to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-mlcp-archive-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-mlcp-archive-files ^
    --path /path/to/files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


## Restricting metadata

By default, all metadata associated with a document in an archive will be included when the document is written to MarkLogic. 
This is true for both the `import-archive-files` command and the `import-mlcp-archive-files` command. This is typically 
desirable so that metadata like collections and permissions in the archive can be applied to the imported documents. 

You can instead restrict which types of metadata are included via the `--categories` option. This option accepts a comma-delimited
sequence of the following metadata types:

- `collections`
- `permissions`
- `quality`
- `properties`
- `metadatavalues`

For example, the following option will only include the collections and properties found in each metadata entry in an 
archive ZIP file or MLCP archive ZIP file:

    --categories collections,properties

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your archive files use a different encoding, you must specify that via the `--encoding` option so that
the content can be correctly translated to UTF-8 when written to MarkLogic - e.g.:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-archive-files \
    --path source \
    --encoding ISO-8859-1 \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-archive-files ^
    --path source ^
    --encoding ISO-8859-1 ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

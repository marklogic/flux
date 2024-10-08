---
layout: default
title: Exporting archives
parent: Exporting Data
nav_order: 4
---

Flux can export documents with their metadata as "archive files" - ZIP files that contain an entry for each document
and another entry for the XML metadata file associated with each document. Archive files can then be imported via 
the `import-archive-files` command, providing a convenient mechanism for storing data and later importing it into a separate
database.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `export-archive-files` command requires a query for selecting documents to export and a directory path for writing 
archive files to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-archive-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-archive-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections example ^
    --path destination
```
{% endtab %}
{% endtabs %}


The following options control which documents are selected to be exported:

| Option | Description | 
| --- |--- |
| `--collections` | Comma-delimited sequence of collection names. |
| `--directory` | A database directory for constraining on URIs. |
| `--options` | Name of a REST API search options document; typically used with a string query. |
| `--query` | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| `--string-query` | A string query utilizing MarkLogic's search grammar. |
| `--uris` | Newline-delimited sequence of document URIs to retrieve.  |

You must specify at least one of `--collections`, `--directory`, `--query`, `--string-query`, or `--uris`. You may specify any
combination of those options as well, with the exception that `--query` will be ignored if `--uris` is specified.

You must then use the `--path` option to specify a directory to write archive files to.

### Windows-specific issues with ZIP files

In the likely event that you have one or more URIs with a forward slash - `/` - in them, then creating a ZIP file
with those URIs - which are used as the zip entry names - will produce confusing behavior on Windows. If you open the
ZIP file via Windows Explorer, Windows will erroneously think the file is empty. If you open the file using
7-Zip, you will see a top-level entry named `_` if one or more of your URIs begin with a forward slash. These are
effectively issues that only occur when viewing the file within Windows and do not reflect the actual contents of the
ZIP file. The contents of the file are correct and if you were to import them with Flux via the `import-archive-files`
command, you will get the expected results.


## Controlling document metadata

Each exported document will have all of its associated metadata - collections, permissions, quality, properties, and 
metadata values - included in an XML document in the archive ZIP file. You can control which types of metadata are
included with the `--categories` option. This option accepts a comma-delimited sequence of the following metadata types:

- `collections`
- `permissions`
- `quality`
- `properties`
- `metadatavalues`

If the option is not included, all metadata will be included. 

## Exporting consistent results

By default, Flux uses MarkLogic's support for
[point-in-time queries](https://docs.marklogic.com/11.0/guide/app-dev/point_in_time#id_47946) when querying for
documents, thus ensuring a [consistent snapshot of data](https://docs.marklogic.com/guide/java/data-movement#id_18227).
Point-in-time queries depend on the same MarkLogic system timestamp being used for each query. Because system timestamps
can be deleted when MarkLogic [merges data](https://docs.marklogic.com/11.0/guide/admin-guide/en/understanding-and-controlling-database-merges.html),
you may encounter the following error that causes an export command to fail:

```
Server Message: XDMP-OLDSTAMP: Timestamp too old for forest
```

To resolve this issue, you must
[enable point-in-time queries](https://docs.marklogic.com/11.0/guide/app-dev/point_in_time#id_32468) for your database
by configuring the `merge timestamp` setting. The recommended practice is to
[use a negative value](https://docs.marklogic.com/11.0/guide/admin-guide/en/understanding-and-controlling-database-merges/setting-a-negative-merge-timestamp-to-preserve-fragments-for-a-rolling-window-of-time.html)
that exceeds the expected duration of the export operation. For example, a value of `-864,000,000,000` for the merge
timestamp would give the export operation 24 hours to complete.

Alternatively, you can disable the use of point-in-time queries by including the following option:

```
--no-snapshot
```

The above option will not use a snapshot for queries but instead will query for data at multiple points in time. As
noted above in the guide for [consistent snapshots](https://docs.marklogic.com/guide/java/data-movement#id_18227), you
may get unpredictable results if your query matches on data that changes during the export operation. If your data is
not changing, this approach is recommended as it avoids the need to configure merge timestamp.

## Transforming document content

You can apply a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
to each document before it is written to an archive. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| `--transform` | Name of a MarkLogic REST transform to apply to the document before writing it. |
| `--transform-params` | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| `--transform-params-delimiter` | Delimiter for `--transform-params`; typically set when a value contains a comma. |

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
You can specify an alternate encoding when exporting archives via the `--encoding` option - e.g.:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-archives \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination \
    --encoding ISO-8859-1
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-archives ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections example ^
    --path destination ^
    --encoding ISO-8859-1
```
{% endtab %}
{% endtabs %}


The encoding will be used for both document and metadata entries in each archive ZIP file. 

## Exporting large binary files 

Similar to [exporting large binary documents as files](export-documents.md), you can include large binary documents
in archives by including the `--streaming` option introduced in Flux 1.1.0. When this option is set, Flux will stream 
each document from MarkLogic directly to a ZIP file, thereby avoiding reading the contents of a file into memory.

As streaming to an archive requires Flux to retrieve one document at a time from MarkLogic, you should not use this option
when exporting smaller documents that can easily fit into the memory available to Flux.

When using `--streaming`, the following options will behave in a different fashion:

- `--batch-size` will still affect how many URIs are retrieved from MarkLogic in a single request, but will not impact
  the number of documents retrieved from MarkLogic in a single request, which will always be 1.
- `--encoding` will be ignored as applying an encoding requires reading the document into memory.
- `--pretty-print` will have no effect as the contents of a document will never be read into memory.

You typically will not want to use the `--transform` option as applying a REST transform in MarkLogic to a
large binary document may exhaust the amount of memory available to MarkLogic.

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

## Importing large binary files in archives

When [exporting archives](../../export/export-archives.md), you can use the `--streaming` option introduced in Flux 
1.1.0 to ensure that large binary documents in MarkLogic can be streamed to an archive file. When importing archives
with large binary files, you should likewise use the `--streaming` option to ensure that each large binary can be read
into MarkLogic without exhausting the memory available to Flux or MarkLogic.

As streaming each entry requires Flux to only send one document at a time to MarkLogic, you should not use this option when
importing smaller files that easily fit into the memory available to Flux.

When using `--streaming`, the following options will have no effect due to Flux not reading the file contents into
memory and always sending one file per request to MarkLogic:

- `--batch-size`
- `--encoding`
- `--failed-documents-path`
- `--uri-template`

You typically will also not want to use the `--transform` option as applying a REST transform in MarkLogic to a very
large binary document may exhaust the amount of memory available to MarkLogic.

In addition, when streaming documents to MarkLogic, URIs will be encoded. For example, an entry named `/my file.json`
will result in a URI of `/my%20file.json`. This is due to an
[issue in the MarkLogic REST API endpoint](https://docs.marklogic.com/REST/PUT/v1/documents) that will be resolved in
a future server release. 

### Applying transforms selectively when streaming

When streaming archive entries to MarkLogic, you may need to apply a REST transform to certain documents but not 
others. One reason for this is when an archive contains files that appear to be JSON, XML, or text based on their URI 
extensions (such as `.json`, `.xml`, or `.txt`), but are stored in the archive with a binary format. As noted
in the [MarkLogic REST API documentation](https://docs.progress.com/bundle/marklogic-server-develop-rest-api-12/page/topics/intro.html#id_53367),
loading such documents as binaries requires the use of a REST transform that converts each document into a binary. This
approach prevents MarkLogic from automatically setting the document type based on the URI extension. The following 
REST transform is one example of how a JSON or XML document can be converted to a binary:

```
xquery version "1.0-ml";

module namespace transform = "http://marklogic.com/rest-api/transform/toBinary";

declare function transform($context as map:map, $params as map:map, $content as document-node()) as document-node()
{
    let $node := $content/node()
    let $enc := xdmp:base64-encode(xdmp:quote($node))
    let $bin := xs:hexBinary(xs:base64Binary($enc))
    return document { binary { $bin } }
};
```

The `--streaming-transform-binary-with-extension` option, introduced in Flux 2.1, allows you to specify which 
documents should be sent to your transform during streaming. This allows you to stream as many documents as possible, 
with only the documents that need to be converted into binaries being sent to the transform. The option accepts a 
comma-delimited list of URI extensions. The transform specified via `--transform` will then only be applied if 
both of the following conditions are met for a document in the archive:

1. The document format in the archive is `BINARY` (requires an archive created with Flux 2.1 or later).
2. The document URI ends with one of the specified extensions.

For example, consider an archive containing:

- `/data/report.json` with format `BINARY` (needs transform)
- `/data/config.json` with format `JSON` (no transform needed)
- `/data/document.xml` with format `BINARY` (needs transform)
- `/data/image.png` with format `BINARY` (no transform needed)

The following set of options will result in the transform only being applied to  `report.json` and `document.xml`:

```
--streaming 
--transform my-transform
--streaming-transform-binary-with-extension json,xml
```

This option only has an effect when `--streaming` and `--transform` are specified. Note that if `--streaming`
and `--transform` are specified without this option, then every document will be sent to the transform. This is 
typically not desirable as it results in each document being ready into memory in MarkLogic, which defeats some of the 
purpose of streaming the data into MarkLogic.

## Common errors

If you use Flux 1.0.x to import an archive created by Flux 1.1.x or higher, you may receive an error containing the
following message:

```
com.marklogic.spark: Could not find metadata entry for entry
```

To solve this, you should use Flux 1.1.0 or higher to import the archive. Flux 1.1.0 and higher can also import 
archives created by Flux 1.0.x. 

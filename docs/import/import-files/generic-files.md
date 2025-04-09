---
layout: default
title: Importing generic files
parent: Importing files
grand_parent: Importing Data
nav_order: 2
---

Flux can import any type of file as-is, with the contents of the file becoming a new document in MarkLogic. The term 
"generic files" is used in this context to refer to files that do not require any special processing
other than potentially decompressing the files.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-files` command imports a set of files into MarkLogic, with each file being written as a separate
document. You must specify at least one `--path` option along with connection information for the MarkLogic database
you wish to write to. For example:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path path\to\files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


## Controlling document URIs

Each document will have an initial URI based on the absolute path of the associated file. See 
[common import features](../common-import-features.md) for details on adjusting this URI. In particular, the 
`--uri-replace` option is often useful for removing most of the absolute path to produce a concise, self-describing
URI. 

## Specifying a document type

The type of each document written to MarkLogic is determined by the file extension found in the URI along with the
set of [MIME types configured in MarkLogic](https://docs.marklogic.com/admin-help/mimetype). For unrecognized file
extensions, or URIs that do not have a file extension, you can force a document type via the `--document-type` option.
The value of this option must be one of `JSON`, `XML`, or `TEXT`.

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your files use a different encoding, you must specify that via the `--encoding` option so that
the content can be correctly translated to UTF-8 when written to MarkLogic:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path source \ 
    --encoding ISO-8859-1 \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path source ^
    --encoding ISO-8859-1 ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


## Importing large binary files

Flux can leverage MarkLogic's [support for large binary documents](https://docs.marklogic.com/guide/app-dev/binaries#id_93203)
by importing binary files of any size. To ensure that binary files of any size can be loaded, consider using the
`--streaming` option introduced in Flux 1.1.0. When this option is set, Flux will stream the contents of each file from
its source directly into MarkLogic, thereby avoiding reading the contents of a file into memory. 

As streaming a file requires Flux to only send one document at a time to MarkLogic, you should not use this option when
importing smaller files that easily fit into the memory available to Flux.

When using `--streaming`, the following options will have no effect due to Flux not reading the file contents into 
memory and always sending one file per request to MarkLogic:

- `--batch-size`
- `--encoding`
- `--failed-documents-path`
- `--uri-template`

You typically will also not want to use the `--transform` option as applying a REST transform in MarkLogic to a very 
large binary document may exhaust the amount of memory available to MarkLogic.

In addition, when streaming documents to MarkLogic, URIs will be encoded. For example, a file named `my file.json` 
will result in a URI of `/my%20file.json`. This is due to an 
[issue in the MarkLogic REST API endpoint](https://docs.marklogic.com/REST/PUT/v1/documents) that will be resolved in 
a future server release. 

## Importing gzip files

To import gzip files with each file being decompressed before written to MarkLogic, include the `--compression` option
with a value of `GZIP`. You can also import gzip files as-is - i.e. without decompressing them - by not including the
`--compression` option. The `--streaming` option introduced in Flux 1.1.0 can also be used for very large gzip files
that may not fit into the memory available to Flux or to MarkLogic.

## Importing ZIP files

To import each entry in a ZIP file as a separate document, include the `--compression` option with a value of `ZIP`.
Each document will have an initial URI based on both the absolute path of the ZIP file and the name of the ZIP entry. 
You can also use the `--document-type` option as described above to force a document type for any entry that has a file
extension not recognized by MarkLogic. The `--streaming` option introduced in Flux 1.1.0 can also be used for ZIP files
containing very large binary files that may not fit into the memory available to Flux or to MarkLogic.

## Extracting text

As of Flux 1.3.0, text can be extracted from files via [Apache Tika](https://tika.apache.org/) and written as separate 
documents in MarkLogic. This is typically useful when importing binary content such as PDF and Word files, where both 
the binary file and extracted text can be stored in MarkLogic.

Text extraction is enabled by including the following option when executing the `import-files` command:

    --extract-text

When included, Flux will extract text from each file and create a separate document based on the following default
behavior:

1. The document will be JSON with a URI equalling that of the binary document URI plus `-extracted-text.json`.
2. The document will inherit any permissions assigned to the binary document.
3. The document will not inherit any collections assigned to the binary document.
4. The document will have keys of `source-uri`, `content`, and `extracted-metadata`. 

The `content` in the document contains the extracted text. The `extracted-metadata` contains each metadata key and value
produced by Apache Tika. 

If you do not want Flux to write the file to a separate document but only want the extracted text document written, 
include the following option:

    --extracted-text-drop-source

### Writing extracted text XML documents

Extracted text documents can be written as XML instead of JSON by including the following option:

    --extracted-text-document-type XML

When writing Tika metadata to the extracted text document, Flux will attempt to determine a well-known namespace based
on the name of the metadata key. For example, a Tika metadata key that begins with `pdf:` will result in an element 
assigned to the `http://ns.adobe.com/pdf/1.3/` namespace.

### Assigning metadata to extracted text documents

Collections can be assigned to extracted text documents via the following option, which accepts a comma-delimited list 
of collection names:

    --extracted-text-collections collection1,collection2

Permissions can be assigned to extracted text documents via the following options, which accepts a comma-delimited 
sequence of MarkLogic role names and capabilities:

    --extracted-text-permissions role1,read,role2,update


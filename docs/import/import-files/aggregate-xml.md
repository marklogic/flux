---
layout: default
title: Importing aggregate XML
parent: Importing files
grand_parent: Importing Data
nav_order: 4
---

Flux can split large XML files - called "aggregate XML files" - in the same fashion as 
[MarkLogic Content Pump](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/importing-content-into-marklogic-server/splitting-large-xml-files-into-multiple-documents.html). 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import_aggregate_xml_files` command supports creating many XML documents based on a particular XML element name and
optional namespace. In addition to a least one required `--path` option and connection information for the MarkLogic 
database you wish to write to, you must specify the `--element` option to identify the name of the XML element that 
will be used as the root of an XML document written to MarkLogic. The `--namespace` option is used if that XML element
has an associated namespace:

```
./bin/flux import_aggregate_xml_files --path /path/to/files --connectionString user:password@localhost:8000 \
    --element employee --namespace org:example
```

## Controlling document URIs

In addition to the options for controlling URIs described in the [common import features guide](../common-import-features.md), 
you can use the `--uriElement` and `--uriNamespace` options to identify an element in each XML document whose value should
be included in the URI:

```
./bin/flux import_aggregate_xml_files --path /path/to/files --connectionString user:password@localhost:8000 \
    --element employee --namespace org:example \
    --uriElement employee ID --namespace org:example
```

You may still wish to use options like `--uriPrefix` and `--uriSuffix` to make the URI more self-describing. 

## Compressed XML files

Flux supports Gzip and ZIP aggregate XML files. Simply include the `--compression` option with a value of `GZIP` or 
`ZIP`. 

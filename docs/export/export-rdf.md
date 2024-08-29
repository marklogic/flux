---
layout: default
title: Exporting RDF data
parent: Exporting Data
nav_order: 5
---

Flux can export semantic triples to a variety of RDF file formats, allowing you to easily exchange large numbers of 
triples and quads with other systems and users. 

## Table of contents
{: .no_toc .text-delta }

- TOC 
{:toc}

## Usage

The `export-rdf-files` command requires a query for selecting documents to export and a directory path for writing
RDF files to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-rdf-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-rdf-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections example ^
    --path destination
```
{% endtab %}
{% endtabs %}

Similar to [exporting documents](export-documents.md), the `export-rdf-files` command supports the following 
options for selecting the documents that contain the triples you wish to export:

| Option | Description | 
| --- |--- |
| `--collections` | Comma-delimited sequence of collection names. |
| `--directory` | A database directory for constraining on URIs. |
| `--graphs` | Comma-delimited sequence of MarkLogic graph names. |
| `--options` | Name of a REST API search options document; typically used with a string query. |
| `--query` | A structured, serialized CTS, or combined query expressed as JSON or XML. |
| `--string-query` | A string query utilizing MarkLogic's search grammar. |
| `--uris` | Newline-delimited sequence of document URIs to retrieve.  |

You must specify at least one of `--collections`, `--directory`, `--graphs`, `--query`, `--string-query`, or `--uris`. 
You may specify any combination of those options as well, with the exception that `--query` will be ignored 
if `--uris` is specified.

For each document matching the query specified by your options above, Flux will retrieve the triples from the document 
and write them to a file. You must specify a `--path` option for where files should be written. See 
[Specifying a path](specifying-path.md) for more information on paths.

By default, Flux will write files using the [standard Turtle or TTL format](https://www.w3.org/TR/turtle/). You can 
change this via the `--format` option, with the following choices supported:

- `nq` = [N-Quads](https://www.w3.org/TR/n-quads/)
- `nt` = [N-Triples](https://en.wikipedia.org/wiki/N-Triples)
- `rdfthrift` = [RDF Binary](https://afs.github.io/rdf-thrift/)
- `trig` = [TriG](https://www.w3.org/TR/trig/)
- `trix` = [Triples in XML](https://en.wikipedia.org/wiki/TriX_(serialization_format))
- `ttl` = [Turtle](https://www.w3.org/TR/turtle/), the default format.

## Specifying the number of files to write

The `--file-count` option controls how many files are written by Flux. The default equals the number of partitions 
used for reading triples from MarkLogic, which is controlled by the `--partitions-per-forest` option, which has a 
default value of 4. For example, if the database you are querying has 3 forests, Flux will write 12 files by default.

## Specifying a base IRI

With the `--base-iri` option, you can specify a base IRI to prepend to the graph associated with each triple when 
the graph is relative. If the graph for a triple is absolute, the base IRI will not be prepended. 

## Overriding the graph

For some use cases involving exporting triples with their graphs to files containing quads, it may not be desirable to
reference the graph that each triple belongs to in MarkLogic. You can use `--graph-override` to specify an alternative
graph value that will then be associated with every triple that Flux writes to a file. 

## GZIP compression

To compress each file written by Flux using GZIP, simply include `--gzip` as an option.

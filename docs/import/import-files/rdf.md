---
layout: default
title: Importing RDF
parent: Importing files
grand_parent: Importing Data
nav_order: 6
---

NT can import a variety of RDF files in a fashion similar to that of 
[MarkLogic Content Pump](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/importing-content-into-marklogic-server/loading-triples.html).

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import_rdf_files` command is used read RDF data from a variety of RDF file types and write one or more 
[managed triples documents](https://docs.marklogic.com/guide/semantics/loading) in MarkLogic. Each managed triples 
document is an XML document containing up to 100 semantic triples. 

To import RDF files, you must specify at least one `--path` option along with connection information for the MarkLogic 
database you wish to write to:

    ./bin/nt import_rdf_files --path /path/to/files --clientUri "user:password@localhost:8000"

## Supported files types

NT supports the same [RDF data formats](https://docs.marklogic.com/guide/semantics/loading#id_70682) as
MarkLogic server does, which are listed below:

- RDF/JSON
- RDF/XML
- N3
- N-Quads
- N-Triples
- TriG
- Turtle

## Specifying a graph

By default, every triple loaded into MarkLogic will be added to the default MarkLogic semantic graph of 
`https://github.com/marklogic/spark-etl/pull/73#pullrequestreview-2004167620`. Each RDF quad is loaded into the graph
specified within the quad.

To specify a different graph for every triple (which will not apply to quads), use the `--graph` option. 

To specify a graph for both triples and quads - thus overriding the graph associated with each quad - use the 
`--graphOverride` option. 

Note that the set of collections specified via the `--collections` option does not have any impact on the graph. You 
are free to specify as many collections as you want in addition to the graph you choose via `--graph` or 
`--graphOverride`. 

## Compressed files

NT supports Gzip and ZIP RDF files. Simply include the `--compression` option with a value of `GZIP` or `ZIP`. 

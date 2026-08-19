---
layout: default
title: Importing RDF
parent: Importing files
grand_parent: Importing Data
nav_order: 6
---

Flux can import a variety of RDF files in a fashion similar to that of
[MarkLogic Content Pump](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/importing-content-into-marklogic-server/loading-triples.html).

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-rdf-files` command reads RDF data from a variety of RDF file types and writes one or more
[managed triples documents](https://docs.marklogic.com/guide/semantics/loading) in MarkLogic.
Each managed triples document is an XML document containing up to 100 semantic triples.

To import RDF files, you must specify at least one `--path` option along with connection information for the MarkLogic
database you wish to write to:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-rdf-files \
    --path /path/to/files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-rdf-files ^
    --path path\to\files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


## Supported files types

Flux supports the same [RDF data formats](https://docs.marklogic.com/guide/semantics/loading#id_70682) as
MarkLogic server does, which are listed below:

| Format  | File Extension |
|---------|----------------|
| RDF/XML | .rdf or .xml |
| RDF/JSON | .json |
| N3 | .n3 |
| N-Quads | .nq |
| N-Triples | .nt |
| TriG | .trig |
| Turtle | .ttl |

Flux will attempt to load each file specified via `--path` by determining the format of the RDF data based on the
file extension.

## Specifying a graph

By default, every triple loaded into MarkLogic will be added to the default MarkLogic semantic graph of
`http://marklogic.com/semantics#default-graph`. Each RDF quad is loaded into the graph specified within the quad.

To specify a different graph for every triple, use the `--graph` option. This option only affects triples and not quads.

To specify a graph for both triples and quads - thus overriding the graph associated with each quad - use the
`--graph-override` option.

Note that the set of collections specified via the `--collections` option does not have any impact on the graph. You
are free to specify as many collections as you want in addition to the graph you choose via `--graph` or
`--graph-override`.

## Compressed files

Flux supports gzip and ZIP RDF files. Simply include the `--compression` option with a value of `GZIP` or `ZIP`.

## Zip bomb protection

When importing compressed RDF files from untrusted sources using `--compression ZIP`, you can protect against
[zip bombs](https://en.wikipedia.org/wiki/Zip_bomb) — archives crafted to expand to an enormous amount of data
and potentially exhaust the memory available to Flux. Both options below are disabled by default.

To limit the maximum number of uncompressed bytes read from any single ZIP entry:

    --zip-max-uncompressed-entry-bytes 268435456

A value of `268435456` (256 MB) is a reasonable starting point for most use cases. If a single entry exceeds
this limit, an error is thrown. Set to `0` or any value less than `1` to disable.

To limit the maximum number of entries processed from a single ZIP file:

    --zip-max-entry-count 100000

A value of `100000` is a reasonable starting point for most use cases. Set to `0` or any value less than `1` to
disable.

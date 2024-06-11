---
layout: default
title: Reprocessing Data
nav_order: 5
---

Flux supports reprocessing data already in MarkLogic by using custom code to query for data and custom code to process
retrieved data. Reprocessing data typically involves writing data back to MarkLogic, but you are free to execute any
custom code you wish both when querying and processing data. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `reprocess` command supports reprocessing via code written in either 
[JavaScript](https://docs.marklogic.com/guide/getting-started/javascript) or 
[XQuery](https://docs.marklogic.com/guide/getting-started/XQueryTutorial). The command requires a "reader" - code that
reads data from MarkLogic - and a "writer" - code that processes each item returned by the reader, though it is not
required to actually write any data. 

A common use case for reprocessing is for the reader to return document URIs, and the writer performs some operation 
on each document URI. However, your reader is free to return any items, and your writer is free to perform any 
processing you wish. 

For the reader, you must specify one of the following options:

- `--readJavascript` = JavaScript code for reading data.
- `--readJavascriptFile` = path to file containing JavaScript code for reading data. 
- `--readXquery` = XQuery code for reading data.
- `--readXqueryFile` = path to file containing XQuery code for reading data.
- `--readInvoke` = path of a MarkLogic server module to invoke for reading data.

For the writer, you must specify one of the following options:

- `--writeJavascript` = JavaScript code for writing data. 
- `--writeJavascriptFile` = path to file containing JavaScript code for writing data.
- `--writeXquery` = XQuery code for writing data.
- `--writeXqueryFile` = path to file containing XQuery code for writing data.
- `--writeInvoke` = path of a MarkLogic server module to invoke for writing data. 

You must also specify [connection information](common-options.md) for the MarkLogic database containing the data 
you wish to reprocess. 

The following shows a simple example of querying a collection for its URIs and logging each one:

```
./bin/flux reprocess --connectionString user:password@localhost:8000 \
  --readJavascript "cts.uris(null, null, cts.collectionQuery('example'))"
  --writeJavascript "var URI; console.log(URI)"
```

## Configuring variables

You can define variables in your custom code, regardless of how you define that code. Variables allow for code to be 
reused with different inputs that are defined as command line options.

For variables in your reader, you can include the `--readVar` option multiple times with each value being of the 
form `variableName=variableValue`. Likewise for the writer, you can include the `--writeVar` option multiple times 
with each value being of the same form.

The following shows a simple example of including a variable in both the reader and writer:

```
./bin/flux reprocess --connectionString user:password@localhost:8000 \
  --readJavascript "var collection; cts.uris(null, null, cts.collectionQuery(collection))"
  --readVar "collection=example"
  --writeJavascript "var URI; var exampleVariable; console.log([URI, exampleVariable])"
  --writeVar "exampleVariable=testValue"
```

## Defining reader partitions

Flux will send a single request to MarkLogic to execute your reader code. If your reader returns a large amount of data 
and is at risk of timing out, or if you seek better performance by breaking your query into many smaller queries, you 
can use one of the following options to define partitions for your reader:

- `--readPartitionsJavascript` = JavaScript code that returns partitions. 
- `--readPartitionsJavascriptFile` = path to file containing JavaScript code that returns partitions. 
- `--readPartitionsXquery` = XQuery code that returns partitions.
- `--readPartitionsXqueryFile` = path to file containing XQuery code that returns partitions.
- `--readPartitionsInvoke` = path of a MarkLogic server module to invoke for returning partitions.

For each partition returned, the reader code will be invoked with a variable named `PARTITION` containing the value of
the partition. Your reader code is then free to use that value however you wish.

Partition values can thus be anything you want. A common use case is to partition a reader by each forest in your 
MarkLogic database. The following shows an example of partitions based on forests:

```
./bin/flux reprocess --connectionString user:password@localhost:8000 \
  --readPartitionsJavascript "xdmp.databaseForests(xdmp.database())"
  --readJavascript "cts.uris(null, null, cts.collectionQuery('example'), 0, [PARTITION])"
  --writeJavascript "var URI; console.log(URI)"
```

In the above example, the code defined by `--readJavascript` will be invoked once for each forest ID returned by the code
defined by `--readPartitionsJavascript`. The value of `PARTITION` - a forest ID - is then passed to the 
[cts.uris](https://docs.marklogic.com/cts.uris) function to constrain it to a particular forest. With this approach, 
the query is broken up into N queries that run in parallel, with N equalling the number of forests in the database.

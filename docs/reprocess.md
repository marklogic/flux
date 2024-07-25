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

- `--read-javascript` = JavaScript code for reading data.
- `--read-javascript-file` = path to file containing JavaScript code for reading data. 
- `--read-xquery` = XQuery code for reading data.
- `--read-xquery-file` = path to file containing XQuery code for reading data.
- `--read-invoke` = path of a MarkLogic server module to invoke for reading data.

For the writer, you must specify one of the following options:

- `--write-javascript` = JavaScript code for writing data. 
- `--write-javascript-file` = path to file containing JavaScript code for writing data.
- `--write-xquery` = XQuery code for writing data.
- `--write-xquery-file` = path to file containing XQuery code for writing data.
- `--write-invoke` = path of a MarkLogic server module to invoke for writing data. 

You must also specify [connection information](common-options.md) for the MarkLogic database containing the data 
you wish to reprocess. 

The following shows a simple example of querying a collection for its URIs and logging each one:

```
./bin/flux reprocess --connection-string user:password@localhost:8000 \
  --read-javascript "cts.uris(null, null, cts.collectionQuery('example'))"
  --write-javascript "var URI; console.log(URI)"
```

Flux assumes that your writer code will expect an external variable named `URI`. To change this, use the 
`--external-variable-name` option to specify a different name. 


## Processing multiple items at once

By default, each item that Flux reads will be sent to the writer. You can typically achieve better performance by 
structuring your custom code to receive multiple items at once, as this can result in far fewer calls to MarkLogic. 

To do so, first set the `--batch-size` option to the number of items that should be sent to each writer. 
Next, you will likely need to modify your custom code to support the external variable named `URI` to be a 
comma-delimited string of items instead of a single item.

For example, you might use the following to add an existing URI to a collection:

    --write-javascript "declareUpdate(); xdmp.documentAddCollections(URI, 'new-collection')"

One way to modify that custom code to support multiple items being sent would be this:

```
--batch-size 100 \
--write-javascript "declareUpdate(); for (var uri of URI.split(',')) {xdmp.documentAddCollections(uri, 'new-collection')}"
```

If your items have commas in them, you can use the `--external-variable-delimiter` option to specify a different 
delimiter for Flux to use when it concatenates items together. 

## Configuring variables

You can define variables in your custom code, regardless of how you define that code. Variables allow for code to be 
reused with different inputs that are defined as command line options.

For variables in your reader, you can include the `--read-var` option multiple times with each value being of the 
form `variableName=variableValue`. Likewise for the writer, you can include the `--write-var` option multiple times 
with each value being of the same form.

The following shows a simple example of including a variable in both the reader and writer:

```
./bin/flux reprocess --connection-string user:password@localhost:8000 \
  --read-javascript "var collection; cts.uris(null, null, cts.collectionQuery(collection))"
  --read-var "collection=example"
  --write-javascript "var URI; var exampleVariable; console.log([URI, exampleVariable])"
  --write-var "exampleVariable=testValue"
```

## Defining reader partitions

Flux will send a single request to MarkLogic to execute your reader code. If your reader returns a large amount of data 
and is at risk of timing out, or if you seek better performance by breaking your query into many smaller queries, you 
can use one of the following options to define partitions for your reader:

- `--read-partitions-javascript` = JavaScript code that returns partitions. 
- `--read-partitions-javascript-file` = path to file containing JavaScript code that returns partitions. 
- `--read-partitions-xquery` = XQuery code that returns partitions.
- `--read-partitions-xquery-file` = path to file containing XQuery code that returns partitions.
- `--read-partitions-invoke` = path of a MarkLogic server module to invoke for returning partitions.

For each partition returned, the reader code will be invoked with a variable named `PARTITION` containing the value of
the partition. Your reader code is then free to use that value however you wish.

Partition values can thus be anything you want. A common use case is to partition a reader by each forest in your 
MarkLogic database. The following shows an example of partitions based on forests:

```
./bin/flux reprocess --connection-string user:password@localhost:8000 \
  --read-partitions-javascript "xdmp.databaseForests(xdmp.database())"
  --read-javascript "cts.uris(null, null, cts.collectionQuery('example'), 0, [PARTITION])"
  --write-javascript "var URI; console.log(URI)"
```

In the above example, the code defined by `--read-javascript` will be invoked once for each forest ID returned by the code
defined by `--read-partitions-javascript`. The value of `PARTITION` - a forest ID - is then passed to the 
[cts.uris](https://docs.marklogic.com/cts.uris) function to constrain it to a particular forest. With this approach, 
the query is broken up into N queries that run in parallel, with N equalling the number of forests in the database.

## Improving performance

The default behavior of Flux of sending each item in an individual call to your writer code is not typically going to 
perform well. The two techniques above that are good candidates for improving performance are:

1. Configuring `--batch-size` and altering your writer code to accept multiple items in a single call. 
2. Defining reader partitions, which allows for more items to be processed in parallel. 

The `reprocess` command in Flux does not have a "thread count" option. Instead, partitions are used to parallelize
work, which allows you to consume more of the resources available to your MarkLogic cluster. 

Creating partitions based on database forests and then adjusting your query to constrain to a specific forest is 
typically a good practice for achieving better performance. The section on creating partitions above uses the 
following, which is a simple way to both create and use forest-based partitions:

```
--read-partitions-javascript "xdmp.databaseForests(xdmp.database())"
--read-javascript "cts.uris(null, null, cts.collectionQuery('example'), 0, [PARTITION])"
```

With the above approach, Flux will create one partition per forest. You should ensure that the number of partitions is
similar to the number of MarkLogic app server threads available. 

For example, consider a 3 host cluster with a load balancer in front of each, where each app server has the default of 
32 threads available. Also assume that the database has 4 forests on each host. With the above approach, Flux will 
create 12 partitions, one per forest. Each partition uses a single thread to make calls to MarkLogic. Thus, while your
cluster has 96 app server threads available, only 12 will be used. 

For the above scenario, you can also use the `--repartition` option mentioned in the [Common Options guide](common-options.md). This
allows you to force a number of partitions to be created after all the data has been and before any has been written. 
You could thus include `--repartition 96` as an option to use all 96 of the available app server threads.

The downside to using `--repartition` is that Flux must read all the data first before it can making any calls to 
MarkLogic to reprocess it. However, in many reprocessing use cases, data can be read very quickly from MarkLogic but 
processing all of it can be much slower. In such a scenario, the need to read all the data is easily offset by the 
significant gains in having dozens or more partitions each processing items - in batches - in parallel calls 
to MarkLogic. 


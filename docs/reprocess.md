---
layout: default
title: Reprocessing Data
nav_order: 5
---

Flux supports reprocessing data in MarkLogic by using custom code to query for data and custom code to process
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

Your reader code must return a sequence, whether implemented [in JavaScript](https://docs.marklogic.com/js/Sequence)
or [in XQuery](https://docs.marklogic.com/guide/xquery/langoverview#id_88685). The following show simple examples
of both:

```
// Return a sequence of URIs in JavaScript.
cts.uris("", null, cts.collectionQuery('example'))
```

```
(: Return a sequence of URIs in XQuery. :)
cts:uris((), (), cts:collection-query('example'))
```

Your writer code will then be invoked once for each item returned by the reader (see below for instructions on sending
a batch of items to the writer). The writer code will be sent a variable named `URI`, though the items do not need to 
be URIs. The following show simple examples of JavaScript and XQuery writers (document metadata such as collections 
and permissions are omitted for the sake of brevity):

```
// A simple JavaScript writer.
declareUpdate();
var URI;
xdmp.documentInsert(URI, {"example": "document"});
```

```
(: A simple XQuery writer :)
xquery version "1.0-ml";
declare variable $URI external;
xdmp:document-insert($URI, <example>document</example>)
```

To configure the reader, you must specify one of the following options:

| Option | Description| 
| --- |---|
| `--read-javascript` | JavaScript code for reading data.|
| `--read-javascript-file` | Path to file containing JavaScript code for reading data. | 
| `--read-xquery` | XQuery code for reading data. |
| `--read-xquery-file` | Path to file containing XQuery code for reading data. |
| `--read-invoke` | Path of a MarkLogic server module to invoke for reading data. |

To configure the writer, you must specify one of the following options:

| Option | Description| 
| --- |---|
| `--write-javascript` | JavaScript code for writing data.| 
| `--write-javascript-file` | Path to file containing JavaScript code for writing data.|
| `--write-xquery` | XQuery code for writing data.|
| `--write-xquery-file` | Path to file containing XQuery code for writing data.|
| `--write-invoke` | Path of a MarkLogic server module to invoke for writing data.|

If you use `--read-invoke` or `--write-invoke`, MarkLogic will determine whether the referenced module is JavaScript
or XQuery based on the extension of the module. MarkLogic by default associates the `.sjs` extension with JavaScript
but not the `.js` extension. For JavaScript modules, it is thus recommended to use `.sjs` as an extension. 

You must also specify [connection information](common-options.md) for the MarkLogic database containing the data 
you wish to reprocess. 

The following shows a simple example of querying a collection for its URIs and logging each one:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux reprocess \
    --connection-string "flux-example-user:password@localhost:8004" \
    --read-javascript "cts.uris(null, null, cts.collectionQuery('example'))" \
    --write-javascript "var URI; console.log(URI)"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux reprocess ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --read-javascript "cts.uris(null, null, cts.collectionQuery('example'))" ^
    --write-javascript "var URI; console.log(URI)"
```
{% endtab %}
{% endtabs %}

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

{% tabs log %}
{% tab log Unix %}
```
./bin/flux reprocess \
  --connection-string "flux-example-user:password@localhost:8004" \
  --read-javascript "var collection; cts.uris(null, null, cts.collectionQuery(collection))" \
  --read-var "collection=example" \
  --write-javascript "var URI; var exampleVariable; console.log([URI, exampleVariable])" \
  --write-var "exampleVariable=testValue"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux reprocess ^
  @options.txt ^
  --read-javascript "var collection; cts.uris(null, null, cts.collectionQuery(collection))" ^
  --read-var "collection=example" ^
  --write-javascript "var URI; var exampleVariable; console.log([URI, exampleVariable])" ^
  --write-var "exampleVariable=testValue"
```
{% endtab %}
{% endtabs %}

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

{% tabs log %}
{% tab log Unix %}
```
./bin/flux reprocess \
  --connection-string "flux-example-user:password@localhost:8004" \
  --read-partitions-javascript "xdmp.databaseForests(xdmp.database())" \
  --read-javascript "cts.uris(null, null, cts.collectionQuery('example'), 0, [PARTITION])" \
  --write-javascript "var URI; console.log(URI)"
```
{% endtab %}
{% tab log Windows %}
```
bin\flux reprocess ^
  --connection-string "flux-example-user:password@localhost:8004" ^
  --read-partitions-javascript "xdmp.databaseForests(xdmp.database())" ^
  --read-javascript "cts.uris(null, null, cts.collectionQuery('example'), 0, [PARTITION])" ^
  --write-javascript "var URI; console.log(URI)"
```
{% endtab %}
{% endtabs %}


In the above example, the code defined by `--read-javascript` will be invoked once for each forest ID returned by the code
defined by `--read-partitions-javascript`. The value of `PARTITION` - a forest ID - is then passed to the 
[cts.uris](https://docs.marklogic.com/cts.uris) function to constrain it to a particular forest. With this approach, 
the query is broken up into N queries that run in parallel, with N equalling the number of forests in the database.

## Logging progress

In addition to using `--log-progress` as described in the [Common Options guide](common-options.md) to log progress
as items are processed, you can also set `--log-read-progress` to configure Flux to log progress as items are read. 
`--log-read-progress` defaults to logging a message at the `INFO` level every time 10,000 items have been read. 

## Improving performance

The default behavior of Flux of sending each item in an individual call to your writer code is not typically going to 
perform well. The two primary techniques for improving performance are:

1. Configuring `--batch-size` and altering your writer code to accept multiple items in a single call, as described in the "Processing multiple items at once" section above.
2. Configuring `--thread-count` to specify the number of threads for processing items.

The `--thread-count` option is actually an alias for the `--repartition` option. It is included in the `reprocess` command
as it is a familiar option name for other tools for reprocessing data in MarkLogic. Both options have the same effect, 
which is to configure the number of threads or partitions used by Flux for processing items. 

You may also be able to improve reader performance by partitioning your query by database forests. The section on 
creating partitions above uses the following, which is a simple way to both create and use forest-based partitions:

```
--read-partitions-javascript "xdmp.databaseForests(xdmp.database())"
--read-javascript "cts.uris(null, null, cts.collectionQuery('example'), 0, [PARTITION])"
```

With the above approach, Flux will create one partition per forest. Note that the `--thread-count` and `--repartition`
options will then adjust the number of partitions used for processing items.

For example, consider a 3 host cluster with a load balancer in front of each, where each app server has the default of 
32 threads available. Also assume that the database has 4 forests on each host. With the above approach, Flux will 
create 12 partitions - one per forest - for reading data. You could then use e.g. `--thread-count 96` to configure Flux
to use 96 threads to process all the items read via the initial set of 12 partitions.

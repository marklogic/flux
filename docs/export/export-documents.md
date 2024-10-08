---
layout: default
title: Exporting documents
parent: Exporting Data
nav_order: 2
---

Flux can export documents to files, with each document being written as a separate file and optionally compressed.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `export-files` command selects documents in a MarkLogic database and writes them to a filesystem.
You must specify a `--path` option for where files should be written along with connection information for the
MarkLogic database you wish to write to - for example:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
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
| `--uris` | Newline-delimited sequence of document URIs to retrieve. |

You must specify at least one of `--collections`, `--directory`, `--query`, `--string-query`, or `--uris`. You may specify any
combination of those options as well, with the exception that `--query` will be ignored if `--uris` is specified.

## Specifying a query

The `--query` accepts any one of the following inputs:

1. A [structured query](https://docs.marklogic.com/guide/search-dev/structured-query#).
2. A [CTS query](https://docs.marklogic.com/guide/rest-dev/search#id_30577).
3. A [combined query](https://docs.marklogic.com/guide/rest-dev/search#id_69918).

The type of query you select can then be expressed in either JSON or XML. The documentation links above provide 
complete details on constructing each type of query, but for convenience, an example of each query is
shown next. 

A structured query:

```
# JSON
{"query": {"term-query": {"text": "hello"}}}

# XML
<query xmlns='http://marklogic.com/appservices/search'><term-query><text>hello</text></term-query></query>
```

A CTS query:

```
# JSON
{"ctsquery": {"wordQuery": {"text": "hello"}}}

# XML
<word-query xmlns='http://marklogic.com/cts'><text>hello</text></word-query>
```

A combined query, with options included:

```
# JSON
{"search": {"options": {"constraint": {"name": "c1", "word": {"element": {"name": "text"}}}}}, "qtext": "c1:hello"}

# XML
<search xmlns='http://marklogic.com/appservices/search'>\
<options><constraint name='c1'><word><element name='text'/></word></constraint></options>\
<qtext>c1:hello</qtext></search>
```

### Specifying a query in an options file

Serialized queries can be very lengthy, and thus it is often easier to put the `--query` option and its value in an 
[options file](../common-options.md).

For queries expressed as JSON, you will need to ensure that the double quotes in your JSON are escaped correctly. 
For example:

```
--query
"{\"ctsquery\": {\"wordQuery\": {\"text\": \"hello\"}}}"
```

As noted in the [options file guide](../common-options.md), you can use a newline symbol specific to the shell
you use for running Flux to break the value into multiple lines:

```
--query
"{\"ctsquery\": \
{\"wordQuery\": {\"text\": \"hello\"}}}"
```

For queries expressed in XML, you may find it easier to use single quotes instead of double quotes, as single quotes 
do not require any escaping.

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
to each document before it is written to a file. A transform is configured via the following options:

| Option | Description | 
| --- | --- |
| `--transform` | Name of a MarkLogic REST transform to apply to each document before writing it to its destination. |
| `--transform-params` | Comma-delimited list of transform parameter names and values - e.g. param1,value1,param2,value2. |
| `--transform-params-delimiter` | Delimiter for `--transform-params`; typically set when a value contains a comma. |

The above link for REST transforms includes instructions on manually installing a transform. If you are using
[ml-gradle to deploy an application to MarkLogic](https://github.com/marklogic/ml-gradle), you can let ml-gradle
[automatically install your transform](https://github.com/marklogic/ml-gradle/wiki/How-modules-are-loaded).

### Redacting content

The [MarkLogic Redaction feature](https://docs.marklogic.com/guide/app-dev/redaction) is not yet available via
configuration in the MarkLogic REST API. Thus, when using a tool like Flux, the best way to redact content is via
a REST transform. An example of this, written in JavaScript, is shown below:

```
const rdt = require('/MarkLogic/redaction');

function transform(context, params, content) {
  return rdt.redact(content, "my-ruleset");
};

exports.transform = transform;
```

To use the above transform, verify that your user has been granted the MarkLogic `redaction-user` role. 

## Compressing content

The `--compression` option is used to write files either to gzip or ZIP files. 

To gzip each file, include `--compression GZIP`. 

To write multiple files to one or more ZIP files, include `--compression ZIP`. A ZIP file will be created for each 
partition that was created when reading data via Optic. You can include `--zip-file-count 1` to force all documents to be
written to a single ZIP file. See the below section on "Understanding partitions" for more information. 

### Windows-specific issues with ZIP files

In the likely event that you have one or more URIs with a forward slash - `/` - in them, then creating a ZIP file
with those URIs - which are used as the zip entry names - will produce confusing behavior on Windows. If you open the
ZIP file via Windows Explorer, Windows will erroneously think the file is empty. If you open the file using
7-Zip, you will see a top-level entry named `_` if one or more of your URIs begin with a forward slash. These are
effectively issues that only occur when viewing the file within Windows and do not reflect the actual contents of the
ZIP file. The contents of the file are correct and if you were to import them with Flux via the `import-files` 
command, you will get the expected results.

## Specifying an encoding

MarkLogic stores all content [in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
You can specify an alternate encoding when exporting documents to files via the `--encoding` option - e.g.:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections example \
    --path destination \
    --encoding ISO-8859-1
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections example ^
    --path destination ^
    --encoding ISO-8859-1
```
{% endtab %}
{% endtabs %}


## Exporting large binary documents

MarkLogic's [support for large binary documents](https://docs.marklogic.com/guide/app-dev/binaries#id_93203) allows 
for storing binary files of any size. To ensure that large binary documents can be exported to a file path, consider
using the `--streaming` option introduced in Flux 1.1.0. When this option is set, Flux will stream each document
from MarkLogic directly to the file path, thereby avoiding reading the contents of a file into memory. This option 
can be used when exporting documents to gzip or ZIP files as well via the `--compression zip` option. 

As streaming to a file requires Flux to retrieve one document at a time from MarkLogic, you should not use this option
when exporting smaller documents that can easily fit into the memory available to Flux. 

When using `--streaming`, the following options will behave in a different fashion:

- `--batch-size` will still affect how many URIs are retrieved from MarkLogic in a single request, but will not impact
the number of documents retrieved from MarkLogic in a single request, which will always be 1.
- `--encoding` will be ignored as applying an encoding requires reading the document into memory.
- `--pretty-print` will have no effect as the contents of a document will never be read into memory.

You typically will not want to use the `--transform` option as applying a REST transform in MarkLogic to a 
large binary document may exhaust the amount of memory available to MarkLogic.

## Understanding partitions

As Flux is built on top of Apache Spark, it is heavily influenced by how Spark
[defines and manages partitions](https://sparkbyexamples.com/spark/spark-partitioning-understanding/). Within the
context of Flux, partitions can be thought of as "workers", with each worker operating in parallel on a different subset
of data. Generally, more partitions allow for more parallel work and improved performance.

When exporting documents to files, the number of partitions impacts how many files will be written. For example, run
the following command below from the [Getting Started guide](getting-started.md):

{% tabs log %}
{% tab log Unix %}
```
rm export/*.zip
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --permissions flux-example-role,read,flux-example-role,update \
    --path destination \
    --compression zip
```
{% endtab %}
{% tab log Windows %}
```
del export\*.zip
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --path destination ^
    --compression zip
```
{% endtab %}
{% endtabs %}

The `./export` directory will have 12 ZIP files in it. This count is due to how Flux reads data from MarkLogic,
which involves creating 4 partitions by default per forest in the MarkLogic database. The example application has 3
forests in its content database, and thus 12 partitions are created, resulting in 12 separate ZIP files.

You can use the `--partitions-per-forest` option to control how many partitions - and thus workers - read documents
from each forest in your database:

{% tabs log %}
{% tab log Unix %}
```
rm export/*.zip
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --permissions flux-example-role,read,flux-example-role,update \
    --path destination \
    --compression zip \
    --partitions-per-forest 1
```
{% endtab %}
{% tab log Windows %}
```
del export\*.zip
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --path destination ^
    --compression zip ^
    --partitions-per-forest 1
```
{% endtab %}
{% endtabs %}


This approach will produce 3 ZIP files - one per forest.

You can also use the `--repartition` option, available on every command, to force the number of partitions used when
writing data, regardless of how many were used to read the data:

{% tabs log %}
{% tab log Unix %}
```
rm export/*.zip
./bin/flux export-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections employee \
    --path destination \
    --compression zip \
    --repartition 1
```
{% endtab %}
{% tab log Windows %}
```
del export\*.zip
bin\flux export-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections employee ^
    --path destination ^
    --compression zip ^
    --repartition 1
```
{% endtab %}
{% endtabs %}


This approach will produce a single ZIP file due to the use of a single partition when writing files. 
The `--zip-file-count` option is effectively an alias for `--repartition`. Both options produce the same outcome. 
`--zip-file-count` is included as a more intuitive option for the common case of configuring how many files should
be written. 

Note that Spark's support for repartitioning may negatively impact overall performance due to the need to read all 
data from the data source first before writing any data. 

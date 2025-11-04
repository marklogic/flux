---
layout: default
title: Common Options
nav_order: 3
---

This guide describes options common to every command in Flux.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Viewing usage

You can view usage for all Flux commands by running Flux without any command name or options:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux
```
{% endtab %}
{% tab log Windows %}
```
bin\flux
```
{% endtab %}
{% endtabs %}

To see a list of options for a particular command, run the `help` command followed by the name of the command
you are interested in:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux help import-jdbc
```
{% endtab %}
{% tab log Windows %}
```
bin\flux help import-jdbc
```
{% endtab %}
{% endtabs %}

The "help" for each command will first list the command-specific options, with each required option having an asterisk
next to it. The command-specific options are followed by the connection options for connecting to MarkLogic, and those
are followed by a list of options common to every Flux command. 

## Command abbreviations

You can specify a command name without entering its full name, as long as you enter a sufficient number of characters
such that Flux can uniquely identify the command name.

For example, instead of entering `import-parquet-files`, you can enter `import-p` as it is the only command in
Flux beginning with that sequence of letters:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-p --path path/to/data etc...
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-p --path path\to\data etc...
```
{% endtab %}
{% endtabs %}


If Flux cannot uniquely identify the command name, it will print an error and list the command names that match what
you entered.

## Reading options from a file

Flux supports reading options from a file. In a text file, you can either add each option name and value to a separate
line:

```
--host
localhost
--port
8000
etc...
```

Or you can put one or more options on the same line:

```
--host localhost
--port 8000
etc...
```

You then reference the file via the `@` symbol followed by a filename:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files @my-options.txt
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files @my-options.txt
```
{% endtab %}
{% endtabs %}

You can reference multiple files this way and also include additional options on the command line.

Values in an options file can have whitespace in them, including newline symbols. If your values have whitespace, 
put double or single quotes around them. Within quoted values, backslashes must be escaped with another backslash. 
Newlines must then be represented by the same character you would use for a newline symbol in a normal value on the 
command line. For example, most Unix shells require a `\` for allowing a value to continue onto the next line.

As an example, MarkLogic Optic queries are often easier to read when stretched across multiple lines. 
The following shows an example of such a query in an options file, using a `\` to let the query continue 
to the next line:

```
--query
"op.fromView('example', 'employees', '')\
   .limit(10)"
```

When using an option such as `--uris` that supports specifying multiple values delimited by a newline symbol, 
you can use the same approach as above to specify multiple values across multiple lines:

```
--uris
"/uri1.json\
/uri2.json\
/uri3.json"
```

## Connecting to MarkLogic

Every command in Flux will need to connect to a MarkLogic database, either for reading data or writing data or both. 
Generally, you must include at least the following information for each command:

- The name of a MarkLogic host.
- The port of a [MarkLogic REST API app server](https://docs.marklogic.com/guide/rest-dev) connected to the database you wish to interact with.
- Authentication information.

### Using a connection string

For the common use case of using digest or basic authentication with a MarkLogic app server, you can use the 
`--connection-string` option to specify the host, port, username, and password in a single concise option:

    --connection-string user:password@host:port

You can also specify a database name in case the database you wish to interact with is not the one associated with 
the port of the app server you connect to:

    --connection-string user:password@host:port/databaseName

In case your username or password use symbols like "@", you can 
[percent encode](https://developer.mozilla.org/en-US/docs/Glossary/Percent-encoding) the values. For example, for a 
password of `sp@r:k`, you would use the following string:

    --connection-string user:sp%40r%3Ak@host:8000

For other authentication mechanisms, you must use the `--host` and `--port` options to define the host and port for 
your MarkLogic app server. 

### Determining the connection type

The `--connection-type` option determines which of the following approaches Flux uses for connecting to MarkLogic:

- `GATEWAY` = the default value; Flux assumes that it cannot directly connect to each host in the MarkLogic cluster, most
likely due to the value of `--host` or the host value found in `--connection-string` being that of a load balancer that
controls access to MarkLogic.
- `DIRECT` = Flux will try to connect to each host in the MarkLogic cluster. 

If you do not have a load balancer in front of MarkLogic, and if Flux is able to connect to each host that hosts one
or more forests for the database you wish to access, then you can set `--connection-type` to a value of `DIRECT`. This
will often improve performance as Flux will be able to both connect to multiple hosts, thereby utilizing the app server
threads available on each host, and also write directly to a forest on the host that it connects to. 

### Connecting to Progress Data Cloud

To connect to Progress Data Cloud (PDC), if you are using Flux 2.0 or higher, you must set at least the following options:

{% tabs log %}
{% tab log Unix %}
```
--host your-pdc-host-name \
--cloud-api-key your-key-goes-here \
--base-path your-integration-endpoint
```
{% endtab %}
{% tab log Windows %}
```
--host your-pdc-host-name ^
--cloud-api-key your-key-goes-here ^
--base-path your-integration-endpoint
```
{% endtab %}
{% endtabs %}

If you are using Flux 1.x, you must also include `--auth-type cloud`. 

The value of `--cloud-api-key` is an API key that you have generated in your PDC tenancy. Please see the 
[PDC documentation on API keys](https://docs.progress.com/bundle/progress-data-cloud-use/page/topics/account-settings/manage-api-key.html)
for more information.

The value of `--base-path` is an "integration endpoint" that you have configured in your PDC tenancy. The integration endpoint 
must map to a MarkLogic REST API app server. While you can 
[manually create a REST API app server](https://docs.progress.com/bundle/marklogic-server-develop-rest-api-12/page/topics/service.html#id_12021), 
it is recommended to [use ml-gradle](https://github.com/marklogic/ml-gradle/wiki) 
to deploy a complete application to PDC instead, which will include a REST API app server. 

Once you have a REST API app server in your PDC tenancy, please see the 
[PDC documentation on integration endpoints](https://docs.progress.com/bundle/progress-data-cloud-use/page/topics/access-your-services/marklogic/expose-an-app-server.html) 
for instructions on configuring an integration endpoint that will proxy requests to your REST API app server. The integration 
endpoint you configure will then be the value of the `--base-path` option. For example, if your integration endpoint is 
"/ml/ml12/default/my-server", you would include the following option:

    --base-path /ml/ml12/default/my-server

By default, Flux will use your JVM's default keystore and truststore for establishing a secure connection to PDC. See 
the following sections for options on how to configure the SSL connection.

### Configuring one-way SSL

Flux supports both one-way and two-way SSL connections to MarkLogic (the term "SSL" is used as a synonym for "TLS" in
this context). The full list of SSL options is in the "Connection Options" table below. Please see the 
[MarkLogic documentation](https://docs.marklogic.com/11.0/guide/security-guide/en/configuring-ssl-on-app-servers.html) 
for full details on configuring SSL for a MarkLogic app server.

For a one-way SSL connection, where Flux only needs to trust the certificate associated with your MarkLogic app server, 
you must configure the following options:

| Option | Description | 
| --- | --- |
| `--ssl-protocol` | Must define the desired SSL protocol; `TLSv1.2` is a typical value. |
| `--truststore-path`| Must define the path to a Java truststore file containing the certificate associated with your MarkLogic app server. |
| `--truststore-password` | Must specify the password for your truststore file.
| `--ssl-hostname-verifier` | You may need to configure this option depending on the certificate authority associated with the app server certificate.  See the section on SSL errors below for more information. |

If the Java virtual machine (JVM) that you use to run Flux has the certificate associated with your MarkLogic app server 
already in the JVM's default truststore, you can omit `--truststore-path` and `--truststore-password` and instead give
the `--ssl-protocol` option a value of `default`. 

### Configuring two-way SSL

Two-way SSL connection is required when the MarkLogic app server has a value of `true` for its 
"SSL Require Client Certificate" field in the MarkLogic admin application. To configure two-way SSL with Flux, you 
must configure the following options:

| Option | Description | 
| --- | --- |
| `--keystore-path` | Must define the path of a Java keystore containing a client certificate trusted by your MarkLogic app server.
| `--keystore-password` | Must specify the password for your Java keystore. |
| `--truststore-path`| Must define the path to a Java truststore file containing the certificate associated with your MarkLogic app server. |
| `--truststore-password` | Must specify the password for your truststore file.
| `--ssl-hostname-verifier` | You may need to configure this option depending on the certificate authority associated with the app server certificate.  See the section on SSL errors below for more information. |

When specifying a keystore via `--keystore-path`, Flux will default the value of `--ssl-protocol` to `TLSv1.2`. You only
need to specify this option if that default protocol is not correct for your application. 

Similar to one-way SSL, if your JVM truststore contains your server certificate, you can set the value of 
`--ssl-protocol` to `default` to use your JVM truststore. 

### Common SSL errors

If you receive an error containing `unable to find valid certification path to requested target`, then your truststore
is not able to trust the certificate associated with your MarkLogic app server. Verify that the truststore identified by
`--truststore-path`, or the default JVM truststore if using `--ssl-protocol default`, contains the required app server 
certificate. 

If you receive an error message containing `Hostname (the hostname) not verified`, this is typically due to the 
certificate authority associated with your app server certificate not being a trusted certificate authority. You can 
consider the use of `--ssl-hostname-verifier ANY` to disable hostname verification.

If you receive an error message containing `No trusted certificate found`, check to see if your MarkLogic app server
has a value of `true` for the "SSL Client Issuer Authority Validation" field. If so, verify that the list of selected
"SSL Client Certificate Authorities" includes a certificate authority associated with your client certificate. 

### Connection options

All available connection options are shown in the table below:

| Option | Description | 
| --- | --- |
| `--auth-type` | Type of authentication to use. Possible values are `BASIC`, `DIGEST`, `CLOUD`, `CERTIFICATE`, `KERBEROS`, `OAUTH`, and `SAML`.|
| `--base-path` | Path to prepend to each call to a MarkLogic [REST API app server](https://docs.marklogic.com/guide/rest-dev). |
| `--certificate-file` | File path for a keystore to be used for `CERTIFICATE` authentication. |
| `--certificate-password` | Password for the keystore referenced by `--certificate-file`. |
| `--connection-string` |  Defines a connection string as user:password@host:port/optionalDatabaseName; only usable when using `DIGEST` or `BASIC` authentication. |
| `--cloud-api-key` | API key for authenticating with a Progress Data Cloud cluster when authentication type is `CLOUD`. |
| `--connection-type` |  Set to `DIRECT` if connections can be made directly to each host in the MarkLogic cluster. Defaults to `GATEWAY`. Possible values are `DIRECT` and `GATEWAY`. |
| `--database` | Name of a database to connect if it differs from the one associated with the app server identified by `--port`. |
| `--disable-gzipped-responses` | If included, responses from MarkLogic will not be gzipped. May improve performance when responses are very small.
| `--host` | The MarkLogic host to connect to. |
| `--kerberos-principal` | Principal to be used with `KERBEROS` authentication. |
| `--keystore-algorithm` |  Algorithm of the keystore identified by `--keystore-path`; defaults to `SunX509`. |
| `--keystore-password` | Password for the keystore identified by `--keystore-path`. |
| `--keystore-path` | File path for a keystore for two-way SSL connections. |
| `--keystore-type` | Type of the keystore identified by `--keystore-path`; defaults to `JKS`. |
| `--oauth-token` | Token to be used with `OAUTH` authentication. |
| `--password` | Password when using `DIGEST` or `BASIC` authentication. |
| `--port` | Port of the [REST API app server](https://docs.marklogic.com/guide/rest-dev) to connect to. |
| `--saml-token` | Token to be used with `SAML` authentication. |
| `--ssl-hostname-verifier` | Hostname verification strategy when connecting via SSL. Possible values are `ANY`, `COMMON`, and `STRICT`. |
| `--ssl-protocol` | SSL protocol to use when the MarkLogic app server requires an SSL connection. If a keystore or truststore is configured, defaults to `TLSv1.2`. |
| `--truststore-algorithm` | Algorithm of the truststore identified by `--truststore-path`; defaults to `SunX509`. |
| `--truststore-password` | Password for the truststore identified by `--truststore-path`. |
| `--truststore-path` | File path for a truststore for establishing trust with the certificate used by the MarkLogic app server. |
| `--truststore-type` | Type of the truststore identified by `--truststore-path`; defaults to `JKS`. |
| `--username` | Username when using `DIGEST` or `BASIC` authentication. |


## Previewing data

The `--preview` option works on every command in Flux. For example, given a set of Parquet files in a directory, 
you can preview an import without writing any of the data to MarkLogic:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-parquet-files \
    --path export/parquet \
    --connection-string "flux-example-user:password@localhost:8004" \ 
    --preview 10
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-parquet-files ^
    --path export\parquet ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --preview 10
```
{% endtab %}
{% endtabs %}

The number after `--preview` specifies how many records to show. You can use `--preview-drop` to specify potentially
verbose columns to drop from the preview. And you can use `--preview-list` to see the results in a list
instead of in a table:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --path export/parquet \
    --preview 10 \
    --preview-drop job_title,department \
    --preview-list
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-parquet-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --path export\parquet ^
    --preview 10 ^
    --preview-drop job_title,department ^
    --preview-list
```
{% endtab %}
{% endtabs %}

Note that in the case of previewing an import, Flux will show the data as it has been read, which consists of a set of
Spark rows with columns. The data is not shown as a set of documents yet, as the transformation of rows to documents 
occurs when the data is written to MarkLogic.

For some commands, it may be helpful to see the schema of the data read from the command's data source. For example, 
when exporting data with a MarkLogic Optic query, you may wish to verify that the datatypes of each column are what you
expect before writing the data to a Parquet file or relational database. Use the `--preview-schema` option to request 
that Flux log the schema and not write any data:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-parquet-files \
    --connection-string "flux-example-user:password@localhost:8004" \
    --query "op.fromView('example', 'employees')" \
    --path export/parquet \
    --preview-schema
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-parquet-files ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --query "op.fromView('example', 'employees')" ^
    --path export\parquet ^
    --preview-schema
```
{% endtab %}
{% endtabs %}


## Applying a limit

For many use cases, it can be useful to only process a small subset of the source data to ensure that the results
are correct. The `--limit` option will achieve this by limiting the number of rows read by a command.

The following shows an example of only importing the first 10 rows from a delimited file:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path ../data/employees.csv.gz \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update \
    --collections employee \
    --uri-template "/employee/{id}.json" \
    --limit 10
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path ..\data\employees.csv.gz ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --collections employee ^
    --uri-template "/employee/{id}.json" ^
    --limit 10
```
{% endtab %}
{% endtabs %}

## Counting records to process

Instead of viewing a preview of data or limiting how much data is processed by Flux, you may want to first obtain a 
count of records that will be read from the data source associated with the Flux command you are executing. This can be 
useful when dealing with a data source where it can be difficult to know how much data will be read, such as with very 
large aggregate XML or Parquet files. 

To view a count, include the `--count` option, which results in Flux reading all the data but not writing any of it
to the destination associated with the command:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path ../data/employees.csv.gz \
    --connection-string "flux-example-user:password@localhost:8004" \
    --count
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path ..\data\employees.csv.gz ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --count
```
{% endtab %}
{% endtabs %}

Flux will then log the count:

```
[main] INFO com.marklogic.flux: Count: 1000
```

The performance of using `--count` is dependent on how quickly data can be read from the command's data source. For 
example, commands that export documents from MarkLogic must read every document to be exported in order to calculate 
a count. For a scenario like that, you can likely much more quickly determine a count by running your query in 
MarkLogic's qconsole application.

## Viewing progress

Each Flux command supports a `--log-progress` option that you can use to specify how often you want a message logged
indicating progress. Import commands will show progress in terms of writing data to MarkLogic, while export 
commands will show progress in terms of reading data from MarkLogic. The `copy` command can show both types of progress, 
with `--log-progress` specifying an interval for logging data that has been read and `--output-log-progress` 
specifying an interval for logging data that has been written. The `reprocess` command supports both `--log-progress`
for processing data in MarkLogic and `--log-read-progress` for reading data from MarkLogic.

The `--log-progress` option defaults to a value of 10,000, with one exception - export commands that read rows from 
MarkLogic defaults `--log-progress` to a value of 100,000. For an import command with a value of 10,000, 
Flux will log a message at the `INFO` level every time it reads 10,000 documents - so at 10,000, 20,000, etc. 
You can change this value to suit your needs, though you typically will want it to be significantly larger than 
the value of `--batch-size` if the command supports that option.

## Viewing a stacktrace

When a command fails, Flux will stop execution of the command and display an error message. If you wish to see the 
underlying stacktrace associated with the error, run the command with the `--stacktrace` option included. This is 
included primarily for debugging purposes, as the stacktrace may be fairly long with only a small portion of it 
potentially being helpful. The initial error message displayed by Flux is intended to be as helpful as possible. 

## Configuring logging

Flux uses a [Log4J2 properties file](https://logging.apache.org/log4j/2.x/manual/configuration.html#Properties) to 
configure its logging. The file is located in a Flux installation at `./conf/log4j2.properties`. You are free to 
customize this file to meet your needs for logging.

## Configuring JVM options

The `./bin/flux` and `./bin/flux.bat` executables both apply JVM options defined by the `JAVA_OPTS` and `FLUX_OPTS`
environment variables. The [Java documentation](https://docs.oracle.com/en/java/javase/11/tools/java.html) provides
a list of standard and extra options that affect how the JVM performs. 

As a simple example, you could run the following on a Unix OS to configure the JVM to print its version information each
time you run Flux:

    export FLUX_OPTS=--show-version

## Advanced Spark options

Flux is built on top of [Apache Spark](https://spark.apache.org/) and provides a number of command line options for 
configuring the underlying Spark runtime environment used by Flux. 

### Configuring Spark worker threads

By default, Flux creates a Spark runtime with a master URL of `local[*]`, which runs Spark with as many worker 
threads as logical cores on the machine running Flux. The number of worker threads affects how many partitions can be
processed in parallel. You can change this setting via the`--spark-master-url` option; please see 
[the Spark documentation](https://spark.apache.org/docs/3.5.6/submitting-applications.html#master-urls) for examples
of valid values. If you are looking to run a Flux command on a remote Spark cluster, please instead see the 
[Spark Integration guide](spark-integration.md) for details on integrating Flux with `spark-submit`.

For import commands, you typically will not need to adjust this as a partition writer in an import command supports its
own pool of threads via the [MarkLogic data movement library](https://docs.marklogic.com/guide/java/data-movement). However,
depending on the data source, additional worker threads may help with reading data in parallel. 

For the [`reprocess` command](reprocess.md), setting the number of worker threads is critical to achieving optimal 
performance. As of Flux 1.2.0, the `--thread-count` option will adjust the Spark master URL based on the number of 
threads you specify. Prior to Flux 1.2.0, you can use `--repartition` to achieve the same effect. 

For exporting data, please see the [exporting guide](export/export.md) for information on how to adjust the worker 
threads depending on whether you are reading documents or rows from MarkLogic.

### Configuring the number of Spark partitions

Flux uses Spark partitions to allow for data to be read and written in parallel. Each partition can be thought of as
a separate worker, operating in parallel with each other worker.

A number of partitions will be determined by the command that you run before it reads data. The nature of the data
source directly impacts the number of partitions that will be created.

For some commands, you may find improved performance by changing the number of partitions used to write data to the
target associated with the command. For example, an `export-jdbc` command may only need a small number of partitions to 
read data from MarkLogic, but performance will be improved by using a far higher number of partitions to write data to
the JDBC destination. You can use the `--repartition` option to force the number of partitions to use for writing data. 
The downside to this option is that it forces Flux to read all the data from the data source before writing any to the
target. Generally, this option will help when data can be read quickly and the performance of writing can be
improved by using more partitions than were created when reading data - this is almost always the case for the 
`reprocess` command.

As of Flux 1.2.0, setting `--repartition` will default the value of the `--spark-master-url` option to be `local[N]`, 
where `N` is the value of `--repartition`. This ensures that each partition writer has a Spark worker thread available
to it. You can still override `--spark-master-url` if you wish.

### Setting Spark configuration properties

Flux allows for [Spark configuration properties](https://spark.apache.org/docs/latest/configuration.html) to be defined which will 
control the Spark runtime and how a Spark session is built. 

To specify options that control how the Spark Session is built, use the `--spark-conf` option as many times as needed. For example,
[encrypt data that Spark spills from memory to disk](https://spark.apache.org/docs/3.5.6/security.html#local-storage-encryption), you could include the following options:

    --spark-conf spark.io.encryption.enabled=true \
    --spark-conf spark.io.encryption.keySizeBits=256

You can also use this option for [Spark data sources](https://spark.apache.org/docs/3.5.6/sql-data-sources.html) that 
define configuration properties. 
For example, the [Spark Avro data source](https://spark.apache.org/docs/3.5.6/sql-data-sources-avro.html#configuration)
identifies several configuration properties, such as `spark.sql.avro.compression.codec`. You can set this value by 
including `--spark-conf spark.sql.avro.compression.codec=snappy` as a command line option. 

---
layout: default
title: Spark Integration
nav_order: 8
---

Flux can be used with an existing [Apache Spark](https://spark.apache.org/) cluster to support large workloads that
require more system resources than what are available when running Flux as a command line application. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Submitting Flux commands

Flux integrates with [spark-submit](https://spark.apache.org/docs/latest/submitting-applications.html) to allow you to 
submit a Flux command invocation to a remote Spark cluster. Every Flux command is a Spark application, and thus every
Flux command, along with all of its option, can be invoked via `spark-submit`. 

To use Flux with `spark-submit`, first download the `flux-1.0.0-all.jar` file from the 
[GitHub release page](https://github.com/marklogic/flux/releases/tag/1.0.0). This jar file includes Flux and all of 
its dependencies, excluding those of Spark itself, which will be provided via the Spark cluster that you connect to 
via `spark-submit`. 

You can now run any Flux command with `spark-submit`.  
The following shows a notional example of running the Flux `import-files` command:

```
$SPARK_HOME/bin/spark-submit --class com.marklogic.flux.spark.Submit \
    --master spark://changeme:7077 \
    flux-1.0.0-all.jar \
    import-files \
    --path path/to/data
    --connection-string user:password@host:8000
    --preview 10
```

Key points on the above example:

1. You must provide a value of `com.marklogic.flux.spark.Submit` for the required `--class` option. This class 
provides the entry point that allows for a Flux command to be run.
2. Set the value of `--master` to the master URL of your Spark cluster.
3. As noted in the [Spark documentation](https://spark.apache.org/docs/latest/submitting-applications.html), the path
to the Flux jar - the "application jar" - must be globally visible inside your cluster.
4. The "application arguments" consist of the name of the Flux command and the options you provide to that command. 


### Accessing S3 with spark-submit

If you wish to run a Flux command that accesses an S3 path, you must use the `--packages` option with `spark-submit`
to include the necessary dependencies to allow for Spark to access S3, as shown below:

    --packages org.apache.hadoop:hadoop-aws:3.3.4,org.apache.hadoop:hadoop-client:3.3.4

The above packages are not included in the Flux jar file, as the version required by your Spark cluster may differ.

### Using Avro data with spark-submit

Per the [Spark Avro documentation](https://spark.apache.org/docs/latest/sql-data-sources-avro.html), the `spark-avro`
dependency is not included in Spark by default. If you wish to run either `import-avro-files` or `export-avro-files`
with Flux and `spark-submit`, you must include the following line in your `spark-submit` invocation:

    --packages org.apache.spark:spark-avro_2.12:3.4.1

You should change the version number of this dependency to match that of your Spark cluster.

## Configuring Spark for local usage

When Flux is run via the command line, it creates a temporary local Spark cluster with a Spark master URL of 
`local[*]`. You can adjust this via the `--spark-master-url` option. Typically, this will involve still creating a 
local Spark cluster but with different settings. If you wish to connect to a remote Spark cluster, you will most likely
want to use `spark-submit` as described above. 

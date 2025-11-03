---
layout: default
title: FAQ
nav_order: 9
---

This page captures frequently asked questions with a focus on errors that you may encounter 
when running Flux. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Common errors

**How do I fix an error containing 'java.net.BindException: Cannot assign requested address'?**

You may encounter this error with any Flux command. The full error will contain the following:

```
java.net.BindException: Cannot assign requested address: Service 'sparkDriver' failed after 
16 retries (on a random free port)! Consider explicitly setting the appropriate binding address 
for the service 'sparkDriver' (for example spark.driver.bindAddress for SparkDriver) to the 
correct binding address.
```

The error is due to the underlying Spark runtime in Flux failing to startup. You can find a number of 
[resolutions for this error](https://stackoverflow.com/questions/52133731/how-to-solve-cant-assign-requested-address-service-sparkdriver-failed-after),
a few of which are summarized below:

1. Include `--spark-conf spark.driver.bindAddress=127.0.0.1` to define a Spark bind address that does not use `localhost`.
2. Run `export SPARK_LOCAL_IP="127.0.0.1"` to define a Spark bind address via an environment variable.
3. If you are on a VPN, try disconnecting and reconnecting to the VPN.


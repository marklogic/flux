---
layout: default
title: Flux API
nav_order: 7
---

Flux can be embedded as a normal dependency into your own JVM-based application. All the functionality available via 
the Flux CLI can instead be accessed via a public API. 


## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Adding Flux as a dependency

To add Flux as a dependency to your application, add the following to your Maven `pom.xml` file:

```
<dependency>
  <groupId>com.marklogic</groupId>
  <artifactId>flux-api</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

Or if you are using Gradle, add the following to your `build.gradle` file:

```
dependencies {
  implementation "com.marklogic:flux-api:1.0-SNAPSHOT"
}
```

## Using the Flux API

The `com.marklogic.flux.api.Flux` class is the entry point to the Flux API. This class has a static method associated
with each Flux CLI command. 

Each method in the `Flux` class returns a subclass of the `com.marklogic.flux.api.Executor` interface. Each instance of 
`Executor` has two methods for specifying a connection to MarkLogic:

- `connectionString(String)` allows you to specify a connection to MarkLogic via a connection string, which requires
the user of `basic` or `digest` authentication.
- `connection(Consumer<ConnectionOptions>)` allows you to specify a Java lambda expression for configuring the necessary 
options on the given `com.marklogic.flux.api.ConnectionOptions` object.

Each subclass of `Executor` has `from` and `to` methods for specifying options that control where data is read from and
control where data is written to. Similar to the `connection` method above, all `from` and `to` methods allow you to
specify a Java lambda expression to configuring the options. 

After configuring the necessary options for an `Executor`, you can use of the following methods to run the executor:

- `execute()` runs the executor, resulting in data being read and written based on the options you configured. This is
the typical method to invoke.
- `count()` returns the number of records that will be read by the executor, but no data will be written. This method is
is used when you first want to get a count of how much data will be processed.

## Examples

The following program shows a simple example of importing files via the Flux API.

```
package org.example;

import com.marklogic.flux.api.Flux;

public class App {

    public static void main(String[] args) {
        Flux.importGenericFiles()
            .connectionString("user:password@localhost:8000")
            .from(options -> options
                .paths("path/to/data"))
            .to(options -> options
                .collections("imported-files", "example-data)
                .permissionsString("rest-reader,read,rest-writer,update"))
            .execute();
    }
}
```

## Javadocs

Please see [the Flux API Javadocs](https://marklogic.github.io/flux/assets/javadoc) for a list of all
available public classes and methods.

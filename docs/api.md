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
  <version>1.2.1</version>
</dependency>
```

Or if you are using Gradle, add the following to your `build.gradle` file:

```
dependencies {
  implementation "com.marklogic:flux-api:1.2.1"
}
```

## Javadocs

Please see [the Flux API Javadocs](https://marklogic.github.io/flux/assets/javadoc) for a list of all
available public classes and methods.

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

### Using the Flux API in Gradle

You can use the Flux API in a `build.gradle` Gradle file too. You must first add it as a dependency to your build file:

```
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "com.marklogic:flux-api:1.2.1"
  }
}
```

You can then create a custom task that makes use of the Flux API in a similar fashion to using it in Java code:

```
import com.marklogic.flux.api.Flux
tasks.register("importFiles") {
  doLast {
    Flux.importGenericFiles()
      .from("path/to/files")
      .connectionString("flux-example-user:password@localhost:8004")
      .to({ it.permissionsString("flux-example-role,read,flux-example-role,update") })
      .execute()
  }
}
```

If you are running Gradle with Java 17 or higher, you will likely need to specify one or more `--add-opens` arguments
when running Gradle. For example, if you run Gradle with `--stacktrace` and see the text 
"because module java.base does not export sun.nio.ch" in the exception, you can add the following to your 
`gradle.properties` file to allow access to the `sun.nio.ch` package:

    org.gradle.jvmargs=--add-opens=java.base/sun.nio.ch=ALL-UNNAMED

The [Gradle documentation](https://docs.gradle.org/current/userguide/build_environment.html)  provides more information
on the `org.gradle.jvmargs` property along with other ways to customize the Gradle environment.

Please note that you cannot yet use the Flux API in your Gradle buildscript when you are also using the
MarkLogic [ml-gradle plugin](https://github.com/marklogic/ml-gradle). This is due to a classpath conflict, where the
MarkLogic Spark connector used by Flux must alter an underlying library so as not to conflict with Spark itself - but
that altered library then conflicts with ml-gradle. We will have a resolution for this soon.

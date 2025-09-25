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
  <version>2.0.0</version>
</dependency>
```

Or if you are using Gradle, add the following to your `build.gradle` file:

```
dependencies {
  implementation "com.marklogic:flux-api:2.0.0"
}
```

**Important** - a bug in the declaration of dependencies for Flux 1.4.0 means that you must also add the following to your 
`build.gradle` file to ensure that its dependencies are resolved correctly:
```groovy
configurations.all {
  resolutionStrategy.eachDependency { DependencyResolveDetails details ->
    if (details.requested.group.startsWith('com.fasterxml.jackson')) {
      details.useVersion '2.18.2'
      details.because 'Must use the version of Jackson required by Spark.'
    }
  }
}
```

The above is not needed for Flux 2.0.0 and later. 

If you are using Maven, you must add the following to your `pom.xml` file:
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
      <version>2.18.2</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.18.2</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-annotations</artifactId>
      <version>2.18.2</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.datatype</groupId>
      <artifactId>jackson-datatype-jsr310</artifactId>
      <version>2.18.2</version>
    </dependency>
  </dependencies>
</dependencyManagement>
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

### Example Java program

The following Java program shows a simple example of importing files via the Flux API.

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

## Using the Flux API in Gradle

You can use the Flux API in a `build.gradle` Gradle file too. This avoids the need for setting up a Java project and 
creating a separate Java class. You must first add the Flux API as a dependency to your build file so that it is 
available to custom Gradle tasks:

```
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "com.marklogic:flux-api:2.0.0"
  }
}
```

You can then create a custom task that makes use of the Flux API in a similar fashion to using it in Java code, but 
without the need for a separate Java source file:

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

For additional examples, please see the 
[client project in the Flux GitHub repository](https://github.com/marklogic/flux/blob/main/examples/client-project/build.gradle)
which demonstrates multiple ways of using the Flux API within Gradle.

### Granting access to Java modules

If you are running Gradle with Java 17 or higher, you will likely need to specify one or more `--add-opens` arguments
when running Gradle. For example, if you run Gradle with `--stacktrace` and see the text 
"because module java.base does not export sun.nio.ch" in the exception, you can add the following to your 
`gradle.properties` file to allow access to the `sun.nio.ch` package:

    org.gradle.jvmargs=--add-opens=java.base/sun.nio.ch=ALL-UNNAMED

The [Gradle documentation](https://docs.gradle.org/current/userguide/build_environment.html) provides more information
on the `org.gradle.jvmargs` property along with other ways to customize the Gradle environment.

### Using the Flux API alongside ml-gradle

Prior to Flux 1.3.0, the Flux API could not be used in the Gradle buildscript classpath if you were also using version
5.0.0 or higher of 
[the ml-gradle plugin](https://github.com/marklogic/ml-gradle) due to a classpath conflict. This conflict has been
resolved with Flux 1.3.0.

However, you must include the following `configurations` block in your `build.gradle` file to ensure that when 
running a custom task that depends on the Flux API while also applying the ml-gradle plugin, 
the required version of a particularly shared dependency is used:

```
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath "com.marklogic:flux-api:2.0.0"
  }
  configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
      if (details.requested.group.startsWith('com.fasterxml.jackson')) {
        details.useVersion '2.18.2'
        details.because 'Must ensure the Jackson version preferred by Spark is used to avoid classpath conflicts.'
      }
    }
  }
}
```

Without the `configurations` block shown above, you will encounter an error similar to the one shown below 
(you will likely need to include the Gradle `--stacktrace` option to see this error):

```
Scala module 2.18.2 requires Jackson Databind version >= 2.18.0 and < 2.19.0 - Found jackson-databind version 2.17.2
```

You may encounter this error with Gradle plugins other than ml-gradle that also use the Jackson library. If you do, 
the solution is the same - include the `configurations` block shown above in the `buildscript` of your `build.gradle`
file.

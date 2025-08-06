---
layout: default
title: Creating a custom integration
parent: Adding embeddings
grand_parent: Importing Data
nav_order: 2
---

As [described in the overview](embedder.md), each Flux release includes separate JAR files that each integrates
with a specific embedding model. If Flux does not yet provide a JAR file for your desired
[LangChain4j-supported embedding model](https://docs.langchain4j.dev/category/embedding-models), you can create your own
integration using the steps below.

## Table of contents
{: .no_toc .text-delta }

- TOC 
{:toc}

## Setup

Before beginning, you may wish to inspect the project that Flux uses to build a
[JAR file for Azure OpenAI](https://github.com/marklogic/flux/tree/develop/flux-embedding-model-azure-open-ai). This project
should serve as a useful reference for building your own integration.

You must have Java 11 or higher installed on your workstation along with [Gradle](https://gradle.org/) 8.x or
higher. The examples below assume that the `gradle` command is available on your path.

Begin by creating a new directory for your integration. Add a `build.gradle` file to the directory with the following
configuration in it:

```
plugins {
  id 'com.gradleup.shadow' version '8.3.3'
}

dependencies {
  // Change this to match the embedding model you wish to use. 
  // For example, if you wish to use Hugging Face, you would change it to:
  // implementation "dev:langchain4j:langchain4j-hugging-face:1.2.0-beta8".  
  implementation "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:1.2.0-beta8"
}
```

Your project will use the [Gradle Shadow plugin](https://gradleup.com/shadow/) to create a single JAR file containing
your code, the LangChain4j dependency, and the dependencies specific to your embedding model.

## Coding the integration

Next, create the directory structure `src/main/java` in your project. Then create a directory structure for the 
Java package name you wish to use. For this example, `org.example` will be used as the package name, so the 
directory structure `src/main/java/org/example` will be created.

Next, create a Java source file in the directory above. You can give it any name you wish. `MyEmbeddingModel` will be
used in this example. Then copy the below contents into the file, changing the package and class name as needed (this
would go into a file named `MyEmbeddingModel.java`):

```
package org.example;

import dev.langchain4j.model.embedding.EmbeddingModel;
import java.util.Map;
import java.util.function.Function;

public class MyEmbeddingModel implements Function<Map<String, String>, EmbeddingModel> {

    @Override
    public EmbeddingModel apply(Map<String, String> options) {
        // Will implement this method next.
    }
}
```

The `apply` method must return an instance of the LangChain4j `EmbeddingModel` interface. The method will receive
a map of options. The map of options is populated via the `-E` option described in the
"Configuring embedding model options" section in the [embedder usage guide](embedder.md). The `-E` option therefore 
allows a user to pass any necessary options to your class.

You are then free to implement the `apply` method any way you see fit. The
[Flux class for Azure OpenAI](https://github.com/marklogic/flux/blob/develop/flux-embedding-model-azure-open-ai/src/main/java/com/marklogic/flux/langchain4j/embedding/AzureOpenAiEmbeddingModelFunction.java)
demonstrates one approach of using LangChain4j and its support for an embedding model to return an instance of the
LangChain4j `EmbeddingModel` interface.

## Building the integration

Once you have implemented and tested your `apply` method, you can run the following Gradle command to build a single 
JAR file, which comprises the integration:

    ./gradle shadowJar

This will produce a single "shadow" JAR file in the `./build/libs` directory in your project. You can then copy that
JAR file to the `./ext` directory in your Flux installation.

## Using the integration

Assuming an implementation class name of `org.example.MyEmbeddingModel`, along with the integration JAR file having 
been copied to Flux's `./ext` directory, the integration can now be used via the following option:

    --embedder "org.example.MyEmbeddingModel"

If your integration accepts options, you can pass them via the `-E` option - for example:

    --embedder "org.example.MyEmbeddingModel" -Eoption1=value1 -Eoption2=value2

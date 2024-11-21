---
layout: default
title: Adding embeddings
parent: Importing Data
nav_order: 7
---

Flux supports adding embeddings - numerical representations of data - to JSON and XML documents. An embedding is 
associated with a "chunk" of text that is usually, but not necessarily, produced via Flux's 
[support for splitting text](splitting.md). Flux can add embeddings during 
any import operation and also when [copying documents](../copy.md). Adding embeddings to documents is a critical 
part of creating a data pipeline in support of 
[retrieval-augmented generation, or RAG](https://en.wikipedia.org/wiki/Retrieval-augmented_generation), 
use cases that utilize MarkLogic's support for 
[vector queries](https://docs.marklogic.com/12.0/guide/release-notes/en/new-features-in-marklogic-12-0-ea1/native-vector-support.html). 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Overview

Flux's support for adding embeddings to chunks depends on 
[LangChain4j's support for embedding models](https://docs.langchain4j.dev/tutorials/rag/#embedding-model). LangChain4j
defines an API for embedding models that allows Flux to be decoupled from any specific embedding model. 

LangChain4j provides [over a dozen implementations](https://docs.langchain4j.dev/category/embedding-models) of its
embedding model API. However, Flux does not ship with any of these implementations, as each embedding model 
implementation can include dozens of dependencies that can potentially conflict with each other. Consequently, a 
required step for using Flux's embedder feature is to include a JAR file in Flux's `./ext` directory that contains the 
LangChain4j embedding model implementation for your desired embedding model, along with its dependencies. Each 
[Flux release](https://github.com/marklogic/flux/releases), beginning with the 1.2.0 release, includes separate JAR 
files that can be downloaded and included in Flux for this purpose. This guide also covers how to construct your 
own JAR file for any LangChain4j-supported embedding model.

## Specifying an embedder

Flux's support for adding embeddings to chunks is activated by including the `--embedder` option, where "embedder"
refers to the Flux support for generating and adding embeddings. The value of this option must be one of the following:

1. The full name of a class that provides an instance of the [LangChain4j EmbeddingModel API](https://docs.langchain4j.dev/tutorials/rag/#embedding-model).
2. An abbreviation associated with an implementation of the above API. 

As of the 1.2.0 release, Flux recognizes the following abbreviations as a matter of convenience to avoid typing the full class name:

- `--embedder azure` refers to the embedding model provided by the `flux-embedding-model-azure-open-ai-(version).jar` file.
- `--embedder minilm` refers to the embedding model provided by the `flux-embedding-model-minilm-(version).jar` file.
- `--embedder ollama` refers to the embedding model provided by the `flux-embedding-model-ollama-(version).jar` file. 

The JAR files associated with each of the above abbreviations can be downloaded from the 
[Flux releases site](https://github.com/marklogic/flux/releases) and added to the `./ext` directory in a Flux 
installation. Adding the JAR file to the `./ext` directory is required in order for the desired embedding model to work
properly. Failure to do so will result in Flux throwing a `ClassNotFoundException`. 

Please see the bottom section in this guide for instructions on building your own JAR file containing support for any 
[LangChain4j embedding model](https://docs.langchain4j.dev/category/embedding-models).

## Configuring embedding model options

Each embedding model implementation provided by Flux can have options specified to configure the behavior of the 
embedding model. Embedding model options are defined via the following option, where `key` is the name of the option 
and `value` is the value of the option:

    -Ekey=value

You can use the `-E` option as many times as needed to configure the embedding model. 

For the options below that enable logging of requests and responses, you will need to edit the `conf/log4j2.properties`
file in your Flux installation and change the `logger.langchain4j.level` logger to have a value of `DEBUG`.

### Azure OpenAI options

The `flux-embedding-model-azure-open-ai-(version).jar` file uses 
[LangChain4's support](https://docs.langchain4j.dev/integrations/embedding-models/azure-open-ai) for 
[Azure OpenAI](https://azure.microsoft.com/en-us/products/ai-services/openai-service) and
supports the following options:

| Option | Description | 
| --- | --- |
| api-key | Required, unless using `non-azure-api-key`; used to authenticate with Azure OpenAI. |
| deployment-name | Required; the name of the Azure OpenAI deployment to use for embeddings. |
| endpoint | Required, unless using `non-azure-api-key`; the Azure OpenAI endpoint in the format: `https://{resource}.openai.azure.com/`. |
| dimensions | The number of dimensions in a generated embedding. |
| duration | Maximum duration, in seconds, of a request before timing out. |
| log-requests-and-responses | If set to `true`, enables logging of requests and responses. |
| max-retries | The number of retries to attempt when generating an embedding fails. |
| non-azure-api-key | Used to authenticate with the OpenAI service instead of Azure OpenAI. If set, the endpoint is automatically set to `https://api.openai.com/v1`. |

The following shows an example of configuring the Azure OpenAI embedding model with what are likely the most common
options to be used (the deployment names and endpoints are notional):

{% tabs log %}
{% tab log Unix %}
```
--embedder azure \
-Eapi-key=changeme \
-Edeployment-name=text-test-embedding-ada-002 \
-Eendpoint=https://gpt-testing.openai.azure.com
```
{% endtab %}
{% tab log Windows %}
```
--embedder azure ^
-Eapi-key=changeme ^
-Edeployment-name=text-test-embedding-ada-002 ^
-Eendpoint=https://gpt-testing.openai.azure.com
```
{% endtab %}
{% endtabs %}

### minilm options

The name "minilm" refers to LangChain4j's support for a [local embedding model](https://docs.langchain4j.dev/integrations/embedding-models/in-process)
with no external dependencies. This embedding model is primarily intended for prototyping and does not support 
any configuration options. 

### Ollama options

The `flux-embedding-model-ollama-(version).jar` file uses 
[langchain4j's support](https://docs.langchain4j.dev/integrations/embedding-models/ollama) for 
[Ollama](https://ollama.com/) and supports the following options:

| Option | Description | 
| --- | --- |
| base-url | Required; the Ollama base URL. Ollama defaults to `http://localhost:11434` when installed. |
| model-name | Required; the name of the [Ollama model](https://registry.ollama.com/search). |
| duration | Maximum duration, in seconds, of a request before timing out. |
| log-requests | If set to `true`, enables logging of requests. |
| log-responses | If set to `true`, enables logging of responses. |
| max-retries | The number of retries to attempt when generating an embedding fails. |

The following shows an example of configuring the Ollama embedding model with its required options, both of which
have notional values:

{% tabs log %}
{% tab log Unix %}
```
--embedder ollama \
-Ebase-url=http://localhost:11434 \
-Emodel-name=llama3.2
```
{% endtab %}
{% tab log Windows %}
```
--embedder ollama ^
-Ebase-url=http://localhost:11434 ^
-Emodel-name=llama3.2
```
{% endtab %}
{% endtabs %}


## Selecting chunks

In order for Flux to generate and add embeddings, it must know where to find "chunks" in a document. A document can 
contain one to many chunks. If Flux cannot find any chunks for a document, it will skip that document and not attempt
to generate any embeddings for it. 

### JSON documents

By default, Flux uses a [JSON Pointer](https://www.rfc-editor.org/rfc/rfc6901) expression of `/chunks` to find chunks
in a document. Flux expects that expression to point to either an array of objects or a single object. Each object is 
expected to have a key named `text` that contains the text to be used to generate an embedding for the chunk. These 
expectations align with the default behavior of Flux's [splitting feature](splitting.md).

You can configure a different JSON Pointer expression for selecting chunks in a JSON document via the following option:

    --embedder-chunks-json-pointer "/path/to/chunks"

You can configure a different key for the text to use for the embedding with the following option, which also
accepts a JSON Pointer expression:

    --embedder-text-json-pointer "/text-to-use"

For a JSON document containing text in a single top-level key named `description`, you would use the following options:

    --embedder-chunks-json-pointer ""
    --embedder-text-json-pointer "/description"

### XML documents

By default, Flux uses an [XPath expression](https://www.w3schools.com/xml/xpath_intro.asp) of `/node()/chunks` to 
find chunks in an XML document. The element at that path is expected to contain a child element per chunk. Each child
element - which can have any name and be in any namespace - is expected to contain its text to use for an embedding 
at a location defined by `node()[local-name(.) = 'text']`. These expectations align with the default behavior of 
Flux's [splitting feature](splitting.md).

You can configure a different XPath expression for selecting chunks in an XML document via the following option:

    --embedder-chunks-xpath "/path/to/chunks"

You can configure a different XPath expression for the text to use for the embedding with the following option:

    --embedder-text-xpath "path/to/text"

Both of the above expressions can use any number of XML namespace prefixes. Namespace prefixes can be registered via
the `-X` option with a format of `-Xprefix=namespaceURI`. The `-X` option can be used multiple times. 

For example, to use an option of `--embedder-chunks-xpath /ex1:parent/ex2:chunks`, you would include the following 
options to define the `ex1` and `ex2` namespace prefixes (the namespace URIs are notional and included only for sake
of example):

    -Xex1=org:example
    -Xex2=org:example2

## Configuring an embedding location

After generating an embedding for a chunk, Flux expects to store the embedding in a location relative to the chunk. The
following sections describe the default behavior for JSON and XML documents and how to customize the behavior. You can
also use a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms) to further control how
embeddings are stored in your documents. See the [guide on common import options](common-import-features.md) for more
information on transforms. 

### JSON documents

By default, Flux will store an embedding in an array named `embedding` that is added to the chunk object. Each value in 
the embedding will be added to this array. 

You can configure a different name for the array via the following option:

    --embedder-embedding-name custom-embedding-name

Flux will then create an array with the name provided to the option and store each embedding value in the array. 

### XML documents

By default, Flux will store an embedding in a new element named `embedding` that is added to the chunk element. The new
element will have an empty namespace value. The embedding array is serialized to a string - so that it begins with `[`
and ends with `]` - and stored as the text node of the `embedding` element. 

You can configure a different name for the embedding element via the following option:

    --embedder-embedding-name custom-element-name

You can configure a namespace for the embedding element via the following option:

    --embedder-embedding-namespace org:example:namespace

## Examples of splitting and embedding

Flux strives to optimize your data pipeline by supporting both splitting and embedding in the same import operation. 
This section shows an example of an import command that utilizes the options for splitting and embedding. The intent
is that this example can serve as a good starting point for your own import or copy command. You may then wish to use
some of the above embedding options or [splitting options](splitting.md) to customize how text is split and how 
embeddings are added. 

Consider the following requirements for splitting and adding embeddings to files as they are imported:

1. The input file is a zip of JSON documents. 
2. Each JSON document has text to be split in a top-level key named "description". 
3. Chunks should be stored in sidecar documents with a maximum chunk size of 500 and a maximum of 5 chunks per sidecar document. 
4. Chunk documents should be in a collection named "chunks" and should have the same permissions as the source documents.
5. The Azure OpenAI embedding model will be used to generate embeddings. 
6. To avoid having the Azure OpenAI API key on the command line, it will be read from an options file named `azure-api-key.txt`.

The `azure-api-key.txt` will have a single entry:

    -Eapi-key=the-azure-api-key

The files can be imported with splitting and embedding enabled using the following command (file path and MarkLogic 
connection string are notional):

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path path/to/file.zip \
    --compression zip \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update \
    --splitter-json-pointer "/description" \
    --splitter-max-chunk-size 500 \
    --splitter-sidecar-max-chunks 5 \
    --splitter-sidecar-collections chunks \
    --embedder azure \
    @azure-api-key.txt \
    -Edeployment-name=text-test-embedding-ada-002 \
    -Eendpoint=https://gpt-testing.openai.azure.com
```
{% endtab %}
{% tab log Windows %}
```
./bin/flux import-files ^
    --path path\to\file.zip ^
    --compression zip ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --splitter-json-pointer "/description" ^
    --splitter-max-chunk-size 500 ^
    --splitter-sidecar-max-chunks 5 ^
    --splitter-sidecar-collections chunks ^
    --embedder azure ^
    @azure-api-key.txt ^
    -Edeployment-name=text-test-embedding-ada-002 ^
    -Eendpoint=https://gpt-testing.openai.azure.com
```
{% endtab %}
{% endtabs %}

## Configuring embedder batch size

By default, Flux will send one request per chunk to the selected embedding model. If your embedding model supports
generating embeddings for multiple chunks of text in a single request, you can use the `--embedder-batch-size` option
to configure how many chunks will be included in a request. 

You can typically achieve much better performance when increasing the embedder batch size. However, you may more easily
run into rate limits imposed by your embedding model. For example, your embedding model may limit the number of tokens
it will process per minute. Increasing the embedder batch size may lead to this limit being hit more frequently. 

If you do hit a rate limit imposed by your embedding model, Flux may not be aware of it based on how your embedding
model handles rate limits. For example, your embedding model may choose to penalize the next request it receives by 
a certain number of seconds. Flux will not be aware of what the embedding model is doing. Flux will simply wait for
the embedding model to respond. It is therefore recommended to test various batch size values to find a setting
that both improves performance and minimizes the chance of a rate limit being reached. 

## Configuring logging

You can configure Flux to include logging specific to the generation of embeddings via the following process:

1. Open the `./conf/log4j2.properties` file in an editor. 
2. Adjust the `logger.marklogiclangchain.level` entry to have a value of `INFO` or `DEBUG`.


## Building your own embedding model JAR

As described in the "Overview" section above, each Flux release includes separate JAR files that provide integration 
with specific embedding models. 

If Flux does not yet provide a JAR file for your desired 
[LangChain4j-supported embedding model](https://docs.langchain4j.dev/category/embedding-models), you can build your own
JAR using the steps below. 

Before beginning, you may wish to inspect the project that Flux uses to build a 
[JAR file for Azure OpenAI](https://github.com/marklogic/flux/tree/develop/flux-embedding-model-azure-open-ai). This project
should serve as a useful reference for building your own integration. 

You must have Java 11 or higher installed on your workstation along with [Gradle](https://gradle.org/) 8.x or
higher. The examples below assume that the `gradle` command is available on your path. 

Begin by creating a new directory for your project. Add a `build.gradle` file to the directory with the following 
configuration in it:

```
plugins {
  id 'com.gradleup.shadow' version '8.3.3'
}

dependencies {
  // Change this to match the embedding model you wish to use. 
  // For example, if you wish to use Hugging Face, you would change it to:
  // implementation "dev:langchain4j:langchain4j-hugging-face:0.35.0".  
  implementation "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2:0.35.0"
}
```

Your project will use the [Gradle Shadow plugin](https://gradleup.com/shadow/) to create a single JAR file containing
your code, the LangChain4j dependency, and the dependencies specific to your embedding model. 

Next, create the directory structure `src/main/java`. Then create a directory structure for the Java package name you 
wish to use. For this example, `org.example` will be used as the package name, so the directory structure 
`src/main/java/org/example` will be created. 

Next, create a Java source file in the directory above. You can give it any name you wish. `MyEmbeddingModel` will be 
used in this example. Then copy the below contents into the file, changing the package and class name as needed:

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

The class must return an instance of the LangChain4j `EmbeddingModel` interface. The `apply` method will receive
a map of options. The map of options is populated via the `-E` option described in the 
"Configuring embedding model options" section above. The `-E` option therefore allows a user to pass any necessary options
to your class. 

You are then free to implement the `apply` method any way you see fit. The 
[Flux class for Azure OpenAI](https://github.com/marklogic/flux/blob/develop/flux-embedding-model-azure-open-ai/src/main/java/com/marklogic/flux/langchain4j/embedding/AzureOpenAiEmbeddingModelFunction.java)
can give you an idea of how to use LangChain4j and its support for your embedding model to return an instance of the
LangChain4j `EmbeddingModel` interface. 

Once you have implemented and tested your `apply` method, you can run the following Gradle command to build a single JAR file:

    ./gradle shadowJar

This will produce a single "shadow" JAR file in the `./build/libs` directory in your project. You can then copy that
JAR file to the `./ext` directory in your Flux installation. 

Assuming an implementation class name of `org.example.MyEmbeddingModel`, you can now utilize your class via the following
option:

    --embedder "org.example.MyEmbeddingModel"

If your implementation class accepts options, you can pass them via the `-E` option - for example:

    --embedder "org.example.MyEmbeddingModel" -Eoption1=value1 -Eoption2=value2

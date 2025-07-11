---
layout: default
title: Adding embeddings
parent: Importing Data
has_children: true
nav_order: 8
---

Flux supports adding embeddings - numerical representations of data - to JSON and XML documents. An embedding, also referred to as a "vector", is 
associated with a "chunk" of text that is usually, but not necessarily, produced via Flux's 
[support for splitting text](../splitting.md). Flux can add embeddings during 
any import operation and also when [copying documents](../../copy.md). Adding embeddings to documents is a critical 
part of creating a data pipeline in support of 
[retrieval-augmented generation, or RAG](https://www.progress.com/marklogic/solutions/generative-ai), 
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
LangChain4j integration for your desired embedding model, along with its dependencies. Each 
[Flux release](https://github.com/marklogic/flux/releases), beginning with the 1.2.0 release, includes separate JAR 
files that can be downloaded and included in Flux for this purpose. You can also 
[create your own integration](custom-integration.md) for any LangChain4j-supported embedding model.

## Specifying an embedder

Flux's support for adding embeddings to chunks is activated by including the `--embedder` option, where "embedder"
refers to the Flux support for generating and adding embeddings. The value of this option must be one of the following:

1. The full name of a class that provides an instance of the [LangChain4j EmbeddingModel API](https://docs.langchain4j.dev/tutorials/rag/#embedding-model).
2. An abbreviation associated with an implementation of the above API. 

As of the 1.2.0 release, Flux recognizes the following abbreviations as a matter of convenience to avoid typing the full class name:

- `--embedder azure` refers to the embedding model provided by the `flux-embedding-model-azure-open-ai-1.3.0.jar` file.
- `--embedder minilm` refers to the embedding model provided by the `flux-embedding-model-minilm-1.3.0.jar` file.
- `--embedder ollama` refers to the embedding model provided by the `flux-embedding-model-ollama-1.3.0.jar` file. 

The JAR files associated with each of the above abbreviations can be downloaded from the 
[Flux releases site](https://github.com/marklogic/flux/releases) and added to the `./ext` directory in a Flux 
installation. Adding the JAR file to the `./ext` directory is required in order for the desired embedding model to work
properly. Failure to do so will result in Flux throwing a `ClassNotFoundException`. 

Please see the guide on [creating your own integration](custom-integration.md) to utilize any embedding model supported
by LangChain4j.

## Configuring embedding model options

Each embedding model implementation provided by Flux can have options specified to configure the behavior of the 
embedding model. Embedding model options are defined via the following option, where `key` is the name of the option 
and `value` is the value of the option:

    -Ekey=value

You can use the `-E` option as many times as needed to configure the embedding model. 

For the options below that enable logging of requests and responses, you will need to edit the `conf/log4j2.properties`
file in your Flux installation and change the `logger.langchain4j.level` logger to have a value of `DEBUG`.

### Azure OpenAI options

The `flux-embedding-model-azure-open-ai-1.3.0.jar` file uses 
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
with no external dependencies. This embedding model is useful for testing vector queries with MarkLogic as it does not
require any setup or configuration.

### Ollama options

The `flux-embedding-model-ollama-1.3.0.jar` file uses 
[LangChain4j's support](https://docs.langchain4j.dev/integrations/embedding-models/ollama) for 
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
expectations align with the default behavior of Flux's [splitting feature](../splitting.md).

You can configure a different JSON Pointer expression for selecting chunks in a JSON document via the following option:

    --embedder-chunks-json-pointer "/path/to/chunks"

You can configure a different key for the text to use for the embedding with the following option, which also
accepts a JSON Pointer expression:

    --embedder-text-json-pointer "/text-to-use"

For example, consider a JSON document with chunks in the following structure:

```
{
  "metadata": {
    "custom-chunks": [
      {"text-to-embed": "Hello world"},
      {"text-to-embed": "More text"}
    ]
  }
}
```

The following options would be used to configure Flux - note that `--embedder-text-json-pointer` must start with `/`
to be a valid JSON Pointer expression (unless the expression is `""`):

```
--embedder-chunks-json-pointer "/metadata/custom-chunks"
--embedder-text-json-pointer "/text-to-embed"
```

For a JSON document containing text in a single top-level key named `description`, you would use the following options:

    --embedder-chunks-json-pointer ""
    --embedder-text-json-pointer "/description"

### XML documents

By default, Flux uses an [XPath expression](https://www.w3schools.com/xml/xpath_intro.asp) of `/node()/chunks` to 
find chunks in an XML document. The element at that path is expected to contain a child element per chunk. Each child
element - which can have any name and be in any namespace - is expected to contain its text to use for an embedding 
at a location defined by `node()[local-name(.) = 'text']`. These expectations align with the default behavior of 
Flux's [splitting feature](../splitting.md).

You can configure a different XPath expression for selecting chunks in an XML document via the following option:

    --embedder-chunks-xpath "/path/to/chunks"

You can configure a different XPath expression for the text to use for the embedding with the following option. Note
that this expression is relative to each element selected by `--embedder-chunks-xpath` and thus does not start with a 
`/`:

    --embedder-text-xpath "path/to/text"

Both of the above expressions can use any number of XML namespace prefixes. Namespace prefixes can be registered via
the `-X` option with a format of `-Xprefix=namespaceURI`. The `-X` option can be used multiple times. 

For example, to use an option of `--embedder-chunks-xpath /ex1:parent/ex2:chunks`, you would include the following 
options to define the `ex1` and `ex2` namespace prefixes (the namespace URIs are notional and included only for sake
of example):

    -Xex1=org:example -Xex2=org:example2

## Configuring an embedding location

After generating an embedding for a chunk, Flux expects to store the embedding in a location relative to the chunk. The
following sections describe the default behavior for JSON and XML documents and how to customize the behavior. You can
also use a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms) to further control how
embeddings are stored in your documents. See the [guide on common import options](../common-import-features.md) for more
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
bin\flux import-files ^
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

You can typically achieve much better performance when increasing the embedder batch size. However, you may more frequently
run into rate limits imposed by your embedding model. For example, your embedding model may limit the number of tokens
it will process per minute. Increasing the embedder batch size may lead to this limit being hit more frequently. 

If you do hit a rate limit imposed by your embedding model, Flux may not be aware of it based on how your embedding
model handles rate limits. For example, your embedding model may choose to penalize the next request it receives by 
a certain number of seconds. Flux will not be aware of what the embedding model is doing. Flux will simply wait for
the embedding model to respond. It is therefore recommended to test various batch size values to find a setting
that both improves performance and minimizes the chance of a rate limit being reached. 

## Encoding embeddings

By default, Flux stores embeddings as arrays of floating-point numbers in your documents. While this format is 
straightforward and human-readable, it can result in large documents when embeddings contain many dimensions. 
For example, a typical embedding with 1536 dimensions can add significant size to each document. 

To reduce document size, typically resulting in a document that is faster to load and index and query, you can configure Flux to encode embeddings into string values using the following option:

    --embedder-base64-encode

When this option is enabled, instead of storing an embedding as an array like this (the values shown below are notional):

```json
{
  "text": "MarkLogic is a database",
  "embedding": [0.1234, -0.5678, 0.9012, ...]
}
```

Flux will store the embedding as an encoded string:

```json
{
  "text": "MarkLogic is a database", 
  "embedding": "AAAAAAMAAADD9UhAH4XLP5qZKUA="
}
```

The encoding used by this option matches the encoding used by the `vec:base64-encode` function in the MarkLogic server.

### When to encode embeddings

Encoding provides significant benefits for most embedding use cases:

1. **Smaller documents**: Encoded embeddings result in smaller documents, reducing storage and indexing requirements in your MarkLogic database.

2. **Faster transmission**: Smaller documents transmit more quickly over networks, improving performance when loading data or retrieving documents with embeddings.

3. **More efficient indexing**: MarkLogic can index base64-encoded embeddings more efficiently than arrays of numbers, particularly for XML documents where the array is otherwise stored as a larger string.

For most production use cases, encoding is recommended due to its storage and performance benefits.


## Configuring a prompt

When generating embeddings for chunks of text with Flux 1.4.0 or higher, you can prepend a prompt to each chunk before 
sending it to the embedding model. This can be useful for providing context or instructions to the embedding model that 
may improve the quality and relevance of the generated embeddings for your specific use case.

To configure a prompt, use the following option:

    --embedder-prompt "Your prompt text here"

The prompt will be prepended to the text of each chunk before the embedding is generated. For example, if you have a 
chunk with the text "MarkLogic is a database" and specify a prompt of "Represent this text for retrieval:", the 
embedding model will receive the combined text "Represent this text for retrieval: MarkLogic is a database".

### When to use a prompt

Consider using a prompt in the following scenarios:

1. **Domain-specific context**: Provide context about the domain or subject matter of your text to help the embedding 
   model generate more appropriate embeddings. For example: `--embedder-prompt "This is a medical document:"`

2. **Retrieval optimization**: Some embedding models perform better when given explicit instructions about how the 
   embeddings will be used. For example: `--embedder-prompt "Represent this document for semantic search:"`

3. **Task-specific instructions**: Guide the embedding model to focus on specific aspects of the text that are 
   important for your use case. For example: `--embedder-prompt "Focus on the technical concepts in this text:"`

4. **Language or format hints**: Provide hints about the language or format of the content when processing mixed 
   content. For example: `--embedder-prompt "This is legal text in English:"`

### Important considerations

- **Embedding model dependency**: The effectiveness of prompts varies significantly between different embedding models. 
  Some models are specifically designed to work with prompts, while others may ignore them or produce unexpected results.

- **Prompt consistency**: Use the same prompt for all documents that you intend to search together using vector queries. 
  Inconsistent prompting across documents in the same collection may lead to reduced search quality.

- **Testing recommended**: The impact of prompts on embedding quality depends on your specific data and use case. It's 
  recommended to test different prompts with a small subset of your data to evaluate their effectiveness before 
  processing large datasets.

- **Token limits**: Be aware that the prompt will be added to each chunk, potentially increasing the total token count 
  sent to the embedding model. This may affect performance and costs, especially for models with token-based pricing.

## Configuring logging

You can configure Flux to include logging specific to the generation of embeddings via the following process:

1. Open the `./conf/log4j2.properties` file in an editor. 
2. Adjust the `logger.marklogiclangchain.level` entry to have a value of `INFO` or `DEBUG`.


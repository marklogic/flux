---
layout: default
title: Splitting text
parent: Importing Data
nav_order: 6
---

Flux supports splitting the text in documents into chunks of configurable size, either written to the source document
or to separate "sidecar" documents containing one or more chunks. Flux can split text during any import operation and also 
when [copying documents](../copy.md). Splitting text is often a critical part of creating a data pipeline in support
of [retrieval-augmented generation, or RAG](https://en.wikipedia.org/wiki/Retrieval-augmented_generation), use cases with MarkLogic.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Selecting text to split

In order to split text into chunks, you must first configure Flux to select the text in a document that you wish to 
split. Flux supports three approaches - using one or more JSON Pointer expressions, using an XPath expression, and 
using all the text in a document. Flux does not yet support splitting text in binary documents. 

### Using JSON Pointer expressions

For JSON documents, you can select text to split by specifying one or more [JSON Pointer](https://www.rfc-editor.org/rfc/rfc6901) 
expressions via the `--splitter-json-pointer` option.

As an example, consider a JSON document containing the following content:

```
{
  "summary": "A summary of the text in this document",
  "content": {
    "description: "A larger amount of text"
  }
}
```

You can select the text in the `description` field via:

    --splitter-json-pointer "/content/description"

You can also select the text in both fields via:

    --splitter-json-pointer "/summary" --splitter-json-pointer "/content/description"

### Using an XPath expression

For XML documents, you can select text to split by specifying an [XPath expression](https://en.wikipedia.org/wiki/XPath) via the `--splitter-xpath` option. 
As a single XPath expression can be used to select text in multiple locations, this option only accepts a single value. 

As an example, consider an XML document with the following content:

```
<root>
  <summary>A summary of the text in this document.</summary>
  <content>
    <description>A much larger amount of text is here.</description>
  </content>
</root>
```

You can select the text in the `description` element via:

    --splitter-xpath "/root/content/description/text()"

You can select the text in both the `summary` and `description` elements via:

    --splitter-xpath "/root/node()[self::summary or self::description[ancestor::content]]/text()"

Note the use of `/text()` to select the text node of an element. If `/text()` is omitted, the element will be selected 
and serialized to a string. For example, the following will serialize the entire document to a string and use that
output as the text to be split:

    --splitter-xpath "/root"

When constructing an XPath expression for the value of `--splitter-xpath`, you may need to specify one or more 
XML namespace prefixes and URIs. You can do so via the `-X` option, where the value is of the 
pattern:

    -Xprefix=URI

For example, for an XPath expression of "/ex:root/ex:text", where the "ex" prefix is associated with the namespace 
"org:example", you would include the following option:

    -Xex=org:example

### Using all the text in a document

To select all the text in a document, use the following option:

    --splitter-text

You can use this option on JSON and XML documents in addition to text documents, in which case the JSON or XML document 
will be treated as a string, including all braces and tags. 

## Configuring how text is split

Flux uses the [LangChain4j framework](https://docs.langchain4j.dev/intro/) to support splitting the text in 
documents. Via LangChain4j, Flux supports 3 options for splitting text:

1. A default approach that splits on paragraphs, sentences, lines, and words.
2. A regex-based approach that splits text based on a regex pattern.
3. A custom splitter, written by a user that implements a LangChain4j API. 

### Default splitter

The default splitter in Flux is used when you select text to split and do not configure either the regex or custom
splitter approaches. This splitter provides two options to control its output:

1. `--splitter-max-chunk-size` controls the maximum size in characters of a chunk, with a default value of 1000. 
2. `--splitter-max-overlap-size` controls the maximum overlap in characters between two consecutive chunks, with a default value of 0. 

When splitting text in documents to support a RAG use case, the max chunk size is often critical in ensuring that your 
RAG solution does not send too much text in one request to your Large Language Model (LLM). 

### Regex splitter

The regex splitter in Flux is used when specifying a [regular expression, or regex](https://en.wikipedia.org/wiki/Regular_expression)
via the `--splitter-regex` option. The regex splits the selected text into parts, with as many parts being added to a
chunk without exceeding the value specified via `--splitter-max-chunk-size`, which defaults to 1000 characters. Parts
are joined together by a single space; this can be overridden via the `--splitter-join-delimiter` option. 

The `--splitter-max-chunk-size` and `--splitter-max-overlap-size` options, as described above for the default splitter, 
can both be used with the regex splitter. 

### Custom splitter

The default and regex approaches described above are intended for quickly implementing a splitting solution. There
are many other splitting strategies available, including those supported by LLMs. Flux will likely support additional
strategies out-of-the-box in the future. In the meantime, you can implement your own splitter via the steps below.

For a working example of a custom splitter, please see [this example project](https://github.com/marklogic/flux/tree/develop/flux-custom-splitter-example)
in the Flux GitHub repository.

To use a custom splitter, you will need to create an implementation of the 
[LangChain4j DocumentSplitter interface](https://docs.langchain4j.dev/apidocs/dev/langchain4j/data/document/DocumentSplitter.html).
You are free to do that in any manner you wish, though you will likely want to use either [Maven](https://maven.apache.org/) 
or [Gradle](https://gradle.org/) as your build tool. 

As Flux currently depends on LangChain4j 0.35.0, you should use the same version of LangChain4j when implementing your
custom splitter. Note that LangChain4j 0.36.0 and higher require the use of Java 17. 

As your custom splitter may need one or more configuration options, your implementation of a LangChain4j `DocumentSplitter` must have
a public constructor that accepts a `java.util.Map<String, String> options` argument. You will be able to provide 
options to your splitter via a Flux command line option described below. 

The outline of your splitter should look similar to this:

```
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;
import java.util.Map;

public class CustomSplitter implements DocumentSplitter {

    public CustomSplitter(Map<String, String> options) {
        // Use the options as you see fit. 
    }
    
    @Override
    public List<TextSegment> split(Document document) {
        // Add your code here for splitting the text in the document into a list of text segments (chunks).
    }
}
```

Once you have implemented and tested your splitter, you will need to package it and its dependencies into a "shadow"
or "uber" jar - i.e. a single jar file that contains your code and all of its dependencies. If you are using Gradle, 
consider using the [Gradle Shadow plugin](https://github.com/GradleUp/shadow). If you are using Maven, consider 
using the [Maven Shade plugin](https://maven.apache.org/plugins/maven-shade-plugin/). 

After constructing a shadow jar, copy it to the `./ext` folder in your Flux distribution. This adds your custom splitter
to the Flux classpath. 

Finally, to use your custom splitter, use the following command line options:

1. `--splitter-custom-class` must specify the full class name of your splitter implementation - e.g. `org.example.MySplitter`.
2. Use `-Skey=value` as many times as you wish to pass key/value pairs to the constructor of your splitter. 

## Configuring how chunks are stored

Flux supports two approaches for storing chunks that have been split from the selected text in a source document:

1. Chunks are added to the source document.
2. Chunks are added to one or more "sidecar" documents that each contain one or more chunks and have a reference to the source document. 

### Storing chunks in the source document

For JSON and XML source documents, Flux defaults to storing chunks in the source document itself. For Text source
documents, Flux only supports writing sidecar documents, which are described in the next section of this guide. 

#### JSON source documents

Given a JSON source document, Flux will add the following:

1. A top-level `chunks` array.
2. Each chunk is added to that array as an object with a single `text` field. 

If a top-level field named `chunks` already exists, Flux will instead use the name `splitter-chunks`.

For example, given the source document:

```
{
  "content": "A large amount of text."
}
```

Flux will modify the source document in the following manner (the manner in which the text is split is not important in 
this example):

```
{
  "content": "A large amount of text.",
  "chunks": [
    {"text": "A large amount"},
    {"text": "of text."}
  ]
}
```

#### XML source documents

Given an XML source document, Flux will add the following:

1. A child element under the root element named "chunks" with a namespace of `http://marklogic.com/appservices/model`. 
2. Each chunk is added as a child element of the "chunks" element named "chunk" with a single "text" child element.

If child element of the root element already exists with the name `chunks`, Flux will instead use the 
name `splitter-chunks`.

For example, given the source document:

```
<root>
  <content>A large amount of text.</content>
</root>
```

Flux will modify the source document in the following manner (the manner in which the text is split is not important in
this example):

```
<root>
  <content>A large amount of text.</content>
  <chunks xmlns='http://marklogic.com/appservices/model'>
    <chunk><text>A large amount</text></chunk>
    <chunk><text>of text.</text></chunk>
  </chunks>
</root>
```

### Storing chunks in sidecar documents

You can configure Flux to store chunks in one or more "sidecar" documents (i.e. a separate document that has a reference
to the source document) via the following option:

    --splitter-sidecar-max-chunks (positive number)

The option both enables the use of sidecar documents and defines the maximum number of chunks to write to a single 
sidecar document. For example, given a source document that produces 10 chunks, a value of 3 for the above option 
would result in 4 sidecar documents. The first 3 sidecar documents would each have 3 chunks, and the 4th sidecar 
document would have 1 chunk. You can also ensure that only one sidecar document is written by giving the option a 
number higher than the maximum number of chunks in any of your documents. 

#### Controlling the document type 

Flux will create sidecar documents of the same document type as the source document. You can instead force a document
type via the following option, which accepts either `JSON` or `XML` as a value:

    --splitter-sidecar-document-type JSON
    --splitter-sidecar-document-type XML

#### Controlling JSON document content

By default, Flux will create each JSON sidecar document using the following structure:

```
{
  "source-uri": "The URI of the source document",
  "chunks": [
    {"text": "first chunk"},
    {"text": "second chunk"},
    etc...
  ]
}
```

You can include a root field in the document via:

    --splitter-sidecar-root-name nameOfRootField

A root field is often useful for making each document more self-descriptive. For example, if your chunks are split from
a document representing a chapter in a book, you could use `--splitter-sidecar-root-name chapter-chunks` to produce the
following document:

```
{
  "chapter-chunks": {
    "source-uri": "The URI of the source document",
    "chunks": [
      {"text": "first chunk"},
      {"text": "second chunk"},
      etc...
    ]
  }
}
```

For further customization, consider using a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms) 
via the `--transform` option for your import command or `--output-transform` option for the `copy` command.

#### Controlling XML document content

By default, Flux will create each XML sidecar document using the following structure:

```
<root>
  <source-uri>The URI of the source document</source-uri>
  <chunks>
    <chunk><text>The first chunk</text></chunk>
    <chunk><text>The second chunk</text></chunk>
    etc...
  </chunk>
</root>
```

You can override the name of the root element - `root` - via:

    --splitter-sidecar-root-name nameOfRootElement

You can also specify a namespace for the root element which will apply to the entire document via:

    --splitter-sidecar-xml-namespace org:example

For example, using the above two options will produce the following sidecar document:

```
<nameOfRootElement xmlns="org:example">
  <source-uri>The URI of the source document</source-uri>
  <chunks>
    <chunk><text>The first chunk</text></chunk>
    <chunk><text>The second chunk</text></chunk>
    etc...
  </chunk>
</nameOfRootElement>
```

For further customization, consider using a [MarkLogic REST transform](https://docs.marklogic.com/guide/rest-dev/transforms)
via the `--transform` option for your import command or `--output-transform` option for the `copy` command.

#### Controlling document URIs

Each sidecar document will have a URI with the following pattern:

    (source document URI)-chunks-(counter).(json|xml)

The `(counter)` defaults to 1 and is incremented for each sidecar document. 

You can instead generate a URI consisting of a prefix, a UUID, and a suffix via the following options:

    --splitter-sidecar-uri-prefix "/chunk/"
    --splitter-sidecar-uri-suffix ".json"

The above options will result in documents having URIs of `/chunk/(UUID).json`.

#### Controlling document metadata

Each sidecar document defaults to the same permissions as its source document and no collections assigned to it. 
You can assign different permissions via the following, where each "role" is the name of a MarkLogic and each 
"capability" is one of "read", "update", "insert", or "execute":

    --splitter-sidecar-permissions role,capability,role,capability,etc.

You can assign collections to each sidecar document via the following, which accepts a comma-delimited sequence of 
collection names:

    --splitter-sidecar-collections collection1,collection2,etc


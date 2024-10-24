---
layout: default
title: Splitting text in documents
parent: Importing Data
nav_order: 6
---

Flux supports splitting the text in documents into chunks of configurable size, either written to the source document
or to separate documents containing one or more chunks. Flux can split text during any import operation and also 
when [copying documents](../copy.md). Splitting text is often a critical part of creating a data pipeline in support
of [retrieval-augmented generation, or RAG](https://en.wikipedia.org/wiki/Retrieval-augmented_generation).

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Selecting text to split

In order to split text into chunks, you must first configure Flux to select the text in a document that you wish to 
split. Flux supports three approaches - using one or more JSON Pointer expressions, using an XPath expression, and 
using all the text in a document. Note that Flux does not support splitting text in binary documents. 

### Using JSON Pointer expressions

If your source documents are JSON, you can specify one or more [JSON Pointer](https://www.rfc-editor.org/rfc/rfc6901) 
expressions via the `--splitter-json-pointer` option. 

As an example, consider a JSON document containing at least the following content:

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

If your source documents are XML, you can specify an [XPath expression](https://en.wikipedia.org/wiki/XPath) for 
selecting the text via the `--splitter-xml-xpath` option. As a single XPath expression can be used to select text
in multiple locations, this option only accepts a single value. 

As an example, consider an XML document with at least the following content:

```
<root>
  <summary>A summary of the text in this document.</summary>
  <content>
    <description>A much larger amount of text is here.</description>
  </content>
</root>
```

You can select the text in the `description` element via:

    --splitter-xml-xpath "/root/content/description/text()"

You can select the text in both the `summary` and `description` elements via:

    --splitter-xml-xpath "/root/node()[self::summary or self::description[ancestor::content]]/text()"

Note the use of `/text()` to select the text node of an element. If you omit this, the element will be selected 
and serialized to a string. For example, the following will serialize the entire document to a string and use that
output as the text to be split:

    --splitter-xml-xpath "/root"

When constructing an XPath expression for the value of `--splitter-xml-xpath`, you may need to specify one or more 
XML namespace prefixes and URIs. You can do so via the `--splitter-xml-namespace` option, where the value is of the 
pattern:

    --splitter-xml-namespace prefix=URI

For example, for an XPath expression of "/ex:root/ex:text", where the "ex" prefix is associated with the namespace 
"org:example", you would need to include the following option:

    --splitter-xml-namespace ex=org:example

### Using all the text in a document

For Text documents - i.e. documents that are not JSON or XML and contain unstructured or semi-structured text - use
the following option to specify that all the text in a document should be split:

    --splitter-text

You can use this option as well on JSON and XML documents, in which case the JSON or XML document will be treated as 
a string, including all braces and tags. 

## Configuring how text is split

Flux uses the popular [langchain4j framework](https://docs.langchain4j.dev/intro/) to support splitting the text in 
documents. Via langchain4j, Flux supports 3 options for splitting text:

1. A default approach that splits on paragraphs, sentences, lines, and words.
2. A regex-based approach that splits text based on a regex pattern.
3. A custom splitter, written by a user that implements a langchain4j API. 

### Default splitter

The default splitter in Flux is used when you select text to split and do not configure either the regex or custom
splitter approaches. This splitter provides two options to control its output:

1. `--splitter-max-chunk-size` controls the maximum size in characters of a chunk, with a default value of 1000. 
2. `--splitter-max-overlap-size` controls the maximum overlap in characters between two consecutive chunks, with a default value of 0. 

When splitting text in documents to support a RAG use case, the max chunk size is often critical in ensuring that your 
RAG solution does not send too much text in one request to your LLM. 

### Regex splitter

The regex splitter in Flux is used when specifying a [regular expression, or regex](https://en.wikipedia.org/wiki/Regular_expression)
via the `--splitter-regex` option. The regex splits the selected text into parts, with as many parts being added to a
chunk without exceeding the value specified via `--splitter-max-chunk-size`, which defaults to 1000 characters. Parts
are joined together by a single space; this can be overridden via the `--splitter-join-delimiter` option. 

The `--splitter-max-chunk-size` and `--splitter-max-overlap-size` options, as described above for the default splitter, 
can both be used with the regex splitter as well. 

### Custom splitter

The default and regex approaches described above are intended for quickly getting a splitting solution in place. There
are many other splitting strategies available, including those supported by LLMs. Flux will likely support additional
strategies out-of-the-box in the future. In the meantime, you can implement your own splitter via the steps below.

To use a custom splitter, you will need to create an implementation of the 
[langchain4j DocumentSplitter interface](https://docs.langchain4j.dev/apidocs/dev/langchain4j/data/document/DocumentSplitter.html).
You are free to do that in any manner you wish, though you will likely want to use either [Maven](https://maven.apache.org/) 
or [Gradle](https://gradle.org/) as your build tool. 

As Flux currently depends on langchain4j 0.35.0, you should use the same version of langchain4j when implementing your
custom splitter.

As your custom splitter may need one or more configuration options, your implementation of `DocumentSplitter` must have
a public constructor that accepts a `java.util.Map<String, String> options` argument. You will be able to provide 
options to your splitter via a Flux command line option described below. 

The outline of your splitter should thus look similar to this:

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
2. Use `--splitter-custom-option key=value` as many times as you wish to pass key/value pairs to the constructor of your splitter. 


## Configuring how chunks are stored

TBD in next PR. 

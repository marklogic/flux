---
layout: default
title: Classifying text
parent: Importing Data
nav_order: 7
---

Flux 1.3 introduces support for [classifying text via Progress Semaphore](https://www.progress.com/semaphore). Classifying
text can provide a number of benefits to your application:

1. Automatically assign categories or tags to documents for easier organization and retrieval.
2. Enhance search capabilities by indexing documents with relevant metadata.
3. Save time by reducing the need for manual tagging or categorization.
4. Use classified text for downstream analytics like trend analysis or sentiment analysis.

This feature is particularly useful for organizations managing large-scale document ingestion and processing.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Configuring the classifier

You can use the classifier options with any import command, as any document written to MarkLogic can have the text 
within it classified. You can also use it when [copying documents](../copy.md) from one MarkLogic database to another.

Use of a Semaphore classifier is enabled by including at least the `--classifier-host` option. When this option is 
included, along with any other options described in the below table, Flux will invoke the classifier for each document
and will add the result to the corresponding document. 

The table below lists each of the options used to configure how the classifier is used by Flux:
 
| Option | Description |
|---|---|
| `--classifier-host`        | Specify the hostname of the classifier service.  | 
| `--classifier-port`        | Specify the port number of the classifier service. | 
| `--classifier-http`        | Use HTTP instead of HTTPS for the classifier service. | 
| `--classifier-path`        | Specify the path of the classifier service. | 
| `--classifier-api-key`     | Provide the API key for accessing the classifier service when hosted in Progress Data Cloud. | 
| `--classifier-token-path`  | Specify the path to the token generator for the classifier service when hosted in Progress Data Cloud. | 
| `--classifier-batch-size`  | Set the number of documents or text chunks to send in a single request to the classifier. Defaults to 20. | 
| `--classifier-prop key=value` | Specify additional options for configuring the behavior of the classifier service. |

As an example, the following options - applicable to any import command - would result in the text of each document 
to be written to MarkLogic having classification added to it, generated via Semaphore:

{% tabs log %}
{% tab log Unix %}
```
--classifier-host example.org \
--classifier-port 443 \
--classifier-path "/cls/dev/cs1/"
```
{% endtab %}
{% tab log Windows %}
```
--classifier-host example.org ^
--classifier-port 443 ^
--classifier-path "/cls/dev/cs1/" 
```
{% endtab %}
{% endtabs %}

## Configuring batch processing

The performance of the Semaphore classifier is typically improved by having Flux send more than one document at a time
to the classifier. Flux defaults to sending the text of up to 20 documents in each request to the classifier. You 
can control this via the `--classifier-batch-size` option. 

The appropriate value for this option will depend on the size of your documents and the performance characteristics of
your Semaphore instance. For small documents, you may find improved performance by increasing the batch size to a 
higher number, and vice versa for larger documents.

## Configuring the behavior of the classifier service

The `--classifier-prop` option allows for passing options that affect the behavior of the classifier service. The
[Semaphore documentation](https://portal.smartlogic.com/docs/5.6/classification_server_-_developers_guide/calling_classification_server), 
which requires logging in to the Semaphore portal, provides a list of the options that you can pass. 

As Flux uses the "multi-article" support for configuring batch processing, you should not set either the 
`singlearticle` or `multiarticle` options. 

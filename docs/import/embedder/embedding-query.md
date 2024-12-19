---
layout: default
title: Querying on embeddings
parent: Adding embeddings
grand_parent: Importing Data
nav_order: 3
---

Once you have used Flux to add embeddings to chunks in your documents, you can follow the 
[MarkLogic documentation on vector queries](https://docs.marklogic.com/12.0/guide/release-notes/en/new-features-in-marklogic-12-0-ea1/native-vector-support.html)
to learn how to query on these embeddings. 

This guide will help you get started by showing a working example that uses Flux to split text and add embeddings 
to chunks, followed with an example [Optic query](https://docs.marklogic.com/11.0/guide/optic-guide/en/getting-started-with-optic.html)
on the embeddings. 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Setup

If you would like to try these examples yourself, you'll need access to an instance of
[MarkLogic 12.0 EA1](https://docs.marklogic.com/12.0/guide/release-notes/en/release-notes.html) or later. Version 
12.0 EA1 is the first version of MarkLogic to support vector queries. 

Additionally, you'll need to deploy the Getting Started example application to the MarkLogic instance per the 
instructions in the [Getting Started guide](../../getting-started.md).

This example uses the ["minilm" embedding model](https://docs.langchain4j.dev/integrations/embedding-models/in-process). 
This embedding model is useful for testing vector queries with MarkLogic as it has no external dependencies and requires
no setup or configuration. To use this embedding model, you must include the `flux-embedding-model-minilm-1.2.0.jar`
in the `./ext` directory of your Flux installation. Please see the [embedder guide](embedder.md) for more information 
on embedding models. 

## Importing the data

The Flux command below will perform the following steps:

1. Import the zip file at `data/marklogic-security-docs.zip` in the Getting Started example application directory.
2. Split the text document into many chunks, each with the default maximum chunk size of 1000 characters. 
3. Use the `minilm` embedding model to add an embedding to each chunk.
4. Write each chunk to its own document in a collection named `chunks`.

Per the [Getting Started guide](../../getting-started.md), you should have a copy of Flux installed in your Getting 
Started project directory. If you have Flux installed elsewhere, you should modify the value of the `--path` option
in the below command to point to the location of the `data/marklogic-security-docs.zip` file. 

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path ../data/marklogic-security-docs.zip \
    --compression zip \
    --connection-string "flux-example-user:password@localhost:8004" \
    --collections docs \
    --permissions flux-example-role,read,flux-example-role,update \
    --uri-replace ".*zip,''" \
    --splitter-xpath "//xhtml:p" \
    -Xxhtml=http://www.w3.org/1999/xhtml \
    --splitter-sidecar-max-chunks 1 \
    --splitter-sidecar-collections "chunks,docs-chunks" \
    --embedder minilm
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path ..\data\marklogic-security-docs.zip ^
    --compression zip ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --collections plays ^
    --permissions flux-example-role,read,flux-example-role,update ^
    --uri-replace ".*zip,''" ^
    --splitter-xpath "//xhtml:p" ^
    -Xxhtml=http://www.w3.org/1999/xhtml ^
    --splitter-sidecar-max-chunks 1 ^
    --splitter-sidecar-collections "chunks" ^
    --embedder minilm
```
{% endtab %}
{% endtabs %}

After running this command, you can access your MarkLogic [Query Console application](https://docs.marklogic.com/guide/qconsole/walkthru),
authenticating as `flux-example-user` with a password of `password` (this user was created when you deployed the 
example application and was granted access to Query Console). After selecting `flux-example-content` as the database, 
you should see 365 documents in the `docs` collection - one for each entry in the zip file - and 591 chunk documents in the 
`chunks` collection. Each chunk document has the following structure (the full text and embedding values have been 
omitted for brevity):

```
{
  "source-uri": "/security-guide/en/element-level-security/configuring-element-level-security/protected-paths/examples-of-protected-paths.html",
  "chunks": [
    {
      "text": "This table shows some examples of protected paths...",
      "embedding": [-0.0011005516, -0.04141206, ...]
    }
  ]
}
```

## Querying on the chunks

A typical RAG - Retrieval Augmented Generation - approach with MarkLogic involves the following steps:

1. Generate an embedding representing a user's question. 
2. Find the documents with embeddings most similar to the embedding representing the user's question, optionally 
leveraging additional MarkLogic indexes to find the most relevant documents.

The following query demonstrates a simple RAG approach, using a vector generated via the `minilm` embedding model 
representing a user's question of "How do I remove a protected path in MarkLogic?". 
You can run this in Query Console as the `flux-example-user` user. You must first select `flux-example-content` as
the database and `JavaScript` as the query type. 

```
const op = require("/MarkLogic/optic");

// A vector representing the question "How do I remove a protected path in MarkLogic?".
const vector = [0.04448475, 0.0876867, -0.016342573, -0.04240541, 0.015850063, -0.005312396, 0.007949237, 0.023670807, -0.021829598, 0.014271442, 0.013014746, 7.450793E-4, 0.043139078, 0.008070213, 0.007401134, 0.029959979, -0.02749421, 0.0050475593, 0.09281992, 0.02386617, 0.04589778, 0.048440754, 0.0502936, -0.019994352, -0.01735496, -0.12785858, 0.03345533, 0.06794369, -0.030383268, -0.013172505, 0.03485373, -0.02912624, -0.014535486, -0.041172642, 0.11092156, 0.09187697, 0.029616253, -0.007799574, 0.047151733, 0.029626407, -0.014644015, -0.0114322, -0.06020052, -0.047506947, -0.080600254, -0.055852585, 0.028733345, -0.051896542, -0.02814144, 0.059477143, 0.021337075, 0.0052722692, -0.083158575, -0.008834242, 0.009964292, 0.0068625603, -0.086964, 0.05483181, -0.012226233, -0.048850305, -0.008503479, 0.08470826, 0.013438191, 0.023715345, -0.038440675, -0.007101753, -0.010723726, 0.07076123, 0.06547668, 0.04261773, -0.004636733, -0.019520236, 0.0064494815, -0.08536026, 0.05432453, 0.08376711, -0.018434023, 0.05824971, 0.08060236, -0.15590852, -4.1310917E-4, -0.057559144, 0.016890189, 0.0779283, -0.02310604, 0.06018625, 0.045166083, 5.031848E-4, 0.16565432, 0.038344674, 0.08651619, -0.09369411, 0.023815751, -0.010488427, 0.0017233577, -0.059090108, -0.012303789, 0.033526286, 0.02491367, 0.0457004, -0.0055292435, -0.0433725, 0.05845098, 0.058072817, 0.02675867, 0.022352148, -0.040789872, -0.023234587, -0.03641237, -0.016252533, 0.015003288, 0.0026499259, -0.020727552, -0.018649168, -0.028496694, 0.010893961, -0.02272028, 0.054394864, -0.0416997, 0.019766511, -0.029020369, -0.046071034, 0.022918815, 0.014884701, -0.0049738036, 0.013125326, 0.07513772, -5.7004254E-33, -0.024983099, 0.0029508485, 0.0036673995, -0.10153321, 0.030411478, -0.008964367, 0.017935203, 9.5415744E-4, -0.14103201, 0.03146228, 0.08716944, -0.06672602, -0.010079625, -0.0059582205, 0.0155715365, 0.031497717, 0.0052678105, -0.0062867445, -0.038195614, 0.003905529, 0.0054950933, 0.078374974, -0.06417738, -0.016880782, -0.018370826, 0.01667308, -0.00761127, 0.034013666, 0.05912897, 0.037647616, 0.013460455, -0.02365719, 0.02760696, 0.09657234, 0.010732468, 0.09186886, -1.3663444E-4, 0.0074819857, -0.025293859, -0.03682305, 0.051113445, -0.06693588, 0.03178061, 0.018828813, 0.044244856, -0.008971187, -0.07295263, -0.05911373, -0.059663355, -0.02105301, -0.011890778, 0.033970095, 0.07238913, -0.082768455, -0.09147456, -0.10237062, -0.036933053, 0.0017054917, -0.03754901, -0.0516384, 0.03449767, 0.09032885, 0.022582958, 0.024874657, -0.028827468, -0.06230907, 0.006569375, -0.0415356, 0.0045126607, -0.01555832, -0.090334885, 0.038899288, 0.022580456, -0.008468197, -0.032318104, -0.031123504, -0.038014065, -0.025938787, 8.08067E-4, -0.025521921, -0.13466498, -0.052372698, -0.08271735, 0.030641135, 0.102673635, -0.09065244, -0.041558135, 0.005418378, -0.010592389, -0.08718628, 0.1237908, -0.016305057, -0.08172576, 0.06738809, -0.020179287, 1.816791E-33, 0.014915897, 0.020103866, 0.06713202, -0.015505714, -0.11480037, -0.04088607, 0.041642025, 0.028892616, -0.058900874, 0.005848602, -0.0071721803, 0.05637159, -0.036332984, -0.005346833, 0.0059723835, -0.06355509, -0.04428219, 0.03824091, -0.12808853, 0.010889337, -0.011476562, 0.036350645, 0.028965522, 0.09602309, -0.0037856393, -0.04699685, 0.04522956, 0.06545701, 0.023350231, 0.024842089, 0.005638969, 0.042513285, -0.04714476, -0.13049486, -0.030644553, -0.054283276, -0.024588436, 0.014570363, -0.031784195, -0.042697255, 0.024127591, -0.010254703, -0.03307041, 0.0124379, 0.017937286, 0.093057066, 0.1046171, 0.024220191, 0.0344596, 0.03343368, 0.027160699, -0.020108528, 0.10950813, -0.048192624, 0.025314337, 0.07827576, -0.01193656, -0.032457046, -0.038840245, -0.024558168, 0.043750472, 0.09714118, -0.019005412, 0.055665094, 0.07367106, -0.04127962, -0.062886, 0.05714727, -0.08162652, -0.09057847, -0.030912139, 0.08603325, -0.01619725, -0.022324188, 0.13877554, -0.0012676752, -0.080113865, -0.037753288, -0.039155647, 0.013734819, 0.022553954, -0.046986997, -0.039534286, 0.0059739137, 0.02111048, -0.060340635, -0.08569863, -0.049381267, -0.079645574, 0.013368437, 0.047417756, 0.07900417, -0.01901812, 0.02293634, -0.13507518, -1.5823765E-8, -0.0028638782, -0.018036049, -0.018239107, -0.08174, 0.037714075, 0.0055549787, -0.009142379, 0.078224465, 0.046093006, 0.050184075, 0.036244355, 0.0020754838, -0.0066427942, 0.00854857, -0.054177936, -0.04575361, 0.019326381, 0.0069793714, -0.08723825, -0.051880304, -0.083343506, -0.04465201, 0.0023928212, 0.052228794, 0.044576626, -0.04866949, 0.018686196, -0.042572424, -0.003154837, 0.015528477, -0.03155757, 0.0015431228, -0.014331048, 0.09231661, -0.065462776, 0.06583986, 0.053692263, 0.05827847, 0.056524165, 0.1453482, -0.006488764, 0.012093219, 0.024091994, 0.013625253, -0.037876207, -0.052456096, 0.046840195, 0.042694904, 0.01085709, 0.02084866, -0.044661924, 0.028147047, -0.06810696, -0.07263803, -0.06285884, -0.030951371, 0.018044282, 0.03331054, -0.050651226, 0.027251903, -0.012845206, -0.032191265, 0.07416453, -0.008432913]

op.fromSearchDocs(cts.andQuery([
    cts.collectionQuery("chunks"),
    cts.wordQuery(["remove", "protected", "path", "MarkLogic"])
  ]), null, {"scoreMethod": "bm25"})
  .orderBy("score")
  .limit(20)
  .bind(op.as("chunkEmbedding", op.vec.vector(op.xpath("doc", "/chunks/embedding"))))
  .bind(op.as("similarity", op.vec.cosineSimilarity(op.col("chunkEmbedding"), op.vec.vector(vector))))
  .orderBy(op.desc("similarity"))
  .limit(10)
  .bind(op.as("chunkText", op.xpath("doc", "/chunks/text")))
  .select(["uri", "chunkText"])
  .result()
```

The above Optic query first performs a simple word search to find the 20 most relevant chunk documents using 
MarkLogic's universal text index. It then uses Optic to compare the embedding in each chunk document to the embedding
representing the user's question. The 10 most relevant chunks are returned in descending order of similarity. 

In a RAG query pipeline, these 10 chunks of text would be sent to a Large Language Model (LLM) that would use the text
to provide a more meaningful response. For example, given a pipeline similar to the one shown in the 
[MarkLogic AI Examples repository](https://marklogic.github.io/marklogic-ai-examples/), an LLM would provide 
a response similar to the following, effectively summarizing the text found in the chunks along with its existing
knowledge of MarkLogic:

```
To remove a protected path in MarkLogic, it is a two-step process. First, you need to 
unprotect the path, which removes the permissions and disables the protection. Then, you 
can remove the path. After unprotecting the path, you can click the delete button on the 
Protected Path Configuration page to remove the path.
```

Without RAG, LLM responses may include incorrect information; for example:

```
To remove a protected path in MarkLogic, you can use the `xdmp:document-remove-permissions` 
function. This function takes the URI of the document as its first argument and the path 
expression as its second argument. Once executed, the protected path will be removed from 
the document and will no longer be subject to any permissions or security settings.
```

LLM responses can vary widely, but a RAG approached that utilizes MarkLogic's indexes and vector query support
can greatly increase the quality of responses. 

## Querying with a view

As shown in the [MarkLogic documentation for vector queries](https://docs.marklogic.com/12.0/guide/release-notes/en/new-features-in-marklogic-12-0-ea1/native-vector-support.html), 
you may instead wish to project chunks as rows via a [MarkLogic TDE template](https://docs.marklogic.com/guide/app-dev/TDE).
You can then use an Optic query to first select documents via a search, and then join chunk rows to access their embeddings. 

To do so, you will first need a TDE template. The following template examples provide starting points based on the 
default JSON and XML data structures produced by Flux when splitting text and adding embeddings to chunks. 

Before loading these templates, you should verify and possibly change at least the following items:

1. The value of `collections` is for example only; Flux does not impose a particular collection on chunk documents. Change
this to match your data or replace it with a different scope.
2. Adjust the schema and view names to any values you prefer.
3. The `dimension` value for the `embedding` column needs to match that of the embedding model you choose. For
convenience, Flux will log this value with a message containing "Using embedding model with dimension: (number of dimensions)".

{% tabs tde %}
{% tab tde JSON %}
```
{
  "template": {
    "context": "/chunks",
    "collections": [
      "chunks"
    ],
    "rows": [
      {
        "schemaName": "example",
        "viewName": "chunks",
        "columns": [
          {
            "name": "uri",
            "scalarType": "string",
            "val": "xdmp:node-uri(.)"
          },
          {
            "name": "text",
            "scalarType": "string",
            "val": "text"
          },
          {
            "name": "embedding",
            "scalarType": "vector",
            "val": "vec:vector(embedding)",
            "dimension": "384",
            "invalidValues": "ignore",
            "nullable": false
          }
        ]
      }
    ]
  }
}
```
{% endtab %}
{% tab tde XML %}
```
<template xmlns="http://marklogic.com/xdmp/tde">
  <context>/node()/chunks/chunk</context>
  <collections>
    <collection>xml-chunks</collection>
  </collections>
  <rows>
    <row>
      <schema-name>example</schema-name>
      <view-name>xml_chunks</view-name>
      <columns>
        <column>
          <name>uri</name>
          <scalar-type>string</scalar-type>
          <val>xdmp:node-uri(.)</val>
        </column>
        <column>
          <name>text</name>
          <scalar-type>string</scalar-type>
          <val>text</val>
        </column>
        <column>
          <name>embedding</name>
          <scalar-type>vector</scalar-type>
          <val>vec:vector(embedding)</val>
          <dimension>384</dimension>
          <invalid-values>ignore</invalid-values>
          <nullable>false</nullable>
        </column>
      </columns>
    </row>
  </rows>
</template>
```
{% endtab %}
{% endtabs %}

Note that a TDE template does not need have the same document type as the documents from which it projects rows. The
above examples are intended primarily to show what the template definition should look like using either JSON or XML 
for defining the template. 

If you wish to try out either template with the Getting Started example application, perform the following steps:

1. Copy the template to a file in the `./src/main/ml-schemas/tde` directory in the Getting Started project directory. 
You can name the file anything you wish - e.g. `chunks.json` -  though you should use `.json` or `.xml` as a suffix 
for the file depending on which template you choose to copy. 
2. From the Getting Started project directory, run `./gradlew mlreloadschemas`. 

You can then run the following query in Query Console as the `flux-example-user` user. It depends on the JSON 
template shown above but can be easily altered if you chose to use the XML template. You must first select 
`flux-example-content` as the database and `JavaScript` as the query type.

```
const op = require("/MarkLogic/optic");

// A vector representing the question "How do I remove a protected path in MarkLogic?".
const vector = [0.04448475, 0.0876867, -0.016342573, -0.04240541, 0.015850063, -0.005312396, 0.007949237, 0.023670807, -0.021829598, 0.014271442, 0.013014746, 7.450793E-4, 0.043139078, 0.008070213, 0.007401134, 0.029959979, -0.02749421, 0.0050475593, 0.09281992, 0.02386617, 0.04589778, 0.048440754, 0.0502936, -0.019994352, -0.01735496, -0.12785858, 0.03345533, 0.06794369, -0.030383268, -0.013172505, 0.03485373, -0.02912624, -0.014535486, -0.041172642, 0.11092156, 0.09187697, 0.029616253, -0.007799574, 0.047151733, 0.029626407, -0.014644015, -0.0114322, -0.06020052, -0.047506947, -0.080600254, -0.055852585, 0.028733345, -0.051896542, -0.02814144, 0.059477143, 0.021337075, 0.0052722692, -0.083158575, -0.008834242, 0.009964292, 0.0068625603, -0.086964, 0.05483181, -0.012226233, -0.048850305, -0.008503479, 0.08470826, 0.013438191, 0.023715345, -0.038440675, -0.007101753, -0.010723726, 0.07076123, 0.06547668, 0.04261773, -0.004636733, -0.019520236, 0.0064494815, -0.08536026, 0.05432453, 0.08376711, -0.018434023, 0.05824971, 0.08060236, -0.15590852, -4.1310917E-4, -0.057559144, 0.016890189, 0.0779283, -0.02310604, 0.06018625, 0.045166083, 5.031848E-4, 0.16565432, 0.038344674, 0.08651619, -0.09369411, 0.023815751, -0.010488427, 0.0017233577, -0.059090108, -0.012303789, 0.033526286, 0.02491367, 0.0457004, -0.0055292435, -0.0433725, 0.05845098, 0.058072817, 0.02675867, 0.022352148, -0.040789872, -0.023234587, -0.03641237, -0.016252533, 0.015003288, 0.0026499259, -0.020727552, -0.018649168, -0.028496694, 0.010893961, -0.02272028, 0.054394864, -0.0416997, 0.019766511, -0.029020369, -0.046071034, 0.022918815, 0.014884701, -0.0049738036, 0.013125326, 0.07513772, -5.7004254E-33, -0.024983099, 0.0029508485, 0.0036673995, -0.10153321, 0.030411478, -0.008964367, 0.017935203, 9.5415744E-4, -0.14103201, 0.03146228, 0.08716944, -0.06672602, -0.010079625, -0.0059582205, 0.0155715365, 0.031497717, 0.0052678105, -0.0062867445, -0.038195614, 0.003905529, 0.0054950933, 0.078374974, -0.06417738, -0.016880782, -0.018370826, 0.01667308, -0.00761127, 0.034013666, 0.05912897, 0.037647616, 0.013460455, -0.02365719, 0.02760696, 0.09657234, 0.010732468, 0.09186886, -1.3663444E-4, 0.0074819857, -0.025293859, -0.03682305, 0.051113445, -0.06693588, 0.03178061, 0.018828813, 0.044244856, -0.008971187, -0.07295263, -0.05911373, -0.059663355, -0.02105301, -0.011890778, 0.033970095, 0.07238913, -0.082768455, -0.09147456, -0.10237062, -0.036933053, 0.0017054917, -0.03754901, -0.0516384, 0.03449767, 0.09032885, 0.022582958, 0.024874657, -0.028827468, -0.06230907, 0.006569375, -0.0415356, 0.0045126607, -0.01555832, -0.090334885, 0.038899288, 0.022580456, -0.008468197, -0.032318104, -0.031123504, -0.038014065, -0.025938787, 8.08067E-4, -0.025521921, -0.13466498, -0.052372698, -0.08271735, 0.030641135, 0.102673635, -0.09065244, -0.041558135, 0.005418378, -0.010592389, -0.08718628, 0.1237908, -0.016305057, -0.08172576, 0.06738809, -0.020179287, 1.816791E-33, 0.014915897, 0.020103866, 0.06713202, -0.015505714, -0.11480037, -0.04088607, 0.041642025, 0.028892616, -0.058900874, 0.005848602, -0.0071721803, 0.05637159, -0.036332984, -0.005346833, 0.0059723835, -0.06355509, -0.04428219, 0.03824091, -0.12808853, 0.010889337, -0.011476562, 0.036350645, 0.028965522, 0.09602309, -0.0037856393, -0.04699685, 0.04522956, 0.06545701, 0.023350231, 0.024842089, 0.005638969, 0.042513285, -0.04714476, -0.13049486, -0.030644553, -0.054283276, -0.024588436, 0.014570363, -0.031784195, -0.042697255, 0.024127591, -0.010254703, -0.03307041, 0.0124379, 0.017937286, 0.093057066, 0.1046171, 0.024220191, 0.0344596, 0.03343368, 0.027160699, -0.020108528, 0.10950813, -0.048192624, 0.025314337, 0.07827576, -0.01193656, -0.032457046, -0.038840245, -0.024558168, 0.043750472, 0.09714118, -0.019005412, 0.055665094, 0.07367106, -0.04127962, -0.062886, 0.05714727, -0.08162652, -0.09057847, -0.030912139, 0.08603325, -0.01619725, -0.022324188, 0.13877554, -0.0012676752, -0.080113865, -0.037753288, -0.039155647, 0.013734819, 0.022553954, -0.046986997, -0.039534286, 0.0059739137, 0.02111048, -0.060340635, -0.08569863, -0.049381267, -0.079645574, 0.013368437, 0.047417756, 0.07900417, -0.01901812, 0.02293634, -0.13507518, -1.5823765E-8, -0.0028638782, -0.018036049, -0.018239107, -0.08174, 0.037714075, 0.0055549787, -0.009142379, 0.078224465, 0.046093006, 0.050184075, 0.036244355, 0.0020754838, -0.0066427942, 0.00854857, -0.054177936, -0.04575361, 0.019326381, 0.0069793714, -0.08723825, -0.051880304, -0.083343506, -0.04465201, 0.0023928212, 0.052228794, 0.044576626, -0.04866949, 0.018686196, -0.042572424, -0.003154837, 0.015528477, -0.03155757, 0.0015431228, -0.014331048, 0.09231661, -0.065462776, 0.06583986, 0.053692263, 0.05827847, 0.056524165, 0.1453482, -0.006488764, 0.012093219, 0.024091994, 0.013625253, -0.037876207, -0.052456096, 0.046840195, 0.042694904, 0.01085709, 0.02084866, -0.044661924, 0.028147047, -0.06810696, -0.07263803, -0.06285884, -0.030951371, 0.018044282, 0.03331054, -0.050651226, 0.027251903, -0.012845206, -0.032191265, 0.07416453, -0.008432913]

op.fromSearchDocs(cts.andQuery([
    cts.collectionQuery("chunks"),
    cts.wordQuery(["remove", "protected", "path"])
  ]), null, {"scoreMethod": "bm25"})
  .orderBy("score")
  .limit(50)
  .joinInner(
    op.fromView("example", "chunks", "", op.fragmentIdCol("chunkFragmentId")),
    op.on(op.fragmentIdCol("fragmentId"), op.fragmentIdCol("chunkFragmentId"))
  )
  .bind(op.as("similarity", op.vec.cosineSimilarity(op.col("embedding"), op.vec.vector(vector))))
  .orderBy(op.desc("similarity"))
  .limit(10)
  .select(["uri", "text"])
  .result()
```

---
layout: default
title: Importing aggregate XML
parent: Importing files
grand_parent: Importing Data
nav_order: 5
---

Flux can split large XML files - called "aggregate XML files" - in the same fashion as 
[MarkLogic Content Pump](https://docs.marklogic.com/11.0/guide/mlcp-guide/en/importing-content-into-marklogic-server/splitting-large-xml-files-into-multiple-documents.html). 

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Usage

The `import-aggregate-xml-files` command creates many XML documents based on a particular XML element name and
optional namespace. The command requires an instance of the `--element` option to identify the name of the XML element
in each file where each occurrence of that XML element will result in a separate document being written to MarkLogic. 
The `--namespace` option must be used if that XML element has an associated namespace.

For example, consider the following notional XML file located at `/data/people.xml`:

```
<people xmlns="org:example">
  <person>
    <id>1</id>
    <first>George</first>
    <last>Washington</last>
  </person>
  <person>
    <id>2</id>
    <first>Betsy</first>
    <last>Ross</last>
  </person>
</people>
```

The following command uses `--element` and `--namespace` to create an XML document in MarkLogic for each occurrence
of the `person` element in the document:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-xml-files \
    --path /data/people.xml \
    --element person \
    --namespace org:example \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-xml-files ^
    --path data\people.xml ^
    --element person ^
    --namespace org:example ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}


## Controlling document URIs

In addition to the options for controlling URIs described in the [common import features guide](../common-import-features.md), 
you can use the `--uri-element` and `--uri-namespace` options to identify an element in each XML document whose value should
be included in the URI. Using the example XML document in the above section, the following would construct a URI 
based on the value of each `id` element in the `org:example` namespace:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-xml-files \
    --path /data/people.xml \
    --element person \
    --namespace org:example \
    --uri-element id \
    --uri-namespace org:example \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-xml-files ^
    --path data\people.xml ^
    --element person ^
    --namespace org:example ^
    --uri-element id ^
    --uri-namespace org:example ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

You may still wish to use options like `--uri-prefix` and `--uri-suffix` to make the URI more self-describing. For
example, adding the following would result in URIs of `/person/1.xml` and `/person/2.xml`:

    --uri-prefix "/person/" --uri-suffix ".xml"

## Compressed XML files

Flux supports Gzip and ZIP aggregate XML files. Simply include the `--compression` option with a value of `GZIP` or 
`ZIP`.

## Specifying an encoding

MarkLogic stores all content 
[in the UTF-8 encoding](https://docs.marklogic.com/guide/search-dev/encodings_collations#id_87576).
If your aggregate XML files use a different encoding, you must specify that via the `--encoding` option so that 
the content can be correctly translated to UTF-8 when written to MarkLogic - e.g.:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-aggregate-xml-files \
    --path source \
    --encoding ISO-8859-1 \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-aggregate-xml-files ^
    --path source ^
    --encoding ISO-8859-1 ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

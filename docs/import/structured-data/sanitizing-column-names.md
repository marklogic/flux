---
layout: default
title: Sanitizing column names
parent: Structured data sources
grand_parent: Importing Data
nav_order: 4
---

Structured data sources - CSV files, databases accessed via JDBC, and so on - often contain column names with
spaces, punctuation, or other characters that are perfectly valid for the source system but can cause problems once
those column names become JSON field names, XML element names, or [TDE](tde-generation.md) column names in
MarkLogic. Use the `--column-name-strategy` option to automatically sanitize column names before documents are
constructed.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Supported commands

The `--column-name-strategy` option is available for the following commands:

- [`import-avro-files`](../import-files/avro.md)
- [`import-delimited-files`](../import-files/delimited-text.md)
- [`import-jdbc`](../import-jdbc.md)
- [`import-orc-files`](../import-files/orc.md)
- [`import-parquet-files`](../import-files/parquet.md)

## Usage

`--column-name-strategy` accepts the following values:

- `none` (the default) - column names are left unchanged, preserving existing behavior.
- `simple` - first, Western European Latin letters are folded to their closest ASCII letter(s) - e.g. `é` becomes
  `e`, `ö` becomes `o`, and `ß` becomes `ss` - see [Folding Western European letters](#folding-western-european-letters)
  below for details. Then, any leading or trailing run of characters that is not an ASCII letter, digit, or
  underscore is removed, and each remaining such run is replaced with a single underscore. Underscores in the
  original column name are always preserved, as they may be meaningful. For example, `first. name` becomes
  `first_name`, ` name ` becomes `name`, `_id` and `a__b` are unchanged, and `prénom` becomes `prenom`.

For example, to sanitize column names when importing a CSV file:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-delimited-files \
    --path customers.csv \
    --column-name-strategy simple \
    --connection-string "flux-example-user:password@localhost:8004" \
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-delimited-files ^
    --path customers.csv ^
    --column-name-strategy simple ^
    --connection-string "flux-example-user:password@localhost:8004" ^
    --permissions flux-example-role,read,flux-example-role,update
```
{% endtab %}
{% endtabs %}

A column named `single'quote` becomes `single_quote`, and a column named `has/slash` becomes `has_slash`, in the
resulting documents.

## Folding Western European letters

Before replacing unsupported characters with underscores, the `simple` strategy folds Western European Latin
letters to their closest ASCII letter(s):

- Accented letters are folded to their base letter, regardless of whether the source text represents them as a
  single precomposed character or as a base letter plus a combining accent - e.g. `é`, `ö`, `ü`, `ñ`, and `ç` become
  `e`, `o`, `u`, `n`, and `c` respectively.
- A small set of Latin letters that don't have a single-letter ASCII equivalent are mapped explicitly: `æ` becomes
  `ae`, `œ` becomes `oe`, `ß` becomes `ss`, `ø` becomes `o`, `ð` becomes `d`, and `þ` becomes `th` (each with the
  corresponding uppercase mapping, e.g. `Æ` becomes `AE`). Accented forms of these letters, such as `ǿ` and `ǽ`,
  fold the same way, e.g. `ǿ` becomes `o` and `ǽ` becomes `ae`.

This folding is language-neutral, not a language-specific spelling convention. For example, German text
conventionally spells `ö` as `oe` and `ß` as `ss` when ASCII-only text is required, but Flux always folds `ö` to
`o`; only `ß` happens to match the German convention. A column named `Müller` becomes `Muller`, and a column named
`straße` becomes `strasse`.

Any character without an ASCII equivalent - including characters from non-Latin scripts - is not folded and is
instead replaced with an underscore, along with any other unsupported characters, as described above.

## Combining with other features

Column name sanitization is applied last, after [`--where`](filtering-data.md), [`--drop`](filtering-data.md#dropping-columns),
and [`--group-by` / `--aggregate` / `--aggregate-order-by`](aggregating-rows.md). This means that `--where`, `--drop`,
`--group-by`, `--aggregate`, and `--aggregate-order-by` must all reference the **original**, unsanitized column
names. Only features that operate on the final set of documents - such as `--uri-template` and
[TDE template generation](tde-generation.md) - see the sanitized column names.

## Limitations

- Only top-level column names are sanitized. Field names nested inside a struct or array - such as the fields within
  a column produced by `--aggregate` - are not modified.
- The `simple` strategy only preserves ASCII letters, digits, and underscores, after first folding Western European
  Latin letters as described above. Characters with no ASCII equivalent, including non-Latin scripts, are replaced,
  so a column name consisting entirely of such characters sanitizes to an empty name and results in an error.
- Because leading and trailing unsupported characters are removed, a column such as ` id ` becomes `id`. If the
  source also has a column named `id`, the two names collide and Flux raises an error.
- The `simple` strategy does not guarantee that a resulting name is a valid XML element name. For example, a column
  name of `123abc` sanitizes to `123abc`, which is not a valid XML element name because it starts with a digit. If
  you are generating XML documents, ensure your source column names will not sanitize to a name starting with a
  digit.
- If the `simple` strategy would produce an empty column name (for example, a column named `...`) or would produce
  the same name for two different columns (a collision), Flux raises an error rather than writing documents with
  ambiguous or missing field names. Rename the offending source columns, or use `--drop` to remove them, before
  reapplying the strategy.

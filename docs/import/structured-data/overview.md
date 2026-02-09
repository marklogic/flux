---
layout: default
title: Structured data sources
parent: Importing Data
nav_order: 4
has_children: true
---

Flux provides specialized support for importing data from structured data sources, where data is organized into rows and columns. These sources include:

- [Avro files](../import-files/avro.md)
- [Delimited text files](../import-files/delimited-text.md) (CSV, TSV, etc.)
- [JDBC-accessible databases](../import-jdbc.md)
- [ORC files](../import-files/orc.md)
- [Parquet files](../import-files/parquet.md)

When working with structured data sources, Flux offers several powerful features to transform and enhance your data during import:

## Common features

- **[Filtering rows](filtering-rows.md)** - Use SQL-like WHERE expressions to select specific rows before importing
- **[Aggregating rows](aggregating-rows.md)** - Combine related rows to create hierarchical JSON or XML documents
- **[Generating TDE templates](tde-generation.md)** - Automatically create Template Driven Extraction templates to make imported data available for relational queries

These features work consistently across all structured data sources, allowing you to apply the same transformation patterns whether you're importing from CSV files, databases, or other formats.

## Basic import patterns

When importing from structured data sources, each row is typically written as a separate JSON or XML document in MarkLogic. For JSON documents, you can use `--json-root-name` to wrap row data in a root object. For XML documents, use `--xml-root-name` (and optionally `--xml-namespace`) to structure the output.

You can also use `--ignore-null-fields` to exclude fields with null values from the generated documents.

See the documentation for each specific data source for usage examples and format-specific options.

---
layout: default
title: Selecting files
parent: Importing files
grand_parent: Importing Data
nav_order: 1
---

Every command for importing data from files must specify one or more paths from which files should be read. The details
of specifying paths are described below.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Specifying paths

The `--path` option controls where files are read from. You can specify multiple occurrences of `--path`, each with a 
different value, to import files from many sources in a single command invocation:

    --path relative/path/to/files --path /absolute/path/to/files

The value of the `--path` option can be any directory or file path. You can use wildcards in any part of the path. For
example, the following, would select every file starting with `example` in any child directory of the root `/data`
directory:

    --path /data/*/example*

## Reading from S3

Flux can read files from S3 via a path expression of the form `s3a://bucket-name/optional/path`.

In most cases, Flux must use your AWS credentials to access an S3 bucket. Flux uses the AWS SDK to fetch credentials from 
[locations supported by the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-authentication-short-term.html). 
To enable this, include the `--s3-add-credentials` option:

```
./bin/flux import-files \
    --path "s3a://my-bucket/some/path" \
    --s3-add-credentials \
    --connection-string etc... 
```

You can also explicitly define your AWS credentials via `--s3-access-key-id` and `--s3-secret-access-key`. To avoid 
typing these in plaintext, you may want to store these in a file and reference the file via an options file. See
[Common Options](../../common-options.md) for more information on how to use options files.

You can also specify an S3 endpoint via `--s3-endpoint`. This is typically required when running Flux in AWS in one 
region while trying to access S3 in a separate region. 

## Ignoring child directories

By default, child directories of each directory specified by `--path` are included. To prevent this, include the following
option:

    --recursive-file-lookup false

## Filtering files

You can restrict which files are read from a directory by specifying a standard
[glob expression](https://en.wikipedia.org/wiki/Glob_(programming)) via the `--filter` option:

    --path /data/examples --filter some*.json

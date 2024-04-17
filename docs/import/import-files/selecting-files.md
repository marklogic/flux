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
different value, to import files from many sources. 

The value of the `--path` option can be any directory or file path. You can use wildcards in any part of the path. For
example, the following, would select every file starting with `example` in any child directory of the root `/data`
directory:

    --path /data/*/example*

## Reading from S3

NT can read files from S3 via a path expression of the form `s3a://bucket-name/optional/path`.

In most cases, NT must use your AWS credentials to access an S3 bucket. NT uses the AWS SDK to fetch credentials from 
[locations supported by the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-authentication-short-term.html). 
To enable this, include the `--s3AddCredentials` option:

```
./bin/nt import_files --path "s3a://my-bucket/some/path" --s3AddCredentials
```

## Ignoring child directories

By default, child directories of each directory specified by `--path` are included. To prevent this, include the following
option:

    --recursiveFileLookup false

## Filtering files

You can restrict which files are read from a directory by specifying a standard
[glob expression](https://en.wikipedia.org/wiki/Glob_(programming)) via the `--filter` option:

    --path /data/examples --filter some*.json


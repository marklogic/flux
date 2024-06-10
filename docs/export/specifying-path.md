---
layout: default
title: Specifying a path
parent: Exporting Data
nav_order: 1
---

## Specifying a path

Every command for exporting data to files must specify a path to which files should be written. The `--path` option 
is used to specify the path. The value of the `--path` option can be any valid directory, and it must already exist.

## Exporting to S3

NT can export files to S3 via a path expression of the form `s3a://bucket-name/optional/path`.

In most cases, NT must use your AWS credentials to access an S3 bucket. NT uses the AWS SDK to fetch credentials from
[locations supported by the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-authentication-short-term.html).
To enable this, include the `--s3-add-credentials` option:

```
./bin/nt export-files --path "s3a://my-bucket/some/path" --s3-add-credentials
```

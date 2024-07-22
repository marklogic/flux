---
layout: default
title: Specifying a path
parent: Exporting Data
nav_order: 1
---

## Specifying a path

Every command for exporting data to files must specify a path to which files should be written via the `--path` option. 
The value of the `--path` option can be any valid directory or S3 bucket, and it must already exist.

## Exporting to S3

Flux can export files to S3 via a path expression of the form `s3a://bucket-name/optional/path`.

In most cases, Flux must use your AWS credentials to access an S3 bucket. Flux uses the AWS SDK to fetch credentials from
[locations supported by the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-authentication-short-term.html).
To enable this, include the `--s3-add-credentials` option:

```
./bin/flux export-files --path "s3a://my-bucket/some/path" --s3-add-credentials
```

You can also explicitly define your AWS credentials via `--s3-access-key-id` and `--s3-secret-access-key`. To avoid
typing these in plaintext, you may want to store these in a file and reference the file via "@my-options.txt". See
the documentation on [Common Options](../common-options.md) for more information.

You can also specify an S3 endpoint via `--s3-endpoint`. This may be required when running Flux in AWS in one region
while trying to access S3 in a separate region. 

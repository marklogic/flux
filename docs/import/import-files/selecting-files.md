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

Each command that imports from files requires the use of the `--path` option with at least one value - for example:

{% tabs log %}
{% tab log Unix %}
```
--path path/to/files
```
{% endtab %}
{% tab log Windows %}
```
--path path\to\files
```
{% endtab %}
{% endtabs %}


You can include multiple values for the `--path` option, which can utilize both relative and absolute file paths:

{% tabs log %}
{% tab log Unix %}
```
--path relative/path/to/files /absolute/path/to/files
```
{% endtab %}
{% tab log Windows %}
```
--path relative\path\to\files C:\absolute\path\to\files
```
{% endtab %}
{% endtabs %}

The value of the `--path` option can be any directory or file path. You can use wildcards in any part of the path. For
example, the following, would select every file starting with `example` in any child directory of the root `/data`
directory:

{% tabs log %}
{% tab log Unix %}
```
--path data/*/example*
```
{% endtab %}
{% tab log Windows %}
```
--path data\*\example*
```
{% endtab %}
{% endtabs %}


## Filtering files

You can restrict which files are read from a directory by specifying a standard
[glob expression](https://en.wikipedia.org/wiki/Glob_(programming)) via the `--filter` option:

{% tabs log %}
{% tab log Unix %}
```
--path data/examples --filter "example*.json"
```
{% endtab %}
{% tab log Windows %}
```
--path data\examples --filter "example*.json"
```
{% endtab %}
{% endtabs %}


Depending on your shell environment, you may need to include the value of `--filter` in double quotes as shown above to
ensure that each asterisk is interpreted correctly. However, if you include `--filter` in an options file as 
described in [Common Options](../../common-options.md), you do not need double quotes around the value. 

## Ignoring child directories

By default, child directories of each directory specified by `--path` are included. To prevent this, include the following
option:

    --recursive-file-lookup false

## Importing from S3

Flux can read files from S3 via a path expression of the form `s3a://bucket-name/optional/path`.

In most cases, Flux must use your AWS credentials to access an S3 bucket. Flux uses the AWS SDK to fetch credentials from 
[locations supported by the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-authentication-short-term.html). 
To enable this, include the `--s3-add-credentials` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path "s3a://my-bucket/some/path" \
    --s3-add-credentials \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path "s3a://my-bucket/some/path" ^
    --s3-add-credentials ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}


You can also explicitly define your AWS credentials via `--s3-access-key-id` and `--s3-secret-access-key`. To avoid 
typing these in plaintext, you may want to store these in a file and reference the file via an options file. See
[Common Options](../../common-options.md) for more information on how to use options files.

You can also specify an S3 endpoint via `--s3-endpoint`. This is typically required when running Flux in AWS in one 
region while trying to access S3 in a separate region. 

## Importing from Azure Storage

Flux can read files from [Azure Storage](https://docs.microsoft.com/en-us/azure/storage/common/storage-introduction) using either [Azure Blob Storage](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction) or [Azure Data Lake Storage Gen2](https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction).  

The first step is configuring authentication to your Azure Storage account. Once authenticated, you can use simple 
relative file paths and Flux will automatically construct the full Azure Storage URLs for you.

The examples below use notional values for options that contain credentials. To avoid typing credentials in plaintext, 
consider storing them in [an options file](../../common-options.md). 

### Blob Storage Authentication

Azure Blob Storage supports two authentication methods:

**Access Key Authentication**

If you are using [access key authentication](https://docs.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage), 
you can define a key via the `--azure-access-key` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path "reports" \
    --azure-storage-account "mystorage" \
    --azure-container-name "mycontainer" \
    --azure-access-key "your-access-key" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path "reports" ^
    --azure-storage-account "mystorage" ^
    --azure-container-name "mycontainer" ^
    --azure-access-key "your-access-key" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

**SAS Token Authentication**

If you are using [SAS (Shared Access Signature) tokens](https://docs.microsoft.com/en-us/azure/storage/common/storage-sas-overview), 
you can define a token via the `--azure-sas-token` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path "reports" \
    --azure-storage-account "mystorage" \
    --azure-container-name "mycontainer" \
    --azure-sas-token "your-sas-token" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path "reports" ^
    --azure-storage-account "mystorage" ^
    --azure-container-name "mycontainer" ^
    --azure-sas-token "your-sas-token" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

> Note that `--azure-container-name` is required when using SAS token authentication.

### Data Lake Storage Authentication

Azure Data Lake Storage uses [shared key authentication](https://docs.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage). 
You can define a shared key via the `--azure-shared-key` option. You must also include `--azure-storage-type DATA_LAKE`
to indicate that you are using Data Lake Storage instead of Blob Storage:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path "reports" \
    --azure-storage-account "mydatalake" \
    --azure-container-name "analytics" \
    --azure-storage-type "DATA_LAKE" \
    --azure-shared-key "your-shared-key" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path "reports" ^
    --azure-storage-account "mydatalake" ^
    --azure-container-name "analytics" ^
    --azure-storage-type "DATA_LAKE" ^
    --azure-shared-key "your-shared-key" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

### Path handling

Flux provides two ways to specify file paths. First, when both `--azure-storage-account` and `--azure-container-name`
are specified and the path is relative - i.e. it does not contain a protocol like `wasbs://` or `abfss://` - Flux will
construct the full Azure Storage URL for you. This is the most convenient way to work with Azure Storage, as it hides 
the underlying Azure protocols from you.

For example:

- `"data/myfile.csv"` becomes `"wasbs://mycontainer@mystorage.blob.core.windows.net/data/myfile.csv"` (for Blob Storage).
- `"analytics/sales-data.orc"` becomes `"abfss://analytics@mydatalake.dfs.core.windows.net/analytics/sales-data.orc"` (for Data Lake Storage Gen2).

If you instead need to mix Azure Storage paths with other types of paths (such as S3 or local file paths), you must 
provide the complete Azure Storage URLs yourself. In this case, Flux will not perform any path transformation:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux import-files \
    --path "wasbs://mycontainer@mystorage.blob.core.windows.net/data/" \
    --path "s3a://my-bucket/other-data/" \
    --path "/local/file/path" \
    --azure-storage-account "mystorage" \
    --azure-access-key "your-access-key" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux import-files ^
    --path "wasbs://mycontainer@mystorage.blob.core.windows.net/data/" ^
    --path "s3a://my-bucket/other-data/" ^
    --path "C:\local\file\path" ^
    --azure-storage-account "mystorage" ^
    --azure-access-key "your-access-key" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

Flux thus uses an "all-or-nothing" approach for path transformation:

- **If all paths are simple relative paths** (no `://` protocol in any path), Flux automatically transforms them to full Azure Storage URLs.
- **If any path contains a protocol** (`://`), Flux assumes you're providing full URLs and performs no transformation.

This allows you to either use all relative paths or mix Azure Storage with other storage types by providing full URLs.

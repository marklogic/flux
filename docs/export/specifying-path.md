---
layout: default
title: Specifying a path
parent: Exporting Data
nav_order: 1
---

Each command for exporting data to files requires a path that defines the location of the exported data. 
This guide describes the different types of paths supported by Flux.

## Table of contents
{: .no_toc .text-delta }

- TOC
{:toc}

## Specifying a path

Commands that export data to files require a single path specified via the `--path` option. 
The value of the `--path` option can be any valid directory, S3 bucket, or Azure Storage container. 

> Note - MarkLogic allows for characters in document URIs that may not be supported in filenames for the target 
> filesystem. For example, Azure Storage will not allow colons in a filename. If you run into issues with characters 
> in the filename, consider exporting a zip file containing documents instead of exporting individual documents as files.

## Exporting to S3

Flux can export files to S3 via a path expression of the form `s3a://bucket-name/optional/path`.

### Authentication

In most cases, Flux must use your AWS credentials to access an S3 bucket. Flux supports several authentication methods:

**Automatic credential retrieval** - Flux uses the AWS SDK to fetch credentials from
[locations supported by the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-authentication-short-term.html).
To enable this, include the `--s3-add-credentials` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
  --path "s3a://my-bucket/some/path" \
  --s3-add-credentials \
  etc...
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
  --path "s3a://my-bucket/some/path" ^
  --s3-add-credentials \
  etc...
```
{% endtab %}
{% endtabs %}

**Explicit credentials** - You can explicitly define your AWS credentials via `--s3-access-key-id` and 
`--s3-secret-access-key`. To avoid typing these in plaintext, you may want to store these in a file and reference 
the file via an options file. See the documentation on [Common Options](../common-options.md) for more information. 
As of Flux 2.0.0, you may also specify an AWS session token via the `--s3-session-token` option. This token will be 
used with your access key ID and secret access key values when authenticating with S3.

**Profile-based authentication** - As of Flux 2.0.0, you can use `--s3-use-profile` to authenticate with AWS profile 
credentials from `~/.aws/config` and `~/.aws/credentials`. This supports SSO profiles (configured via `aws sso login`), 
standard profiles with access keys, and any other profile types. By default, the `[default]` profile is used, or you 
can specify a profile via the `AWS_PROFILE` environment variable. See the 
[AWS documentation on configuration and credential files](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html) 
for more information.

**Anonymous authentication** - As of Flux 2.2.0, you can use `--s3-anonymous` to access public S3 buckets that do not 
require authentication.

### Configuring the S3 connection

When running Flux within AWS and accessing an S3 bucket in a different region, you may need to configure the S3 
connection explicitly. Use `--s3-endpoint` to specify the S3 endpoint URL, and as of Flux 2.0.0, use `--s3-region` 
to specify the AWS region of the S3 bucket to access.

For advanced S3 configuration, you can use `--spark-conf` to set 
[Hadoop S3 properties](https://hadoop.apache.org/docs/stable/hadoop-aws/tools/hadoop-aws/index.html#General_S3A_Client_configuration). 
For example, the following sets the connection timeout:

`--spark-conf spark.hadoop.fs.s3a.connection.timeout=300000`

See [Common Options](../common-options.md) for more information on using `--spark-conf`.

## Exporting to Azure Storage

Flux can export files to [Azure Storage](https://docs.microsoft.com/en-us/azure/storage/common/storage-introduction) using either [Azure Blob Storage](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction) or [Azure Data Lake Storage](https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction). 

The examples below use notional values for options that contain credentials. To avoid typing credentials in plaintext,
consider storing them in [an options file](../../common-options.md).

> When exporting to Azure Blob Storage, you may notice that folders appear as both a folder structure and 
> a blob with the same name. This is normal behavior - Azure Blob Storage uses special marker blobs to represent 
> folder structures, and these are automatically created by the underlying storage libraries.

### Azure Blob Storage Authentication

Azure Blob Storage supports two authentication methods:

**Access Key Authentication**

If you are using [access key authentication](https://docs.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage),
you can define a key via the `--azure-access-key` option:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --path "reports" \
    --azure-storage-account "mystorage" \
    --azure-container-name "exports" \
    --azure-access-key "your-access-key" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --path "reports" ^
    --azure-storage-account "mystorage" ^
    --azure-container-name "exports" ^
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
./bin/flux export-files \
    --path "reports" \
    --azure-storage-account "mystorage" \
    --azure-container-name "exports" \
    --azure-sas-token "your-sas-token" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --path "reports" ^
    --azure-storage-account "mystorage" ^
    --azure-container-name "exports" ^
    --azure-sas-token "your-sas-token" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

> Note that `--azure-container-name` is required when using SAS token authentication.

### Azure Data Lake Storage Authentication

Azure Data Lake Storage uses [shared key authentication](https://docs.microsoft.com/en-us/azure/storage/common/storage-account-keys-manage).
You can define a shared key via the `--azure-shared-key` option. You must also include `--azure-storage-type DATA_LAKE`
to indicate that you are using Data Lake Storage instead of Blob Storage:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --path "analytics" \
    --azure-storage-account "mydatalake" \
    --azure-container-name "exports" \
    --azure-storage-type "DATA_LAKE" \
    --azure-shared-key "your-shared-key" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --path "analytics" ^
    --azure-storage-account "mydatalake" ^
    --azure-container-name "exports" ^
    --azure-storage-type "DATA_LAKE" ^
    --azure-shared-key "your-shared-key" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

### Path Handling

Flux provides two ways to specify an export path. First, when both `--azure-storage-account` and `--azure-container-name`
are specified and the path is relative - i.e. it does not contain a protocol like `wasbs://` or `abfss://` - Flux will
construct the full Azure Storage URL for you. This is the most convenient way to work with Azure Storage, as it hides
the underlying Azure protocols from you.

For example:

- `"data/myfile.csv"` becomes `"wasbs://mycontainer@mystorage.blob.core.windows.net/data/myfile.csv"` (for Blob Storage).
- `"analytics/sales-data.orc"` becomes `"abfss://analytics@mydatalake.dfs.core.windows.net/analytics/sales-data.orc"` (for Data Lake Storage Gen2).

Alternatively, you can provide the complete storage URL yourself:

{% tabs log %}
{% tab log Unix %}
```
./bin/flux export-files \
    --path "wasbs://exports@mystorage.blob.core.windows.net/reports/" \
    --azure-storage-account "mystorage" \
    --azure-access-key "your-access-key" \
    --connection-string etc... 
```
{% endtab %}
{% tab log Windows %}
```
bin\flux export-files ^
    --path "wasbs://exports@mystorage.blob.core.windows.net/reports/" ^
    --azure-storage-account "mystorage" ^
    --azure-access-key "your-access-key" ^
    --connection-string etc... 
```
{% endtab %}
{% endtabs %}

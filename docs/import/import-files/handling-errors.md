---
layout: default
title: Handling errors
parent: Importing files
grand_parent: Importing Data
nav_order: 10
---

If Flux fails to write a batch of documents to MarkLogic, it will attempt to write each document in the batch in smaller
batches until it concludes it cannot write a particular document. An error will be logged for that document and 
processing will continue. 

To force Flux to throw an error when it fails to write a batch of documents, include the `--abortOnWriteFailure` option. 

When Flux is not using the `--abortOnWriteFailure` option, you can capture failed documents with their metadata in a
ZIP archive file. To enable this, include the `--failedDocumentsPath` option with a file path for where you want 
archive files written containing failed documents. You can later use the `import_archive_files` command to retry these 
failed documents, presumably after making a fix to either the data or your application that will allow the documents 
to be successfully imported.

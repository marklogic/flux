---
layout: default
title: Handling errors
parent: Importing files
grand_parent: Importing Data
nav_order: 10
---

If NT fails to write a batch of documents to MarkLogic, it defaults to stopping execution of the command and displaying
the error. To configure NT to only log errors and keep writing documents, set `--abortOnFailure false` as an option.

Currently, NT is only aware that a batch of documents failed. A future enhancement will allow for configuring NT to
attempt to write each document individually in a failed batch to both minimize the number of failed documents and
identify which documents failed. 

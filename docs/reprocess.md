---
layout: default
title: Reprocessing Data
nav_order: 4
---

This will eventually document the command for reprocessing data in MarkLogic.

`./bin/nt help reprocess` will show all the different ways to specify custom code for reading and processing data
in MarkLogic. Note that while the term "write" is used in the options below, the code or module is not required to 
write any data, though it is common to do so.

Key options:

- `--readJavascript` = JavaScript code for reading URIs or other items.
- `--readXquery` = XQuery code for reading URIs or other items.
- `--readInvoke` = path of a module to invoke for reading URIs or other items.
- `--writeJavascript` = JavaScript code for writing / processing URIs or other items.
- `--writeXquery` = XQuery code for writing / processing URIs or other items.
- `--writeInvoke` = path of a module to invoke for writing / processing URIs or other items.

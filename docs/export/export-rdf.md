---
layout: default
title: Exporting RDF data
parent: Exporting Data
nav_order: 5
---

More to come, just an example for now:

```
./bin/flux import-rdf-files \
  --path ../1k.n3 \
  --connection-string "admin:admin@localhost:8000" \
  --collections "my-triples" \
  --permissions "rest-reader,read,rest-writer,update"

./bin/flux export-rdf-files \
  --path export \
  --connection-string "admin:admin@localhost:8000" \
  --graphs "my-triples" \
  --format ttl
```

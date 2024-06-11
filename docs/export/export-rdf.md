---
layout: default
title: Exporting RDF data
parent: Exporting Data
nav_order: 5
---

More to come, just an example for now:

```
./bin/flux import_rdf_files \
  --path ../1k.n3 \
  --connectionString "admin:admin@localhost:8000" \
  --collections "my-triples" \
  --permissions "rest-reader,read,rest-writer,update"

./bin/flux export_rdf_files \
  --path export \
  --connectionString "admin:admin@localhost:8000" \
  --graphs "my-triples" \
  --format ttl
```

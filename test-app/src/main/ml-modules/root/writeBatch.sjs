declareUpdate();

for (var uri of URI.split(",")) {
  xdmp.documentInsert(`/reprocess-test${uri}`, cts.doc(uri),
    {
      "collections": ["reprocess-batch-test"],
      "permissions": [xdmp.permission("flux-test-role", "read"), xdmp.permission("flux-test-role", "update")]
    }
  )
}

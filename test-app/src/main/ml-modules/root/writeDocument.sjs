declareUpdate();

var theValue;

xdmp.documentInsert(`/reprocess-test${URI}`,
  {"theValue": theValue},
  {
    "collections": ["reprocess-test"],
    "permissions": [xdmp.permission("flux-role", "read"), xdmp.permission("flux-role", "update")]
  }
)

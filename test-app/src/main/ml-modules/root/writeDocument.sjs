declareUpdate();

var theValue;

xdmp.documentInsert(`/reprocess-test${URI}`,
  {"theValue": theValue},
  {
    "collections": ["reprocess-test"],
    "permissions": [xdmp.permission("new-tool-role", "read"), xdmp.permission("new-tool-role", "update")]
  }
)

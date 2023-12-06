declareUpdate();

var THE_VALUE;

xdmp.documentInsert(
  `/test/${URI}.json`, {"hello": THE_VALUE},
  {"permissions": [xdmp.permission("new-tool-role", "read"), xdmp.permission("new-tool-role", "update")]}
)

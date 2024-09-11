const rdt = require('/MarkLogic/redaction');

function transform(context, params, content) {
  return rdt.redact(content, params["rulesetName"]);
};

exports.transform = transform;

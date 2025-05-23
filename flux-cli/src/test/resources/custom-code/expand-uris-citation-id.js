var URIs;
const citationIds = cts.elementValues(xs.QName('CitationID'), null, null, cts.documentQuery(URIs));
cts.uris(null, null, cts.andQuery([
   cts.notQuery(cts.documentQuery(URIs)),
   cts.collectionQuery('author'),
   cts.jsonPropertyValueQuery('CitationID', citationIds)
]))

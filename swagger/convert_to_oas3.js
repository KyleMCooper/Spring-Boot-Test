/**
* Convert a swagger 2 file to open api 3 using api-spec-converter
*/
const fs = require('fs');
var Converter = require('api-spec-converter');

Converter.convert({
  from: 'swagger_2',
  to: 'openapi_3',
  source: 'swagger/swagger.json'
}, function(err, converted) {
  var  options = {syntax: 'yaml', order: 'openapi'}
  fs.writeFileSync('swagger/oas3.yaml', converted.stringify(options));
})
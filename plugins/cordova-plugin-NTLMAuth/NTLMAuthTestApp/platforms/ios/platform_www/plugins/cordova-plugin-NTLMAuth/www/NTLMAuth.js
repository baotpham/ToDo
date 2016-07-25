cordova.define("cordova-plugin-NTLMAuth.NTLMAuth", function(require, exports, module) {
var exec = require('cordova/exec');

exports.callNtlmMethods = function(arrayArgs, success, error){
  exec(success, 
       error, 
       "NTLMAuth", 
       "callNtlmMethods", 
       arrayArgs
      );  
}

});

var exec = require('cordova/exec');

exports.callNtlmMethods = function(arrayArgs, success, error){
  exec(success, 
       error, 
       "NTLMAuth", 
       "callNtlmMethods", 
       arrayArgs
      );  
}

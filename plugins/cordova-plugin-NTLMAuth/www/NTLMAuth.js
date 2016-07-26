var exec = require('cordova/exec');

var NTLMAuth = {
  callNtlmMethods: function(arrayArgs, success, error){
      exec(success, 
          error, 
          "NTLMAuth", 
          "callNtlmMethods", 
          arrayArgs
          );  
  }
};

module.exports = NTLMAuth;

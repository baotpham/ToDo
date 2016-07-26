// link     :     https://github.com/EddyVerbruggen/Insomnia-PhoneGap-Plugin

angular.module('cordova.plugins.NTLMAuth', [])

  .factory('$NTLMAuth', ['$q', function ($q) {

      return {
          callNtlmMethods: function(arrayArgs, success, error){
              console.log("hi")
              var q = $q.defer();
              cordova.plugins.ntlmAuth.callNtlmMethods(arrayArgs, success, error);
              console.log("hi")
              return q.promise;
          }
      }
    
  }]);
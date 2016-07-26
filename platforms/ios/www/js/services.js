angular.module('app.services', ['cordova.plugins.NTLMAuth'])

.service('LoginService', function($q, $NTLMAuth) {
    return {
        loginUser: function(name, pw) {
            var deferred = $q.defer();
            var promise = deferred.promise;
           
            $NTLMAuth.callNtlmMethods(
                [name, pw, "http://events-dev.pfizer.com/ldap", "", "GET", "", ""],
                function callback(data) {
                    //alert("Response from plugin: " + data);
                    deferred.resolve('Welcome ' + name + '!');
                },
                function errorHandler(err) {
                    //alert("Response from plugin (error): " + err);
                    deferred.reject('Wrong credentials.');
                }
            );

            promise.success = function(fn) {
                promise.then(fn);
                return promise;
            }
            promise.error = function(fn) {
                promise.then(null, fn);
                return promise;
            }
            return promise;
        }
    }
});
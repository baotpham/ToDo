var submitButton = document.getElementById('submitButton');

submitButton.addEventListener("click", function(){

    var values = [document.getElementById('username').value, document.getElementById('password').value, document.getElementById('urlAddress').value, document.getElementById('urlPath').value, document.getElementById('methodName').value, document.getElementById('header').value, document.getElementById('data').value];

    ntlmAuth.callNtlmMethods(
        values,
        function callback(data) {
            alert("Response from plugin: " + data);
        },
        function errorHandler(err) {
            alert("Response from plugin (error): " + err);
        }
    );
});
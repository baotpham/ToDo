// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'


//setup angular
angular.module('todo-app', ['ionic', 'LocalStorageModule', 'app.controllers', 'app.services', 'cordova.plugins.NTLMAuth'])

.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    if(window.cordova && window.cordova.plugins.Keyboard && window.cordova.plugins.NTLMAuth) {
      // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
      // for form inputs)
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);

      // Don't remove this line unless you know what you are doing. It stops the viewport
      // from snapping when text inputs are focused. Ionic handles this internally for
      // a much nicer keyboard experience.
      cordova.plugins.Keyboard.disableScroll(true);
    }
    if(window.StatusBar) {
      StatusBar.styleDefault();
    }
  });
})

.factory('NTLMAuth', function(){

})

.config(function ($stateProvider, $urlRouterProvider, localStorageServiceProvider) {
    
    localStorageServiceProvider
        .setPrefix('todo-app');
    
    $stateProvider
        .state('login', {
            url: '/login',
            templateUrl: 'templates/login.html',
            controller: 'LoginCtrl'
        })
        .state('toDoList', {
            url: '/toDoList',
            templateUrl: 'templates/toDoList.html',
            controller: 'toDoListCtrl'
        });
      
    // if none of the above states are matched, use this as the fallback
    $urlRouterProvider
        .otherwise('/login');

});
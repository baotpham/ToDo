angular.module('app.controllers', [])

.controller('LoginCtrl', function($scope, LoginService, $ionicPopup, $state) {
    $scope.data = {};

    $scope.login = function() {
        LoginService.loginUser($scope.data.username, $scope.data.password).success(function(data) {
            var alertPopup = $ionicPopup.alert({
                title: 'Login success!',
                template: 'Welcome ' + $scope.data.username + '!'
            });
            $state.go('toDoList')
            // $scope.listTaskModal.show();
        }).error(function(data) {
            var alertPopup = $ionicPopup.alert({
                title: 'Login failed!',
                template: 'Please check your credentials!'
            });
        });
    }
})



.controller('toDoListCtrl', function ($scope, $ionicModal, localStorageService) {
    //store the entities name in a variable

    var taskData = 'task';

    //initialize the tasks scope with empty array
    $scope.tasks = [];

    //initialize the task scope with empty object
    $scope.task = {};

    //configure the ionic modal before use
    //$ionicModal.fromTemplateUrl('createTodo-modal.html', {
    $ionicModal.fromTemplateUrl('templates/modals/createTodo-modal.html', {
        scope: $scope,
        animation: 'slide-in-up'
    }).then(function (modal) {
        $scope.newTaskModal = modal;
    });

    $scope.getTasks = function () {
        //fetches task from local storage
        if (localStorageService.get(taskData)) {
            $scope.tasks = localStorageService.get(taskData);
        } else {
            $scope.tasks = [];
        }
    };

    $scope.createTask = function () {
        //creates a new task
        $scope.tasks.push($scope.task);
        localStorageService.set(taskData, $scope.tasks);
        $scope.task = {};

        //close new task modal
        $scope.newTaskModal.hide();
    };

    $scope.removeTask = function (index) {
        //removes a task
        $scope.tasks.splice(index, 1);
        localStorageService.set(taskData, $scope.tasks);
    };


    $scope.completeTask = function (index) {
        //updates a task as completed
        localStorageService.set(taskData, $scope.tasks);
    };

    $scope.openNewTaskModal = function () {
        $scope.newTaskModal.show();
        console.log("here")
    };

    $scope.closeNewTaskModal = function () {
        $scope.newTaskModal.hide();
    };
})
var app = angular.module('app', ['ngRoute']);
app.controller('ConfigController', ['$scope', '$routeParams', '$http', ConfigController]);
app.config(['$routeProvider', function ($routeProvider) {
  $routeProvider
    .when('/',
      {templateUrl: 'partials/all.html', controller: 'ConfigController'}
    );
}]);
var app = angular.module('app', ['ngRoute', 'ngResource']);
app.controller('ConfigController', ['$scope', 'Configs', '$routeParams', ConfigController]);
app.controller('NewController', ['$scope', 'Config', '$routeParams', NewController]);
app.config(['$routeProvider', function ($routeProvider) {
  $routeProvider
    .when('/configs',
      {templateUrl: 'partials/all.html', controller: 'ConfigController'}
    ).when('/configs/current/:current',
      {templateUrl: 'partials/all.html', controller: 'ConfigController'}
    ).when('/configs/new/:newName',
        {templateUrl: 'partials/editConfig.html', controller: 'NewController'})
    .otherwise({
        redirectTo: '/configs'
    });
}]);
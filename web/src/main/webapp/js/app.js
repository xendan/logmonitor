var app = angular.module('app', [ 'ngRoute', 'ngResource' ]);

// Controllers
app.controller('AllConfigsController', [ '$scope', 'Configs', '$routeParams',
		AllConfigsController ]);
app.controller('ConfigController', [ '$scope', 'Configs', 'Servers', '$http', '$routeParams',
		ConfigController ]);

app.config([ '$routeProvider', function($routeProvider) {
	$routeProvider.when('/configs', {
		templateUrl : 'partials/all.html',
		controller : 'AllConfigsController'
	}).when('/configs/current/:current', {
		templateUrl : 'partials/all.html',
		controller : 'AllConfigsController'
	}).when('/configs/:configId', {
		templateUrl : 'partials/editConfig.html',
		controller : 'ConfigController'
	}).otherwise({
		redirectTo : '/configs'
	});
} ]);
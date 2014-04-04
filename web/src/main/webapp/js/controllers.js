function ConfigController($scope, $routeParams, $http) {
		$http.get('rest/configs').success(function(configs) {
		    if (configs) {
			    $scope.configs = configs.configurations;
			}
			else {
			    $scope.configs = [];
			}
			console.log($routeParams);
			for(var zu in $routeParams) {
			    console.log(zu + '--' + $routeParams[zu]);
			}

		}).error(function(data, status, headers, config) {
			$scope.errorStatus = status;
			$scope.errorData = data;
		});
	}
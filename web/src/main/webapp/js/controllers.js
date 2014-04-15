function AllConfigsController($scope, Configs, $routeParams) {
	$scope.configs = Configs.getAll();
	$scope.currentProject = $routeParams.projectName;
}

function ConfigController($scope, Configs, Servers, $http, $routeParams) {
	this.scope = $scope;
	this.Configs = Configs;
	$scope.saveConfig = function() {
		// TODO add polyfill
		$scope.config.environments.forEach(function(env) {
			env.matchConfigs.forEach(function(matcher) {
				delete matcher.selected;
			});
		});
		Configs.update($scope.config);
		/*
		 * if ($scope.userForm.$valid) { alert('our form is amazing'); }
		 */
	};
	var localhost = {name:"localhost"};
	$scope.servers = Servers.getAll({}, function(servers) {
		servers.unshift(localhost);
		servers.unshift({name:"new..."});		

	});
	$http({
		method : 'GET',
		url : 'rest/loglevels/'
	}).success(function(data, status, headers, config) {
		$scope.levels = data;
	}).error(function(data, status, headers, config) {
		// TODO do something
	});
	var serviceParams = {
		configId : $routeParams.configId,
		projectName : $routeParams.projectName
	};
	$scope.matchers = [];
	$scope.config = Configs
			.getOne(serviceParams,
					function() {						
						for (var i = 0; i < $scope.config.environments.length; i++) {
							for (var j = 0; j < $scope.config.environments[i].matchConfigs.length; j++) {
								var matcher = $scope.config.environments[i].matchConfigs[j];
								if ($scope.matchers.indexOf(matcher) == -1) {
									$scope.matchers.push(matcher);
								}
							}
						}
					});
	var findById = function(id, items) {
		for (var i = 0; i < items.length; i++) {
			if (items[i].id == id) {
				return items[i];
			}
			return undefined;
		}
	};
	$scope.$watch("selectedEnvironment", function(newValue, oldValue) {
		if (newValue) {
			for (var i = 0; i < $scope.matchers.length; i++) {
				$scope.matchers[i].selected = findById($scope.matchers[i].id,
						newValue.matchConfigs) != undefined;

			}
		}
	});

	$scope.toggleSelected = function(matcher) {
		if (!matcher.selected) {
			$scope.selectedEnvironment.matchConfigs.push(matcher);
		} else {
			$scope.selectedEnvironment.matchConfigs.pop(findById(matcher.id,
					$scope.selectedEnvironment.matchConfigs));
		}
	};
	$scope.cantSave = function() {
		// TODO validate environment and match config
		return false;
	};

	document.onclick = function(evt) {
		var inEnvironment = $('.selectedEnvironment').find(evt.target).length;
		var inMatcher = $('.selectedMatcher').find(evt.target).length;
		if (!inEnvironment && !inMatcher) {
			$scope.selectedEnvironment = undefined;
		}
		if (!inMatcher) {
			$scope.selectedMatcher = undefined;
		}
		$scope.$apply();
	};
}

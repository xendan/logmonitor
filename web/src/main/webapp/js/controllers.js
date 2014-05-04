function AllConfigsController($scope, Configs, $routeParams) {
	$scope.configs = Configs.getAll();
	$scope.currentProject = $routeParams.projectName;
}

function ConfigController($scope, Configs, Servers, $http, $routeParams) {
	this.scope = $scope;
	this.Configs = Configs;
	$scope.saveConfig = function() {
		// TODO add polyfill
		Configs.update($scope.config);
	};
	var localhost = {
		host : "localhost"
	};
	$scope.servers = Servers.getAll({}, function(servers) {
		servers.unshift(localhost);
		servers.unshift({
			host : "new..."
		});

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
	$scope.createNewEnvironment = function(env) {
		var minId = 0;
		forEachEnvMatcher(function(env, matcher) {
			if (env.id < minId) {
				minId = env.id;
			}
		}, true);
		return {
			updateInterval : 5,
			server : localhost,
			matchConfigs : $scope.matchers,
			id : minId - 1
		};
	};
	$scope.createNewMatcher  = function() {
		var minId = 0;
		forEachEnvMatcher(function(env, matcher) {
			if (matcher && matcher.id < minId) {
				minId = matcher.id;
			}
		});
		return {
			general : true,
			showNotification : true,
			level : 'ERROR',
			id : minId - 1
		};
	};
	var forEachEnvMatcher = function(action, visitEnv) {
		for (var i = 0; i < $scope.config.environments.length; i++) {
			var env = $scope.config.environments[i];
			if (visitEnv) {
				action(env);
			}
			for (var j = 0; j < env.matchConfigs.length; j++) {
				action($scope.config.environments[i], env.matchConfigs[j]);
			}
		}
	};	
	$scope.config = Configs.getOne(serviceParams, function() {
		forEachEnvMatcher(function(env, matcher) {
			if (!env.server) {
				env.server = localhost;
			}
			if (matcher) {
				if ($scope.matchers.indexOf(matcher) == -1) {
					$scope.matchers.push(matcher);
				}
			}
		}, true);
	});

	$scope.cantSave = function() {
		// TODO validate environment and match config
		return false;
	};
	var findById = function(id, items) {
		for (var i = 0; i < items.length; i++) {
			if (items[i].id == id) {
				return items[i];
			}			
		}
	};
	$scope.saveEnvironment = function(env, enabledMatchers) {
		var envUpdate = findById(env.id, $scope.config.environments);
		if (!envUpdate) {
			$scope.config.environments.push(env);
			envUpdate = env;
		}
		angular.extend(envUpdate, env);
		envUpdate.matchConfigs = [];
		for (var i = 0; i < $scope.matchers.length; i++) {
			var matcher = $scope.matchers[i];
			if (enabledMatchers[matcher.id]) {
				envUpdate.matchConfigs.push(matcher);
			}			
		}
		$scope.$digest();
	};
	
	$scope.saveMatcher = function(matcher, enabledEnvironments) {
	};
	
}

function AllConfigsController($scope, Configs, $routeParams) {
	$scope.configs = Configs.getAll();
	$scope.currentProject = $routeParams.projectName;
}

function ConfigController($scope, Configs, Servers, $http, $routeParams) {
	this.scope = $scope;
	this.Configs = Configs;
	$scope.saveConfig = function() {
		// TODO add polyfill
		/*
		 * $scope.config.environments.forEach(function(env) {
		 * env.matchConfigs.forEach(function(matcher) { delete matcher.selected;
		 * }); })
		 */;
		Configs.update($scope.config);
		/*
		 * if ($scope.userForm.$valid) { alert('our form is amazing'); }
		 */
	};
	var localhost = {
		name : "localhost"
	};
	$scope.servers = Servers.getAll({}, function(servers) {
		servers.unshift(localhost);
		servers.unshift({
			name : "new..."
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
		return {
			updateInterval : 5,
			sever : localhost,
			matchConfigs : $scope.matchers
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
		$scope.environmentMatcher = {};
		forEachEnvMatcher(function(env, matcher) {
			if (!$scope.environmentMatcher[env.id]) {
				$scope.environmentMatcher[env.id] = {};
				$scope.environmentMatcher[env.id][-1] = true;
			}
			if (matcher) {
				$scope.environmentMatcher[env.id][matcher.id] = true;
				if ($scope.matchers.indexOf(matcher) == -1) {
					$scope.matchers.push(matcher);
				}
			}
		}, true);
	});

	$scope.selection = {};

	$scope.$watch('environmentMatcher', function(newValue) {
		if (newValue) {
			forEachEnvMatcher(function(env, matcher) {
				var index = env.matchConfigs.indexOf(matcher);
				if (newValue[env.id][matcher.id]) {
					if (index == -1) {
						env.matchConfigs.push(matcher);
					}
				} else {
					if (index != -1) {
						env.matchConfigs.splice(index, 1);
					}
				}
			});
		}
	}, true);

	$scope.cantSave = function() {
		// TODO validate environment and match config
		return false;
	};
	$scope.saveEnvironment = function(env) {

	};
	$scope.newMatcher = {
		id : -1,
		level : "ERROR",
		general : true
	};
}

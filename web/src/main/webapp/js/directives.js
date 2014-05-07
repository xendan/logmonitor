app.directive('environment', function() {
	return {
		restrict : 'A',
		templateUrl : 'partials/environment.html',
		replace : true,
		scope : {
			name : '=',
			env0 : '=',
			createNewEnvironment : '=',
			saveEnvironment : '=',
			servers : '=',
			index : '=',
			matchers : '=',
			envToString : '=',
			matcherToString : '='
		},
		link : function($scope, element, attrs) {
			$scope.onLinkClick = function() {
				var createCopy = function(env) {
					var copy = angular.copy(env);
					copy.server = env.server;
					return copy;
				};
				$scope.env = ($scope.env0) ? createCopy($scope.env0) : $scope.createNewEnvironment();
				$scope.enabledMatchers = {};
				if (!$scope.env0) {
					$scope.env.matchConfigs = $scope.matchers;
				}
				$scope.env.matchConfigs.forEach(function(matcher) {
					$scope.enabledMatchers[matcher.id] = true;
				});
				$('#env' + $scope.index).dialog(
						{
							modal : true,
							width : 550,
							buttons : {
								OK : function() {
									$scope.saveEnvironment($scope.env,
											$scope.enabledMatchers);
									$(this).dialog("close");
									$scope.$digest();
								}
							}
						});

			};

		}
	};
});
app.directive('matcher', function() {
	return {
		restrict : 'A',
		templateUrl : 'partials/matcher.html',
		replace : true,
		scope : {
			name : '=',			
			levels : '=',
			matcher0 : '=',
			allEnvironments : '=',
			createNewMatcher : '=',
			saveMatcher : '=',
			index : '=',
			envToString : '=',
			matcherToString : '='

		},
		link : function($scope, element, attrs) {
			$scope.onLinkClick = function() {
				$scope.matcher = ($scope.matcher0) ? angular
						.copy($scope.matcher0) : $scope.createNewMatcher();
				$scope.enabledEnvironments = {};
				$scope.allEnvironments.forEach(function(env) {
					if (!$scope.matcher0 || env.matchConfigs.indexOf($scope.matcher0) != -1) {
						$scope.enabledEnvironments[env.id] = true;
					}
				});
				$('#matcher' + $scope.index).dialog(
						{
							modal : true,
							width : 550,
							buttons : {
								OK : function() {
									$scope.saveMatcher($scope.matcher,
											$scope.enabledEnvironments);
									$(this).dialog("close");
									$scope.$digest();
								}
							}
						});

			};
		}
	};
});
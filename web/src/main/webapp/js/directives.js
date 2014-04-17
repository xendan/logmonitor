app.directive('environment', function() {
	return {
		// required to make it work as an element
		restrict : 'A',
		templateUrl : 'partials/environment.html',
		replace : true,
		scope : {
			name : '=',
			selection : '=',
			env0 : '='
		},
		link : function($scope, element, attrs) {
			//TODO: angular, why???
			if (!attrs.env0) {
				$scope.env = $scope.$parent.createNewEnvironment();
			} else {
				$scope.env = $scope.env0;
			}
		}
	};
});
app.directive('matcher', function() {
	return {
		// required to make it work as an element
		restrict : 'A',
		templateUrl : 'partials/matcher.html',
		replace : true,
		scope : {
			name : '=',
			selection : '=',
			env : '=',
			levels : '=',
			matcherObj : '=',
			environments : '=',
			environmentMatcher :  '='
				
		},
		link : function($scope, element, attrs) {
		}
	};
});
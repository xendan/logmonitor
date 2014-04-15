app.directive('environment', function() {
	return {
		// required to make it work as an element
		restrict : 'A',

		// replace <photo> with this html
		templateUrl : 'partials/environment.html',
		replace : true,
		scope : {
			name : '='
		},
		// observe and manipulate the DOM
		link : function($scope, element, attrs) {
			console.log("attrs" + attrs );
			console.log("element" + element);
			
		}

	}
});
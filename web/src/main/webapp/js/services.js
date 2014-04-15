app.factory('Configs', ['$resource',
  function($resource){
    return $resource('rest/configs', {}, {
      getAll: {
            method:'GET',
            isArray:true},

      getOne: {
            method:'GET',
            isArray:false,
            params:{configId:'@id'},
            url:'rest/configs/:configId'},

      update: {
            method: 'POST'
      }
    });
  }]);
app.factory('Servers', ['$resource',
                        function($resource){
	return $resource('rest/servers', {}, {
		getAll: {
			method:'GET',
			isArray:true}
	});
}]);


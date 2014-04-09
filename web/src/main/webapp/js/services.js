app.factory('Configs', ['$resource',
  function($resource){
    return $resource('rest/configs', {}, {
      query: {
            method:'GET',
            isArray:true},

      getNew: {
            method:'GET',
            isArray:false,
            params:{configId:-1},
            url:'rest/configs/:configId'},

      update: {
            method: 'POST'
      }
    });
  }]);
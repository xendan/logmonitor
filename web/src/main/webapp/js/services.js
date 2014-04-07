app.factory('Configs', ['$resource',
  function($resource){
    return $resource('rest/configs', {}, {
      query: {method:'GET', params:{}, isArray:true}
    });
  }]);

app.factory('Config', ['$resource',
  function($resource){
    return $resource('rest/configs/:configId', {configId: '@configId'}, {
      getNew: {method:'GET', params:{configId:-1}, isArray:false}
    });
  }]);

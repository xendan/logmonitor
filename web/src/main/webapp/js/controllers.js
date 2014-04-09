function ConfigController($scope, Configs, $routeParams) {
        $scope.configs = Configs.query();
        $scope.currentProject = $routeParams.current;
}

function NewController($scope, Configs, $routeParams) {
        $scope.config = Configs.getNew({projectName:$routeParams.newName}, function() {
        if ($scope.config.environments.length == 1) {
            $scope.matchers = $scope.config.environments[0].matchConfigs;
                for (var matcher in $scope.matchers) {
                    matcher.selected = true;
                }
            }
        });
        $scope.saveConfig = function() {
            console.log('Yes mam ill save it');
            Configs.update($scope.config);
        };
}
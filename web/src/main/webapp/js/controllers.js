function ConfigController($scope, Configs, $routeParams) {
        $scope.configs = Configs.query();
        $scope.currentProject = $routeParams.current;
}

function NewController($scope, Config, $routeParams) {
        $scope.config = Config.getNew();
        $scope.config.projectName = $routeParams.newName;
}
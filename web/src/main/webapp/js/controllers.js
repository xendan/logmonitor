function AllConfigsController($scope, Configs, LogEntries, $routeParams) {
	$scope.statuses = {};
	$scope.entries = {};
	$scope.newEntries = {};
	$scope.notifications = {};
	$scope.entriesNum = {};
	$scope.expanded = {configs:{}, matchers:{}, groups:{}, messages:{}};
	var eachEntry = function(list, lambda) {
	    var eachInList = function(entries) {
	        if (entries) {
	            for (var i = 0; i < entries.length; i++) {
	                lambda(entries[i]);
	            }
	        }
	    };
	    if (list.groups) {
	        for (var i=0; i < list.groups.length; i++) {
	            eachInList(list.groups[i].entries);
	        }
	    }
	    eachInList(list.notGrouped);
	};
	var addNotification = function(matcher, entry) {
	    if (!$scope.notifications[matcher.name]) {
	        $scope.notifications[matcher.name] = [];
	    }
	    $scope.notifications[matcher.name].push(entry);
	};
	var getLoadEntriesHandler = function(matcher, envId) {
	    return function(entries) {
            	    eachEntry(entries, function(newEntry) {
            	        var existed = false;
            	        eachEntry($scope.entries, function(oldEntry) {
            	            if (newEntry.id == oldEntry.id) {
            	                existed = true;
            	            }
            	        });
            	        $scope.newEntries[newEntry.id] = !existed;
            	        if (!existed && matcher.showNotification) {
            	            addNotification(matcher, newEntry);
            	        }
            	    });
                    $scope.entries[envId][matcher.id] = entries;
                    var counter= 0;
                    eachEntry(entries, function() {
                        counter++;
                    })
                    $scope.entriesNum[envId][matcher.id] = counter;
    //                $scope.$digest();
            	}
	};
	var refreshEnvironment = function(env) {
        LogEntries.getStatus({envId:env.id}, function(status) {
            $scope.statuses[env.id] = status;
            setTimeout(refreshEnvironment, status.updateInterval, env);
        });
        for (var k = 0; k < env.matchConfigs.length; k++ ) {
            if (!$scope.entries[env.id]) {
                $scope.entries[env.id] = {};
            }
            var matcherId = env.matchConfigs[k].id;
         	LogEntries.getEntries(
        	            {envId:env.id, matcherId:matcherId, isGeneral:env.matchConfigs[k].general },
                    getLoadEntriesHandler(env.matchConfigs[k], env.id));
        }
	}
	var visibleFields = {};
	$scope.configs = Configs.getAll({}, function(configs) {
    	for (var i = 0; i < configs.length; i++) {
    	    visibleFields[configs[i].id] = configs[i].visibleFields;
    	    $scope.entries = {};
    	    for (var j = 0; j < configs[i].environments.length; j++) {
    	        var env = configs[i].environments[j];
    	        refreshEnvironment(env);
    	        $scope.entriesNum[env.id] = {};
    	        $scope.expanded.matchers[env.id] = {};
    	    }
    	}
	});
	$scope.currentProject = $routeParams.projectName;
    $scope.entryToHtml = function(entry, configId) {
        var result = "";
        for (var i = 0;  i < visibleFields[configId].length; i++) {
            var field = visibleFields[configId][i];
            var value = (field == 'message' && entry.expandedMessage) ? entry.expandedMessage : entry[field];
            result +=  field + ":" + value + '<br />';
        }
        return result;
    }
}

function ConfigController($scope, Configs, Servers, $http, $routeParams) {
	$scope.saveConfig = function() {
		// TODO add polyfill
		var copy = angular.copy($scope.config);
		for (var i = 0; i < copy.environments.length; i++ ) {
		    var env = copy.environments[i];
		    if (env.server.host == localhost.host) {
		        delete env.server;
		    }
		}
		Configs.update(copy, function() {
		    window.location = '';
		});
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
		return !($scope.config.projectName) || !($scope.config.logPattern);
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
		forEachEnvMatcher(function(env, matcher1) {
			var matcherUpdate = findById(matcher.id, env.matchConfigs);
			if (!matcherUpdate && !findById(matcher.id, $scope.matchers)) {
				$scope.matchers.push(matcher);
			}
			if (enabledEnvironments[env.id]) {
				if (!matcherUpdate) {
					env.matchConfigs.push(matcher);
				} else {
					angular.extend(matcherUpdate, matcher); 
				}
			} else if (matcherUpdate) {
				env.matchConfigs.pop(matcherUpdate);
			}
		});
		$scope.$digest();
	};
	
	$scope.envToString = function(env, name, showPath) {
		if (env) {
			var path = showPath ? ", " + (env.path ? env.path : "Not defined") : "";
			return env.name + " (" + env.server.host +  path + ")";
		}
		return name;
	};
	$scope.matcherToString = function(matcher, name) {
		var substring = function(message, max) {
			if (message.length < max) {
				return message;
			}
			return message + "...";
		};
		if (matcher != undefined) {
			name = matcher.name + " (" + matcher.level ;
			if (matcher.message && matcher.message.length !== 0) {
				name += ", " + substring(matcher.message, 10);
			}
			name += ")";
		}
		return name;
	};
	
}

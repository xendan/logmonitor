function truncate(string, length) {
	if (string.length < length + 3) {
		return string;
	}
	return string.substring(0, length) + '...';

}

function AllConfigsController($scope, Configs, LogEntries, $routeParams) {
	$scope.newEntries = {};
	$scope.statuses = {};
	$scope.entries = {};
	$scope.notifications = {};
	$scope.entriesNum = {};
	$scope.expanded = {
		configs : {},
		matchers : {},
		groups : {},
		messages : {}
	};

	$scope.getItemClass = function(item) {
		var isSelected = function() {
			if ("entries" in item) {
				for (var i = 0; i < item.entries.length; i++) {
					if ($scope.newEntries[item.entries[i].id]) {
						return true;
					}
				}
			} else {
				return $scope.newEntries[item.id];
			}
			return false;
		};
		return isSelected() ? "newItem" : "";
	};
	var eachEntry = function(list, lambda) {
		for (var i = 0; i < list.groups.length; i++) {
			list.groups[i].entries.forEach(lambda);
		}
		list.notGrouped.forEach(lambda);
	};
	/*
	 * var addNotification = function(matcher, entry) { if
	 * (!$scope.notifications[matcher.name]) {
	 * $scope.notifications[matcher.name] = []; }
	 * $scope.notifications[matcher.name].push(entry); };
	 */
	var getLoadEntriesHandler = function(matcher, envId) {
		return function(items) {
			var markNew = true;
			if (!$scope.entries[envId]) {
				$scope.entries[envId] = {};
			}
			if (!$scope.entries[envId][matcher.id]) {
				$scope.entries[envId][matcher.id] = {
					groups : [],
					notGrouped : []
				};
				markNew = false;
			}
			var oldCounter = 0;
			eachEntry($scope.entries[envId][matcher.id], function() {
				oldCounter++;
			});
			var oldItems = $scope.entries[envId][matcher.id];
			eachEntry(oldItems, function(entry) {
				delete $scope.newEntries[entry.id];
			});
			var addNewEntries = function(newEntries, oldEntries) {
				for (var k = 0; k < newEntries.length; k++) {
					oldEntries.splice(k, 0, newEntries[k]);
				}
			};
			var getByIdOrAdd = function(group) {
				for (var i = 0; i < oldItems.groups.length; i++) {
					if (group.id == oldItems.groups[i].id) {
						return oldItems.groups[i];
					}
				}
				oldItems.groups.push(group);
				oldItems.groups.sort(function(a, b) {
					return b.entries.length - a.entries.length;
				});
				return group;
			};
			for (var i = 0; i < items.groups.length; i++) {
				var newGroup = items.groups[i];
				var oldGroup = getByIdOrAdd(newGroup);
				if (newGroup != oldGroup) {
					addNewEntries(newGroup.entries, oldGroup.entries);
				}
			}
			addNewEntries(items.notGrouped, oldItems.notGrouped);
			var counter = 0;
			eachEntry($scope.entries[envId][matcher.id], function() {
				counter++;
			});
			$scope.entriesNum[envId][matcher.id] = counter;
			if (markNew) {
				eachEntry(items, function(entry) {
					$scope.newEntries[entry.id] = true;
				});
			}
		};
	};
	var refreshTimeoutVars = {};
	var refreshEnvironment = function(env, doRefresh) {
		var getMaxDate = function(envId, matcherId) {
			if (!$scope.entries[envId]) {
				return 0;
			}
			var maxDate = 0;
			eachEntry($scope.entries[envId][matcherId], function(entry) {
				var date = new Date(entry.date[0], entry.date[1] - 1,
						entry.date[2], entry.date[3], entry.date[4],
						entry.date[5], entry.date[6]);
				if (date.getTime() > maxDate) {
					maxDate = date.getTime();
				}
			});
			return maxDate;
		};
		LogEntries.getStatus({
			envId : env.id
		}, function(status) {
			$scope.statuses[env.id] = status;			
			refreshTimeoutVars[env.id] = setTimeout(refreshEnvironment, status.updateInterval, env);
		});
		for (var k = 0; k < env.matchConfigs.length; k++) {
			var matcherId = env.matchConfigs[k].id;

			LogEntries.getEntries({
				envId : env.id,
				matcherId : matcherId,
				isGeneral : env.matchConfigs[k].general,
				since : getMaxDate(env.id, matcherId),
				refresh: doRefresh 
			}, getLoadEntriesHandler(env.matchConfigs[k], env.id));
		}
	};
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
		for (var i = 0; i < visibleFields[configId].length; i++) {
			var field = visibleFields[configId][i];
			var value = (field == 'message' && entry.expandedMessage) ? entry.expandedMessage
					: entry[field];
			result += field + ":" + value + '<br />';
		}
		return result;
	};
	$scope.deleteEntriesInEnv = function(env) {
		var matchers = [];
		function createContent() {
			var html = $('<h3> Are you sure to delete entries for ' + env.name
					+ '</h3>');
			var list = html.append('<ul></ul>').find('ul');
			for (var i = 0; i < env.matchConfigs.length; i++) {
				var id = env.matchConfigs[i].id;
				matchers.push(id);
				var input = $("<input type='checkbox' checked/>").click(
						function() {
							var index = matchers.indexOf(id);
							if (index == -1) {
								matchers.push(id);
							} else {
								matchers.splice(index, 1);
							}
						});
				list.append(input).append(env.matchConfigs[i].name);
			}
			html.append(list);
			return html;
		}
		$('<div></div>').appendTo('body').html(createContent()).dialog({
			modal : true,
			title : "Delete all entries",
			zIndex : 10000,
			autoOpen : true,
			width : 'auto',
			resizable : false,
			buttons : {
				Yes : function() {
					LogEntries.deleteEntriesInEnv({
						envId : env.id,
						mathcerIds : matchers
					});
					$(this).dialog("close");
				},
				No : function() {
					$(this).dialog("close");
				}
			},
			close : function(event, ui) {
				$(this).remove();
			}

		});
	};
	$scope.refresh = function(env) {
		clearTimeout(refreshTimeoutVars[env.id]);
		refreshEnvironment(env, true);
	};
	$scope.groupName = function(group, doTruncate) {
		return '('
				+ group.entries.length
				+ ') - '
				+ (doTruncate ? truncate(group.messagePattern, 60)
						: group.messagePattern);
	};
}

function ConfigController($scope, Configs, Servers, $http, $routeParams) {
	var localhost = {
		host : "localhost"
	};
	$scope.saveConfig = function() {
		// TODO add polyfill
		var copy = angular.copy($scope.config);
		for (var i = 0; i < copy.environments.length; i++) {
			var env = copy.environments[i];
			if (env.server.host == localhost.host) {
				delete env.server;
			}
		}
		Configs.update(copy, function() {
			window.location = '';
		});
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
	$scope.createNewMatcher = function() {
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
			var path = showPath ? ", " + (env.path ? env.path : "Not defined")
					: "";
			return env.name + " (" + env.server.host + path + ")";
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
			name = matcher.name + " (" + matcher.level;
			if (matcher.message && matcher.message.length !== 0) {
				name += ", " + substring(matcher.message, 10);
			}
			name += ")";
		}
		return name;
	};

}

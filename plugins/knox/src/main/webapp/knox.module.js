/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// Localization completed
angular.module('plugin-knox', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-knox', {
                url: "/" + 'plugin-knox/{configurationId}',
                params:  {
                    configurationId: {
                        value: null,
                        squash: true
                    }
                },
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.knox.configurations" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-knox';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-knox', e);
        }

        try {
            $stateProvider.state('plugin-knox-rules', {
                url: "/" + 'plugin-knox-rules/{configurationId}',
                params:  {
                    configurationId: {
                        value: null,
                        squash: true
                    }
                },
                templateUrl: 'app/components/plugins/knox/views/rules.html',
                controller: 'PluginKnoxRulesTabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.knox.rules" | localize}}', //label to show in breadcrumbs
                    parent: 'plugin-knox'
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-knox-rules', e);
        }

    })
    .factory('pluginKnoxService', function ($resource) {
        return $resource('', {}, {
            searchRules: {url: 'rest/plugins/knox/private/search', method: 'POST'},
            saveRule: {url: 'rest/plugins/knox/private/rule', method: 'PUT'},
            deleteRule: {url: 'rest/plugins/knox/private/rule/:id', method: 'DELETE'},
            copyProfile: {url: 'rest/plugins/knox/private/copy', method: 'POST'}
        });
    })
    .controller('PluginKnoxConfigurationsTabController', function ($scope, $rootScope, $window, $state, $modal,
                                                            configurationService, localization) {

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.localization = localization;

        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            currentPage: 1,
            totalItems: 0,
            searchValue: null
        };

        $scope.$watch('paging.pageNum', function() {
            $window.scrollTo(0, 0);
        });

        $scope.search = function () {
            configurationService.getAllConfigurations({value: $scope.paging.searchValue},
                function (response) {
                    $scope.configurations = response.data;
                });
        };

        $scope.editRules = function(configuration) {
            $state.transitionTo('plugin-knox-rules', {configurationId: configuration.id,
                configurationName: configuration.name});
        };

        $scope.copyProfile = function (configuration) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/knox/views/copy.modal.html',
                controller: 'PluginKnoxCopyProfileController',
                resolve: {
                    request: function () {
                        return {
                            configSrc: configuration.id,
                            configDst: configuration.id,
                            removeExisting: true
                        };
                    }
                }
            });

            modalInstance.result.then(function (error) {
                if (error) {
                    $scope.successMessage = undefined;
                    $scope.errorMessage = error;
                } else {
                    $scope.errorMessage = undefined;
                    $scope.successMessage = localization.localize("plugin.knox.copy.successful");
                    $scope.search();
                }
            });
        };

        $scope.search();

    })
    .controller('PluginKnoxRulesTabController', function ($scope, $window, $state, $stateParams, $modal, confirmModal,
                                                          configurationService, pluginKnoxService, localization) {

        $scope.localization = localization;

        configurationId = $stateParams.configurationId;
        configurationService.getById({"id": configurationId}, function(response) {
            if (response.data) {
                $scope.translations = { title: localization.localize("plugin.knox.rules.title").replace("${configName}", response.data.name) };
                $scope.customerId = response.data.customerId;
            }
        });

        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            currentPage: 1,
            totalItems: 0,
            configurationFilter: configurationId,
            patternFilter: null,
            tableTypeFilter: null,
            ruleTypeFilter: null
        };

        $scope.$watch('paging.pageNum', function() {
            loadData();
            $window.scrollTo(0, 0);
        });

        var loadData = function () {
            $scope.errorMessage = undefined;

            var request = {};
            for (var p in $scope.paging) {
                if ($scope.paging.hasOwnProperty(p)) {
                    request[p] = $scope.paging[p];
                }
            }

            // Workaround against strange behavior of Angular's select ng-value=null
            // https://stackoverflow.com/questions/23686118/angularjs-null-value-for-select
            if (request.tableTypeFilter === 'null') {
                request.tableTypeFilter = null;
            }
            if (request.ruleTypeFilter === 'null') {
                request.ruleTypeFilter = null;
            }

            pluginKnoxService.searchRules(request, function (response) {
                if (response.status === 'OK') {
                    $scope.rules = response.data.items;
                    $scope.paging.totalItems = response.data.totalItemsCount;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                loading = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            })
        };

        $scope.search = function () {
            $scope.errorMessage = undefined;
            $scope.paging.pageNum = 1;
            loadData();
        };

        $scope.addRule = function() {
            $scope.editRule({configurationId: configurationId,
                customerId: $scope.customerId,
                tableType: "URL",
                ruleType: "BLACKLIST"});
        }

        $scope.editRule = function(rule) {
            rule.customerId = $scope.customerId;
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/knox/views/rule.modal.html',
                controller: 'PluginKnoxEditRuleController',
                resolve: {
                    rule: function () {
                        return rule;
                    }
                }
            });

            modalInstance.result.then(function (saved) {
                if (saved) {
                    loadData();
                }
            });

        };

        $scope.removeRule = function (rule) {
            var localizedText = localization.localize('plugin.knox.question.delete.rule').replace('${rulename}', rule.rule);
            confirmModal.getUserConfirmation(localizedText, function () {
                pluginKnoxService.deleteRule({id: rule.id}, function (response) {
                    if (response.status === 'OK') {
                        loadData();
                    } else {
                        alertService.showAlertMessage(localization.localize('error.internal.server'));
                    }
                });
            });
        };

        loadData();

    })
    .controller('PluginKnoxEditRuleController', function ($scope, $modal, $modalInstance,
                                                          localization, pluginKnoxService, rule) {

        var ruleCopy = {};
        for (var p in rule) {
            if (rule.hasOwnProperty(p)) {
                ruleCopy[p] = rule[p];
            }
        }

        $scope.rule = ruleCopy;
        $scope.saving = false;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            // Validate form
            if (!$scope.rule.rule || $scope.rule.rule.length === 0) {
                $scope.errorMessage = localization.localize('plugin.knox.error.empty.rule.pattern');
            } else {
                $scope.saving = true;
                pluginKnoxService.saveRule($scope.rule, function (response) {
                    $scope.saving = false;
                    if (response.status === 'OK') {
                        $modalInstance.close(true);
                    } else {
                        $scope.errorMessage = localization.localize(response.message);
                    }
                }, function () {
                    $scope.saving = false;
                    $scope.errorMessage = localization.localize('error.request.failure');
                });
            }
        };

    })
    .controller('PluginKnoxCopyProfileController', function ($scope, $modal, $modalInstance, localization,
                                                             pluginKnoxService, configurationService, request) {

        $scope.request = request;

        configurationService.getAllConfigurations({},
            function (response) {
                $scope.configurations = response.data;
            });

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.copy = function () {
            $scope.saving = true;
            pluginKnoxService.copyProfile($scope.request, function (response) {
                $scope.saving = false;
                $modalInstance.close(response.status === 'OK' ? false : localization.localize('error.request.failure'));
            }, function () {
                $scope.saving = false;
                $modalInstance.close(localization.localize('error.request.failure'));
            });
        };

    })
    .run(function (localization) {
        localization.loadPluginResourceBundles("knox");
    })
;

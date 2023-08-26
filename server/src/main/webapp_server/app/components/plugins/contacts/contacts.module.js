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
angular.module('plugin-contacts', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-contacts', {
                url: "/" + 'plugin-contacts/{deviceNumber}',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.contacts.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-contacts';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-contacts', e);
        }
    })
    .factory('pluginContactsService', function ($resource) {
        return $resource('', {}, {
            getContactsSettings: {url: 'rest/plugins/contacts/:configurationId', method: 'GET'},
            saveContactsSettings: {url: 'rest/plugins/contacts/', method: 'PUT'}
        });
    })
    .controller('PluginContactsTabController', function ($scope, $rootScope, $stateParams, pluginContactsService,
                                                            configurationService, localization) {

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.localization = localization;

        $scope.data = {
            configurationId: 0
        };

        var clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        configurationService.getAllConfigurations(function (response) {
            $scope.configurations = response.data;
        });

        $scope.onConfigSelect = function() {
            clearMessages();
            var configId = $scope.data.configurationId;
            pluginContactsService.getContactsSettings({"configurationId": configId}, function(response) {
                if (response.status === 'OK') {
                    $scope.data = response.data;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            })
        };

        $scope.save = function() {
            clearMessages();
            pluginContactsService.saveContactsSettings($scope.data, function(response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('plugin.contacts.data.saved');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        }
    })
    .run(function (localization) {
        localization.loadPluginResourceBundles("contacts");
    })
;

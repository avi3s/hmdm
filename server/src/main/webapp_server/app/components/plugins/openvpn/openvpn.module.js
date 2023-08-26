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

angular.module('plugin-openvpn', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-settings-openvpn', {
                url: "/" + 'plugin-settings-openvpn/',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.openvpn.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-openvpn';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-openvpn', e);
        }
    })
    .factory('pluginOpenVPNService', function ($resource) {
        return $resource('', {}, {
            lookupDevices: {url: 'rest/private/devices/autocomplete', method: 'POST'},
            getOpenVPNSettings: {url: 'rest/plugins/openvpn/:configurationId', method: 'GET'},
            saveOpenVPNSettings: {url: 'rest/plugins/openvpn/', method: 'PUT'},
            runOpenVPNApp: {url: 'rest/plugins/openvpn/run/', method: 'PUT'}
        });
    })
    .controller('PluginOpenVPNTabController', function ($scope, $rootScope, $stateParams, pluginOpenVPNService,
                                                        $modal, configurationService, localization) {

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.localization = localization;

        $scope.data = {
            configurationId: 0
        };

        $scope.run = {
            scope: "",
            configurationId: null,
            deviceNumber: null
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
            pluginOpenVPNService.getOpenVPNSettings({"configurationId": configId}, function(response) {
                if (response.status === 'OK') {
                    $scope.data = response.data;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            })
        };

        $scope.upload = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/file.html',
                // Defined in files.controller.js
                controller: 'FileModalController'
            });

            modalInstance.result.then(function (data) {
                if (data) {
                    $scope.data.vpnUrl = data.url;
                }
            });
        };

        var saveSettings = function(request, message) {
            pluginOpenVPNService.saveOpenVPNSettings(request, function(response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize(message);
                } else {
                    if (response.message == 'error.application.not.found') {
                        $scope.errorMessage = localization.localize('plugin.openvpn.install.app');
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }
            });
        };

        var getDeviceInfo = function( device ) {
            if ( device.info ) {
                try {
                    return JSON.parse( device.info );
                } catch ( e ) {}
            }

            return undefined;
        };

        var resolveDeviceField = function (serverData, deviceInfoData) {
            if (serverData === deviceInfoData) {
                return serverData;
            } else if (serverData.length === 0 && deviceInfoData.length > 0) {
                return deviceInfoData;
            } else if (serverData.length > 0 && deviceInfoData.length === 0) {
                return serverData;
            } else {
                return deviceInfoData;
            }
        };

        $scope.getDevices = function(val) {
            return pluginOpenVPNService.lookupDevices(val).$promise.then(function(response){
                if (response.status === 'OK') {
                    return response.data.map(function (device) {
                        var deviceInfo = getDeviceInfo(device);
                        var serverIMEI = device.imei || '';
                        var deviceInfoIMEI = deviceInfo ? (deviceInfo.imei || '') : '';
                        var resolvedIMEI = resolveDeviceField(serverIMEI, deviceInfoIMEI);

                        return device.name + (resolvedIMEI.length > 0 ? " / " + resolvedIMEI : "");
                    });
                } else {
                    return [];
                }
            });
        };

        $scope.setDefault = function() {
            clearMessages();
            var request = {};
            for (var prop in $scope.data) {
                request[prop] = $scope.data[prop];
            }
            request.configurationId = 0;
            saveSettings(request, 'plugin.openvpn.defaults.saved');
        };

        $scope.loadDefault = function() {
            clearMessages();
            pluginOpenVPNService.getOpenVPNSettings({"configurationId": 0}, function(response) {
                if (response.status === 'OK') {
                    var configId = $scope.data.configurationId;
                    $scope.data = response.data;
                    $scope.data.configurationId = configId;
                    $scope.errorMessage = localization.localize('plugin.openvpn.unsaved');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            })
        };

        $scope.save = function() {
            clearMessages();
            saveSettings($scope.data, 'plugin.openvpn.data.saved');
        };

        $scope.runApp = function() {
            clearMessages();
            pluginOpenVPNService.runOpenVPNApp($scope.run, function(response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('plugin.openvpn.run.pushed');
                    $scope.run = {
                        scope: "",
                        configurationId: null,
                        deviceNumber: null
                    };
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };
    })
    .run(function (localization) {
        localization.loadPluginResourceBundles("openvpn");
    })
;

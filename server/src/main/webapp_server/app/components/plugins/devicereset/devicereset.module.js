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
angular.module('plugin-devicereset', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-devicereset', {
                url: "/" + 'plugin-devicereset/{deviceNumber}',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.devicereset.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-devicereset';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-devicereset', e);
        }
    })
    .factory('pluginDeviceResetService', function ($resource) {
        return $resource('', {}, {
            requestDeviceReboot: {url: 'rest/plugins/devicereset/private/reboot/:deviceId', method: 'PUT'},
            requestDeviceReset: {url: 'rest/plugins/devicereset/private/:deviceId', method: 'PUT'},
            requestDeviceLock: {url: 'rest/plugins/devicereset/private/lock', method: 'PUT'},
            requestPasswordReset: {url: 'rest/plugins/devicereset/private/password', method: 'PUT'},
            getDevice: {url: 'rest/plugins/devicereset/private/device/:deviceNumber', method: 'GET'},
            getStatus: {url: 'rest/plugins/devicereset/private/status/:deviceNumber', method: 'GET'},
        });
    })
    .controller('PluginDeviceResetTabController', function ($scope, $rootScope, $stateParams, $http, $location,
                                                            $modal, $filter, confirmModal, pluginDeviceResetService,
                                                            alertService, localization) {

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.formatDate = function(ts) {
            var format = localization.localize('plugin.devicereset.time.format');
            return ts ? $filter('date')(ts, format) : '-';
        }

        $scope.localization = localization;

        var deviceNumber = $stateParams.deviceNumber;
        $scope.formData = {
            deviceNumber: deviceNumber
        };

        var clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        var getDeviceInfo = function( device ) {
            if ( device.info ) {
                try {
                    return JSON.parse( device.info );
                } catch ( e ) {}
            }

            return undefined;
        };

        var deviceLookupFormatter = function (v) {
            if (v) {
                var pos = v.indexOf('/');
                if (pos > -1) {
                    return v.substr(0, pos).trim();
                }
            }
            return v;
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

        $scope.loading = false;
        var loadData = function () {
            if ($scope.formData.deviceNumber) {
                clearMessages();

                $scope.loading = true;

                pluginDeviceResetService.getStatus({deviceNumber: deviceLookupFormatter($scope.formData.deviceNumber)}, function (response) {
                    if (response.status === "OK") {
                        $scope.loading = false;
                        $scope.device = response.data;
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, function () {
                    $scope.loading = false;
                    $scope.errorMessage = localization.localize("error.request.failure");
                });
            }
        };

        $scope.deviceLookupFormatter = deviceLookupFormatter;

        $scope.searchDevices = function (val) {
            return $http.get('rest/plugins/devicereset/private/search/device?limit=10&filter=' + val)
                .then(function (response) {
                    if (response.data.status === 'OK') {
                        return response.data.data.map(function (device) {
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

        $scope.search = function () {
            clearMessages();
            loadData();
        };

        $scope.cancel = function () {
            $location.url('/');
        };

        $scope.deviceReset = function () {
            var localizedText = localization.localize('plugin.devicereset.confirm').replace('${deviceNumber}',
                deviceLookupFormatter($scope.formData.deviceNumber));
            confirmModal.getUserConfirmation(localizedText, function () {

                clearMessages();

                pluginDeviceResetService.requestDeviceReset({deviceId: $scope.device.deviceId}, {}, function (response) {
                    if (response.status === 'OK') {
                        $scope.successMessage = localization.localize('plugin.devicereset.success');
                        $scope.device = response.data;
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, function () {
                    $scope.loading = false;
                    $scope.errorMessage = localization.localize("error.request.failure");
                });
            });
        };

        $scope.deviceReboot = function () {
            var localizedText = localization.localize('plugin.devicereset.reboot.confirm').replace('${deviceNumber}',
                deviceLookupFormatter($scope.formData.deviceNumber));
            confirmModal.getUserConfirmation(localizedText, function () {

                clearMessages();

                pluginDeviceResetService.requestDeviceReboot({deviceId: $scope.device.deviceId}, {}, function (response) {
                    if (response.status === 'OK') {
                        $scope.successMessage = localization.localize('plugin.devicereset.reboot.success');
                        $scope.device = response.data;
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, function () {
                    $scope.loading = false;
                    $scope.errorMessage = localization.localize("error.request.failure");
                });
            });
        };

        $scope.deviceLock = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/devicereset/views/lock.modal.html',
                controller: 'LockConfirmModalController',
                resolve: {
                    device: function () {
                        return $scope.device;
                    }
                }
            });

            modalInstance.result.then(function(device) {
                if (device.deviceLocked) {
                    clearMessages();

                    pluginDeviceResetService.requestDeviceLock({
                        deviceId: device.deviceId,
                        lock: device.deviceLocked,
                        message: device.lockMessage
                    }, function (response) {
                        if (response.status === 'OK') {
                            $scope.successMessage = localization.localize('plugin.devicereset.lock.success');
                            $scope.device = response.data;
                        } else {
                            $scope.errorMessage = localization.localizeServerResponse(response);
                        }
                    }, function () {
                        $scope.loading = false;
                        $scope.errorMessage = localization.localize("error.request.failure");
                    });

                }
            });
        };

        $scope.deviceUnlock = function () {
            clearMessages();

            pluginDeviceResetService.requestDeviceLock({
                deviceId: $scope.device.deviceId,
                lock: false,
                message: ""
            }, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('plugin.devicereset.unlock.success');
                    $scope.device = response.data;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.loading = false;
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };


        $scope.passwordReset = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/devicereset/views/password.modal.html',
                controller: 'PasswordModalController',
                resolve: {
                    device: function () {
                        return $scope.device;
                    }
                }
            });

            modalInstance.result.then(function(device) {
                if (device.passwordReset) {
                    clearMessages();

                    pluginDeviceResetService.requestPasswordReset({
                        deviceId: device.deviceId,
                        password: device.password
                    }, function (response) {
                        if (response.status === 'OK') {
                            $scope.successMessage = localization.localize('plugin.devicereset.password.reset.success');
                            $scope.device = response.data;
                        } else {
                            $scope.errorMessage = localization.localizeServerResponse(response);
                        }
                    }, function () {
                        $scope.loading = false;
                        $scope.errorMessage = localization.localize("error.request.failure");
                    });

                }
            });
        };

        loadData();

    })
    .controller('LockConfirmModalController', function ($scope, $modalInstance, device) {
        var deviceCopy = {};
        for (var p in device) {
            if (device.hasOwnProperty(p)) {
                deviceCopy[p] = device[p];
            }
        }

        $scope.device = deviceCopy;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.lock = function () {
            $scope.device.deviceLocked = true;
            $modalInstance.close($scope.device);
        };
    })
    .controller('PasswordModalController', function ($scope, $modalInstance, device) {
        var deviceCopy = {};
        for (var p in device) {
            if (device.hasOwnProperty(p)) {
                deviceCopy[p] = device[p];
            }
        }

        $scope.device = deviceCopy;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.passwordReset = function () {
            $scope.device.passwordReset = true;
            $modalInstance.close($scope.device);
        };
    })
    .run(function ($rootScope, $location, localization) {
        $rootScope.$on('plugin-devicereset-device-selected', function (event, device) {
            $location.url('/plugin-devicereset/' + device.number);
        })
        localization.loadPluginResourceBundles("devicereset");
    })
;

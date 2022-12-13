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
angular.module('plugin-deviceimport', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        // TODO : #5937 : Localization : localize ncyBreadcrumb.label
        try {
            $stateProvider.state('plugin-deviceimport', {
                url: "/" + 'plugin-deviceimport',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.deviceimport.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-deviceimport';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-devicelog', e);
        }
    })
    .factory('pluginDeviceImportService', function ($resource) {
        return $resource('', {}, {
            getLookupData: {url: 'rest/plugins/deviceimport/private/lookup', method: 'GET'},
            parseFile: {url: 'rest/plugins/deviceimport/private/parseFile', method: 'POST'},
            importDevices: {url: 'rest/plugins/deviceimport/private/import', method: 'POST'},
        });
    })
    .controller('PluginDeviceImportTabController', function ($scope, $rootScope, $window, $location, $interval, $http,
                                                             pluginDeviceImportService, settingsService, confirmModal, alertService,
                                                             authService, localization) {

        $scope.hasPermission = authService.hasPermission;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        var columnOptions = [];
        for (var i = 0; i < 27; i++) {
            columnOptions.push({
                id: i,
                name: localization.localize('plugin.deviceimport.column.' + i)
            });
        }
        $scope.columnOptions = columnOptions;

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.onStartedUpload = function (files) {
            clearMessages();

            $scope.invalidFile = false;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.fileName = files[0].name;

                let excelFileRequired = $scope.request.importType === 'XLSX';
                if (excelFileRequired) {
                    if ($scope.fileName.endsWith(".xls") || $scope.fileName.endsWith(".xlsx")) {
                        $scope.loading = true;
                        $scope.successMessage = localization.localize('success.uploading.file');
                    } else {
                        $scope.errorMessage = localization.localize('plugin.deviceimport.error.xls.file.required');
                        $scope.invalidFile = true;
                    }
                }

                let csvFileRequired = $scope.request.importType === 'CSV';
                if (csvFileRequired) {
                    if ($scope.fileName.endsWith(".csv") || $scope.fileName.endsWith(".txt")) {
                        $scope.loading = true;
                        $scope.successMessage = localization.localize('success.uploading.file');
                    } else {
                        $scope.errorMessage = localization.localize('plugin.deviceimport.error.csv.file.required');
                        $scope.invalidFile = true;
                    }
                }
            }
        };

        $scope.fileUploaded = function (response) {
            clearMessages();

            $scope.fileSelected = false;

            $scope.loading = false;

            if (!$scope.invalidFile) {
                if (response.data.status === 'OK') {
                    $scope.file.path = response.data.data.fileName;
                    $scope.successMessage = localization.localize('success.file.uploaded');
                    $scope.fileSelected = true;
                    $scope.request.filePathId = response.data.data.uuid;
                } else {
                    $scope.errorMessage = localization.localize(response.data.message);
                }
            } else {
                let excelFileRequired = $scope.request.importType === 'XLSX';
                if (excelFileRequired) {
                    $scope.errorMessage = localization.localize('plugin.deviceimport.error.xls.file.required');
                }

                let csvFileRequired = $scope.request.importType === 'CSV';
                if (csvFileRequired) {
                    $scope.errorMessage = localization.localize('plugin.deviceimport.error.csv.file.required');
                }
            }
        };
        
        $scope.clearFile = function () {
            $scope.file = {};
            clearMessages();
            $scope.fileSelected = false;
            $scope.invalidFile = false;
            $scope.loading = false;
        };

        $scope.toStep1 = function () {
            $scope.step = 1;
        };

        $scope.doImport = function () {
            clearMessages();

            var fileRequired = $scope.request.importType !== 'LIST';

            if (!$scope.request.configurationId) {
                $scope.errorMessage = localization.localize('plugin.deviceimport.error.configuration.required');
            } else if (!$scope.request.deviceNumberColumnIndex) {
                $scope.errorMessage = localization.localize('plugin.deviceimport.error.devicenumber.required');
            } else if (fileRequired && (!$scope.fileSelected || $scope.invalidFile)) {
                $scope.errorMessage = localization.localize('plugin.deviceimport.error.file.required');
            } else if (!fileRequired && (!$scope.request.listContent || $scope.request.listContent.length === 0)) {
                $scope.errorMessage = localization.localize('plugin.deviceimport.error.list.required');
            } else {
                $scope.loading = true;
                $scope.successMessage = localization.localize('plugin.deviceimport.parsing');

                pluginDeviceImportService.parseFile($scope.request, function (response) {
                    $scope.loading = false;
                    clearMessages();

                    if (response.status === 'OK') {
                        var statusOk = localization.localize('plugin.deviceimport.device.status.ok');
                        var statusExists = localization.localize('plugin.deviceimport.device.status.exists');
                        var statusDuplicate = localization.localize('plugin.deviceimport.device.status.repeating');

                        response.data.devices.forEach(function (item) {
                            if (item.existingDeviceId) {
                                item.status = statusExists;
                            } else {
                                item.status = statusOk;
                            }
                            if (item.count > 0) {
                                item.status = statusDuplicate;
                            }
                        });
                        $scope.importRequest.uuid = response.data.uuid;
                        $scope.devices = response.data.devices;
                        $scope.step = 2;
                    } else {
                        $scope.errorMessage = localization.localize(response.message);
                    }
                }, function (response) {
                    $scope.loading = false;
                    clearMessages();
                    alertService.onRequestFailure(response);
                });
            }
        };

        $scope.doImport2 = function () {
            clearMessages();
            $scope.loading = true;
            $scope.successMessage = localization.localize('plugin.deviceimport.importing');

            pluginDeviceImportService.importDevices($scope.importRequest, function (response) {
                $scope.loading = false;
                clearMessages();

                if (response.status === 'OK') {
                    $scope.importResult = response.data;
                    $scope.step = 3;
                    if ($scope.importResult.devicesSkippedByLicense > 0) {
                        $scope.errorMessage = localization.localize('plugin.deviceimport.license.limit').replace('${devices}',
                            $scope.importResult.devicesSkippedByLicense);
                    }
                } else {
                    $scope.errorMessage = localization.localize(response.message);
                }
            }, function (response) {
                $scope.loading = false;
                clearMessages();
                alertService.onRequestFailure(response);
            });

        };

        $scope.newImport = function () {
            initScope();
        };

        var loading = false;

        var loadData = function () {
            $scope.errorMessage = undefined;

            loading = true;

            settingsService.getSettings({}, function(response) {
                if (response.data) {
                    $scope.settings = response.data;

                    pluginDeviceImportService.getLookupData({}, function (response) {
                        loading = false;
                        if (response.status === 'OK') {
                            $scope.groups = response.data.groups;
                            $scope.configurations = response.data.configurations;
                        } else {
                            $scope.errorMessage = localization.localizeServerResponse(response);
                        }
                    }, function () {
                        loading = false;
                        $scope.errorMessage = localization.localize('error.request.failure');
                    })
                } else {
                    loading = false;
                    $scope.errorMessage = localization.localize('error.request.failure');
                }
            }, function() {
                loading = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            });
        };

        var clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        var initScope = function () {
            $scope.configurations = [];
            $scope.groups = [];

            $scope.file = {};
            $scope.loading = false;
            $scope.fileName = null;
            $scope.invalidFile = false;
            $scope.fileSelected = false;

            $scope.request = {
                groupId: null,
                configurationId: null,
                deviceNumberColumnIndex: 1,
                imeiColumnIndex: 0,
                phoneNumberColumnIndex: 0,
                descriptionColumnIndex: 0,
                custom1ColumnIndex: 0,
                custom2ColumnIndex: 0,
                custom3ColumnIndex: 0,
                filePathId: null,
                listContent: null,
                importType: 'XLSX'
            };
            $scope.devices = [];
            $scope.importRequest = {
                existingMode: 2,
                uuid: null
            };
            $scope.importResult = {};

            $scope.step = 1;

            $scope.importTypeOptions = [
                {id: 'XLSX', name: localization.localize('plugin.deviceimport.selection.file.type.excel')},
                {id: 'CSV', name: localization.localize('plugin.deviceimport.selection.file.type.csv')},
                {id: 'LIST', name: localization.localize('plugin.deviceimport.selection.file.type.list')},
            ];

            loadData();
        };

        initScope();

    })
    .run(function (localization) {
        localization.loadPluginResourceBundles("deviceimport");
    });






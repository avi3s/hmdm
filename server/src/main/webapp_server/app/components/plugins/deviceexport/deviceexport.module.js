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
angular.module('plugin-deviceexport', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        // TODO : #5937 : Localization : localize ncyBreadcrumb.label
        try {
            $stateProvider.state('plugin-deviceexport', {
                url: "/" + 'plugin-deviceexport',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: "Device Export" //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-deviceexport';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-deviceexport', e);
        }
    })
    .factory('pluginDeviceExportService', function ($resource) {
        return $resource('', {}, {
            getLookupData: {url: 'rest/plugins/deviceexport/private/lookup', method: 'GET'},
            exportDevices: {
                url: 'rest/plugins/deviceexport/private/export',
                method: 'POST',
                responseType: 'arraybuffer',
                cache: false,
                transformResponse: function (data) {
                    return {
                        response: new Blob([data], {
                            // type: "text/plain"
                        })
                    };
                }
            },
        });
    })
    .controller('PluginDeviceExportTabController', function ($scope, $rootScope, $window, $location, $interval, $http,
                                                             pluginDeviceExportService, confirmModal, alertService,
                                                             settingsService, authService, localization) {

        $scope.hasPermission = authService.hasPermission;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.configurations = [];
        $scope.groups = [];
        $scope.loading = false;

        $scope.configurationsList = [];
        $scope.configurationsSelection = [];
        $scope.configurationsFilteringTexts = {
            'buttonDefaultText': localization.localize('table.filtering.no.selected.configuration'),
            'checkAll': localization.localize('table.filtering.check.all'),
            'uncheckAll': localization.localize('table.filtering.uncheck.all'),
            'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.configuration')
        };


        $scope.groupsList = [];
        $scope.groupsSelection = [];
        $scope.groupsFilteringTexts = {
            'buttonDefaultText': localization.localize('table.filtering.no.selected.group'),
            'checkAll': localization.localize('table.filtering.check.all'),
            'uncheckAll': localization.localize('table.filtering.uncheck.all'),
            'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.group')
        };

        $scope.columnsList = [
            {id: 'devicenumber', label: localization.localize('plugin.deviceexport.column.devicenumber')},
            {id: 'imei', label: localization.localize('plugin.deviceexport.column.imei')},
            {id: 'phone', label: localization.localize('plugin.deviceexport.column.phone')},
            {id: 'description', label: localization.localize('plugin.deviceexport.column.description')},
            {id: 'group', label: localization.localize('plugin.deviceexport.column.group')},
            {id: 'configuration', label: localization.localize('plugin.deviceexport.column.configuration')},
            {id: 'launcher.version', label: localization.localize('plugin.deviceexport.column.launcher.version')},
            {id: 'permission.status', label: localization.localize('plugin.deviceexport.column.permission.status')},
            {id: 'installation.status', label: localization.localize('plugin.deviceexport.column.installation.status')},
            {id: 'sync.time', label: localization.localize('plugin.deviceexport.column.sync.time')},
            {id: 'model', label: localization.localize('plugin.deviceexport.column.model')},
            {id: 'launcher', label: localization.localize('plugin.deviceexport.column.launcher')}
        ];

        $scope.columnsSelection = $scope.columnsList.slice();

        $scope.columnsFilteringTexts = {
            'buttonDefaultText': localization.localize('plugin.deviceexport.no.selected.columns'),
            'checkAll': localization.localize('table.filtering.check.all'),
            'uncheckAll': localization.localize('table.filtering.uncheck.all'),
            'dynamicButtonTextSuffix': localization.localize('plugin.deviceexport.suffix.column')
        };

        settingsService.getSettings({}, function(response) {
            if (response.data) {
                if (response.data.customPropertyName1) {
                    $scope.columnsList.push({id: 'custom1', label: response.data.customPropertyName1});
                }
                if (response.data.customPropertyName2) {
                    $scope.columnsList.push({id: 'custom2', label: response.data.customPropertyName2});
                }
                if (response.data.customPropertyName3) {
                    $scope.columnsList.push({id: 'custom3', label: response.data.customPropertyName3});
                }
                if (response.data.customPropertyName1 ||
                    response.data.customPropertyName2 ||
                    response.data.customPropertyName3) {
                    $scope.columnsSelection = $scope.columnsList.slice();
                }
            }
        });

        $scope.request = {
            groupId: 0,
            configurationId: 0,
            filterType: 2,
            exportType: 'XLSX'
        };

        $scope.filterTypeOptions = [
            {id: 1, name: localization.localize('plugin.deviceexport.selection.group')},
            {id: 2, name: localization.localize('plugin.deviceexport.selection.configuration')}
        ];

        $scope.exportTypeOptions = [
            {id: 'XLSX', name: localization.localize('plugin.deviceexport.selection.file.type.excel')},
            {id: 'CSV', name: localization.localize('plugin.deviceexport.selection.file.type.csv')}
        ];

        $scope.doExport = function () {
            if ($scope.columnsSelection.length == 0) {
                alertService.showAlertMessage(localization.localize('plugin.deviceexport.column.selection.warning'));
                return;
            }

            clearMessages();
            $scope.loading = true;
            $scope.successMessage = localization.localize('plugin.deviceexport.exporting');

            var exportRequest = {
                filterType: $scope.request.filterType,
                exportType: $scope.request.exportType
            };

            exportRequest.columns = $scope.columnsSelection.map(function (item) {
                return item.id;
            });

            if ($scope.request.filterType === 1) {
                exportRequest.filterIds = $scope.groupsSelection.map(function (item) {
                    return item.id;
                });
            } else {
                exportRequest.filterIds = $scope.configurationsSelection.map(function (item) {
                    return item.id;
                });
            }

            var fileName = 'devices';
            if (exportRequest.filterIds.length === 1 && exportRequest.filterIds[0] > 0) {
                var targetArray;
                if (exportRequest.filterType === 1) {
                    targetArray = $scope.groups;
                } else {
                    targetArray = $scope.configurations;
                }

                var item = targetArray.find(function (item) {
                    return item.id === exportRequest.filterIds[0];
                });

                if (item) {
                    fileName = item.name;
                }
            }

            exportRequest.locale = localization.getLocale();

            pluginDeviceExportService.exportDevices(exportRequest, function (data) {
                $scope.loading = false;
                clearMessages();

                var downloadableBlob = URL.createObjectURL(data.response);

                var link = document.createElement('a');
                link.href = downloadableBlob;
                if (exportRequest.exportType === 'XLSX') {
                    link.download = fileName + '.xlsx';
                } else {
                    link.download = fileName + '.txt';
                }

                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }, function (response) {
                $scope.loading = false;
                clearMessages();
                alertService.onRequestFailure(response);
            });
        };

        var loading = false;

        var loadData = function () {
            $scope.errorMessage = undefined;

            loading = true;

            pluginDeviceExportService.getLookupData({}, function (response) {
                loading = false;
                if (response.status === 'OK') {
                    $scope.groups = [{id: 0, name: localization.localize('plugin.deviceexport.selection.all')}]
                        .concat(response.data.groups);
                    $scope.groupsList = response.data.groups.map(function (item) {
                        return {id: item.id, label: item.name};
                    });
                    $scope.configurations = [{id: 0, name: localization.localize('plugin.deviceexport.selection.all')}]
                        .concat(response.data.configurations);
                    $scope.configurationsList = response.data.configurations.map(function (item) {
                        return {id: item.id, label: item.name};
                    });
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                loading = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            })
        };

        var clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        loadData();

    })
    .run(function (localization) {
        localization.loadPluginResourceBundles("deviceexport");
    });






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
angular.module('plugin-devicelocations', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .constant('PLUGIN_DEVICE_LOCATIONS_MAP_ICON_CONFIGS', {
        'default': {
            maxClusterRadius: 13,
            iconSize: [25, 40],
            shadowSize: [40, 40],
            iconAnchor: [12, 39],
            popupAnchor: [0, -40]
        },
        'smaller': {
            maxClusterRadius: 10,
            iconSize: [15, 24],
            shadowSize: [24, 24],
            iconAnchor: [10, 24],
            popupAnchor: [-2, -26]
        }
    })
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-devicelocations', {
                url: "/" + 'plugin-devicelocations',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.devicelocations.title" | localize}}'
                    // label: '{{formData.deviceNumber}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-devicelocations'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-devicelocations', e);
        }

        try {
            $stateProvider
                .state('plugin-devicelocations-history', {
                url: "/" + 'plugin-devicelocations-history/{deviceId}',
                templateUrl: 'app/components/plugins/devicelocations/views/history.html',
                controller: 'PluginDeviceLocationHistoryController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.devicelocations.history.title" | localize}}', //label to show in breadcrumbs
                    parent: 'plugin-devicelocations'
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-devicelocations-history', e);
        }

        try {
            $stateProvider.state('plugin-settings-devicelocations', {
                url: "/" + 'plugin-settings-devicelocations',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.devicelocations.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-devicelocations'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-devicelocations', e);
        }
    })
    .factory('pluginDeviceLocationsService', function ($resource) {
        return $resource('', {}, {
            getSettings: {url: 'rest/plugins/devicelocations/devicelocations-plugin-settings/private', method: 'GET'},
            saveSettings: {url: 'rest/plugins/devicelocations/devicelocations-plugin-settings/private', method: 'PUT'},
            getLookupData: {url: 'rest/plugins/devicelocations/devicelocations/private/lookup', method: 'GET'},
            searchDevices: {url: 'rest/plugins/devicelocations/devicelocations/private/devices', method: 'GET'},
            getDeviceLocationHistoryPreview: {url: 'rest/plugins/devicelocations/devicelocations/private/device/:deviceId/history/preview', method: 'GET'},
            getDeviceLocationHistory: {url: 'rest/plugins/devicelocations/devicelocations/private/device/:deviceId/history', method: 'GET'},
            getDeviceLatestLocation: {
                url: 'rest/plugins/devicelocations/devicelocations/private/device/:id/location',
                method: 'GET'
            },
            getDevicesLatestLocations: {
                url: 'rest/plugins/devicelocations/devicelocations/private/devices/locations',
                method: 'GET'
            },

        });
    })
    .factory('pluginDeviceLocationsExportService', function ($resource) {
        return $resource('', {}, {
            exportLocationsHistory: {
                url: 'rest/plugins/devicelocations/devicelocations/private/device/:deviceId/history/export',
                method: 'GET',
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
    .controller('PluginDeviceLocationsSettingsController', function ($scope, $rootScope,
                                                                     pluginDeviceLocationsService, localization) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.settings = {};

        pluginDeviceLocationsService.getSettings(function (response) {
            if (response.status === 'OK') {
                $scope.settings = response.data;
            } else {
                $scope.errorMessage = localization.localize('error.internal.server');
            }
        });
        $scope.save = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;

            pluginDeviceLocationsService.saveSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('plugin.devicelocations.settings.saved');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        }
    })
    .controller('PluginDeviceLocationsController', function ($rootScope, $scope, $state,
                                                             $window, $timeout, $interval, $http, $compile,
                                                             authService, hmdmMap, PLUGIN_DEVICE_LOCATIONS_MAP_ICON_CONFIGS,
                                                             pluginDeviceLocationsService, localization) {

        // **************
        // Internal stuff
        // **************
        const localStorage = $window.localStorage;
        const deviceSelectionAttrName = "hmtm-devicelocations-devices-" + authService.getUser().id;
        const map = hmdmMap.get();

        // A list of devices having the checkboxes switched on
        var deviceSelection = undefined;

        $scope.selectAll = {
            value: false
        };

        // Restore the initial state of device selection checkboxes from local storage (if available)
        const deviceSelectionStr = localStorage.getItem(deviceSelectionAttrName);
        const selectedDeviceIds = [];
        try {
            deviceSelection = JSON.parse(deviceSelectionStr);
            angular.forEach(deviceSelection, function (on, deviceId) {
                if (on) {
                    selectedDeviceIds.push(deviceId);
                }
            });
        } catch (e) {
        }
        if (!deviceSelection) {
            deviceSelection = {};
        }


        const loadData = function (callback) {
            clearMessages();

            pluginDeviceLocationsService.getLookupData({}, function (response) {
                if (response.status === 'OK') {
                    $scope.groups = [{id: -1, name: localization.localize('plugin.devicelocations.group.selection')}]
                        .concat(response.data.groups);
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.errorMessage = localization.localize('error.request.failure');
            });

            if (selectedDeviceIds.length > 0) {
                const fetchSize = 20;
                var start = 0;
                while (start < selectedDeviceIds.length) {
                    pluginDeviceLocationsService.getDevicesLatestLocations({ids: selectedDeviceIds.slice(start, start + fetchSize)}, function (response) {
                        if (response.status === 'OK') {
                            const result = response.data;
                            result.forEach(function (item) {
                                addMarkerToMap(item.device, item.location);
                            });

                            if (callback) {
                                callback();
                            }
                        } else {
                            console.error("Failed to load latest locations for devices", localization.localizeServerResponse(response))
                        }
                    }, function (response) {
                        console.error("Failed to load latest locations for devices", localization.localize('error.request.failure'));
                    });

                    start += fetchSize;
                }
            }
        };

        /**
         * Gets the list of devices matching the criteria currently set by user from the server.
         */
        const searchDevices = function () {
            clearMessages();

            if ($scope.loading) {
                console.log("Skipping to query for list of devices since a previous request is pending");
                return;
            }

            var request = angular.copy($scope.paging, {});
            delete request.totalItemsCount;

            $scope.loading = true;

            pluginDeviceLocationsService.searchDevices(request, function (response) {
                $scope.loading = false;
                if (response.status === 'OK') {

                    response.data.items.forEach(function (item) {
                        item.selected = (deviceSelection[item.id] === true);
                    });

                    $scope.devices = response.data.items;
                    $scope.paging.totalItemsCount = response.data.totalItemsCount;

                    checkAllSelected();
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.loading = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            })
        };

        /**
         * Removes displayed error/success messages.
         */
        const clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        // icon configuration
        const iconConfigs = PLUGIN_DEVICE_LOCATIONS_MAP_ICON_CONFIGS;

        // ********************
        // Map stuff
        // ********************

        // A content of the marker popup template
        var popupSingleMarkerTemplate = undefined;

        /**
         * A default configuration for marker icons.
         *
         * @type {{popupAnchor: *, shadowUrl: string, iconAnchor: *, iconSize: *, iconUrl: string, shadowSize: *}}
         */
        const defaultMarkerIconConfig = {
            iconUrl: 'app/components/plugins/devicelocations/images/marker-29x41.png',
            iconSize: iconConfigs["default"].iconSize,
            shadowUrl: 'app/components/plugins/devicelocations/images/marker-shadow.png',
            shadowSize: iconConfigs["default"].shadowSize,
            iconAnchor: iconConfigs["default"].iconAnchor,
            popupAnchor: iconConfigs["default"].popupAnchor,
            maxClusterRadius: iconConfigs["default"].maxClusterRadius
        };

        /**
         * Compiles the specified template.
         *
         * @param device a device to initialize the scope bound to template.
         * @param location a latest location of device.
         * @param template a content of the template.
         * @param callback an optional callback to be called when template is compiled and ready.
         * @returns {string} a compiled template. If a callback is provided then nothing is returned but compiled template is passed
         *          to callback instead.
         */
        const compileTemplate = function (device, location, template, callback) {
            const scope = $rootScope.$new(true);

            scope.device = device;
            scope.location = location;
            scope.formatMultiLine = function (text) {
                if (!text) {
                    return text;
                } else {
                    return text.replace(/\n/g, "<br/>");
                }
            };
            scope.viewHistory = function () {
                // var url = "#/plugin-devicelocations-history/" + device.number;
                // $window.open(url, "_blank");
                $state.transitionTo('plugin-devicelocations-history', {"deviceId": device.id})
            };


            const compiledTemplate = $compile(template)(scope)[2];
            if (callback) {
                scope.$apply(function () {
                    callback(compiledTemplate);
                });
            } else {
                return compiledTemplate;
            }
        };

        /**
         * Gets the identifier to refer to marker presenting the specified device on map.
         *
         * @param device a device to get the marker identifier for.
         * @returns {string} a marker identifier for specified device.
         */
        const getDeviceMarkerIdentifier = function (device) {
            return "device-" + device.id;
        };

        /**
         * Evaluates the textual presentation of the past time interval for the device location bound to marker.
         *
         * @param hmdmProps the custom properties bound to target marker.
         */
        const timeDistanceUpdater = function (hmdmProps) {
            const location = hmdmProps.location;
            
            const diff = Math.floor(Math.floor((new Date().getTime() - location.ts) / 1000) / 60);

            var nextTickOffset = -1;
            var textKey;
            var amount;
            if (diff >= 24 * 60) {
                if (diff > 30 * 24 * 60) {
                    textKey = 'plugin.devicelocations.last.update.interval.plus30';
                    amount = 31;
                } else {
                    textKey = 'plugin.devicelocations.last.update.interval.days';
                    amount = Math.floor(Math.floor(diff / 24) / 60);
                    nextTickOffset = 24 * 3600 * 1000;
                }
            } else if (diff >= 60) {
                textKey = 'plugin.devicelocations.last.update.interval.hours';
                amount = Math.floor(diff / 60);
                nextTickOffset = 3600 * 1000;
            } else {
                textKey = 'plugin.devicelocations.last.update.interval.minutes';
                amount = diff;
                nextTickOffset = 60 * 1000;
            }

            location.lastUpdateInterval = localization.localize(textKey).replace('${amount}', amount);

            if (nextTickOffset !== -1) {
                const locationTime = new Date(location.ts);
                
                const now = new Date().getTime();
                var tickerStartTime = new Date(now);
                tickerStartTime.setSeconds(locationTime.getSeconds(), locationTime.getMilliseconds());
                while (tickerStartTime < now) {
                    tickerStartTime = new Date(tickerStartTime.getTime() + nextTickOffset);
                }

                // console.log("Starting time update at ", tickerStartTime);

                hmdmProps.timerPromise = $timeout(function () {
                    timeDistanceUpdater(hmdmProps);
                }, tickerStartTime.getTime() - now);
            }
        };

        /**
         * A handler for "popupopen" event triggered for marker. Starts the updater for location pas time interval.
         *
         * @param event an event referencing the target marker.
         */
        const markerPopupOpenHandler = function (event) {
            const marker = event.target;
            const hmdmProps = marker.hmdmProperties;
            hmdmProps.popupIsDisplayed = true;

            hmdmProps.timerPromise = $timeout(function () {
                timeDistanceUpdater(hmdmProps);
            }, 0);
        };

        /**
         * A handler for "popupclose" event triggered for marker. Stops the updater for location pas time interval.
         *
         * @param event an event referencing the target marker.
         */
        const markerPopupCloseHandler = function (event) {
            const marker = event.target;
            const hmdmProps = marker.hmdmProperties;
            delete hmdmProps.popupIsDisplayed;

            const timerPromise = hmdmProps.timerPromise;
            if (timerPromise) {
                $timeout.cancel(timerPromise);
            }
        };

        /**
         * Adds marker for specified device location to map.
         *
         * @param {object} device the details for device.
         * @param {object} location the location of device.
         */
        const addMarkerToMap = function(device, location) {
            var marker = map.addMarker(
                getDeviceMarkerIdentifier(device),
                location.lat,
                location.lon,
                defaultMarkerIconConfig,
                device.number,
                compileTemplate(device, location, popupSingleMarkerTemplate),
                markerPopupOpenHandler,
                markerPopupCloseHandler
            );
            marker.hmdmProperties.device = device;
            marker.hmdmProperties.location = location;
        };

        /**
         * Adds the marker corresponding to latest device location for the specified device to map.
         *
         * @param device
         */
        const showDeviceOnMap = function (device) {
            clearMessages();

            const request = {id: device.id};

            clearMessages();

            pluginDeviceLocationsService.getDeviceLatestLocation(request, function (response) {
                if (response.status === 'OK') {
                    const location = response.data;

                    addMarkerToMap(device, location);

                    map.centerMap(location.lat, location.lon);

                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };

        /**
         * Removes the marker for the specified device from map.
         *
         * @param device a device to remove marker for.
         */
        var removeDeviceFromMap = function (device) {
            clearMessages();

            map.removeMarker(getDeviceMarkerIdentifier(device));
        };


        /**
         * A reference to currently selected (e.g. last clicked) marker on map and it's original z-index.
         *
         * @type {{}}
         */
        const selectedMarker = {};

        /**
         * Handler for marker selection. Centers the map on specified marker and makes it blinking for a second.
         *
         * @param marker a map marker corresponding to device.
         */
        const markerSelectHandler = function (marker) {
            if (selectedMarker.marker) {
                if (selectedMarker.marker !== marker) {
                    selectedMarker.marker.setZIndexOffset(selectedMarker.previousZIndex);
                    // if (selectedMarker.marker.hmdmProperties.popupIsDisplayed) {
                    //     selectedMarker.marker.closePopup();
                    // }
                }
            }

            if (selectedMarker.marker !== marker) {
                selectedMarker.marker = marker;
                selectedMarker.previousZIndex = marker.options.zIndexOffset;
            }

            marker.setZIndexOffset(1000);

            const identifier = marker.hmdmProperties.identifier;
            map.centerMapOnMarker(identifier);

            if (!marker.blinking) {
                const mainIcon = marker.getIcon();

                const blinkingIcon = map.createMarkerIcon(mainIcon.options);
                blinkingIcon.options.className = "blinking";

                marker.setIcon(blinkingIcon);
                marker.blinking = true;

                $timeout(function () {
                    marker.setIcon(mainIcon);
                    delete marker.blinking;
                }, 1000);
            }

        };

        /**
         * Focuses map center on marker for specified device.
         *
         * @param device a device to focus map center on.
         */
        const selectDeviceOnMap = function (device) {
            clearMessages();

            const markerIdentifier = getDeviceMarkerIdentifier(device);
            if (!map.isMarkerDisplayed(markerIdentifier)) {
                $scope.deviceSelection[device.id] = true;
                $scope.deviceSelectionChanged(device);
            }

            if (map.isMarkerDisplayed(markerIdentifier)) {
                map.selectMarker(markerIdentifier, markerSelectHandler);
            }
        };

        // ********************
        // Scope initialization
        // ********************
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.groups = [];

        $scope.paging = {
            filter: '',
            groupId: -1,
            pageNum: 1,
            pageSize: 20,
            totalItemsCount: 0
        };

        $scope.loading = false;

        $scope.searchDevices = searchDevices;

        $scope.groupSelected = function () {
            $scope.paging.pageNum = 1;
            searchDevices();
        };

        $scope.deviceSelection = deviceSelection;

        $scope.deviceSelectionChanged = function (device) {
            checkAllSelected();
            localStorage.setItem(deviceSelectionAttrName, JSON.stringify(deviceSelection));

            if (deviceSelection[device.id]) {
                showDeviceOnMap(device);
            } else {
                removeDeviceFromMap(device);
            }
        };

        const checkAllSelected = function() {
            var allSelected = $scope.devices.every(function(device) {
                return $scope.deviceSelection[device.id];
            });
            $scope.selectAll.value = allSelected;
            return allSelected;
        };

        const selectAll = function() {
            $scope.devices.forEach(function(device) {
                if (!$scope.deviceSelection[device.id]) {
                    $scope.deviceSelection[device.id] = true;
                    showDeviceOnMap(device);
                }
                $scope.selectAll.value = true;
            });
        };

        const unselectAll = function() {
            $scope.devices.forEach(function(device) {
                if ($scope.deviceSelection[device.id]) {
                    $scope.deviceSelection[device.id] = false;
                    removeDeviceFromMap(device);
                }
            });
            $scope.selectAll.value = false;
        };

        $scope.toggleSelectAll = function() {
            if (checkAllSelected()) {
                unselectAll();
            } else {
                selectAll();
            }
            localStorage.setItem(deviceSelectionAttrName, JSON.stringify(deviceSelection));
        };

        $scope.invertSelection = function() {
            $scope.devices.forEach(function(device) {
                if ($scope.deviceSelection[device.id]) {
                    $scope.deviceSelection[device.id] = false;
                    removeDeviceFromMap(device);
                } else {
                    $scope.deviceSelection[device.id] = true;
                    showDeviceOnMap(device);
                }
            });
            checkAllSelected();
        };

        $scope.selectDeviceOnMap = selectDeviceOnMap;

        $scope.$watch('paging.pageNum', function () {
            searchDevices();
        });

        // **************
        // Initialization
        // **************
        pluginDeviceLocationsService.getSettings(function (settingsResponse) {
            if (settingsResponse.status === 'OK') {
                map.initMap($scope, "map", settingsResponse.data.tileServerUrl);

                // if ($window.navigator) {
                //     if ($window.navigator.geolocation) {
                //         $window.navigator.geolocation.getCurrentPosition(function (position) {
                //             map.centerMap(position.coords.latitude, position.coords.longitude);
                //         })
                //     }
                // }

                $http.get('app/components/plugins/devicelocations/views/marker.popup.html').then(function (result) {
                    popupSingleMarkerTemplate = result.data;
                    loadData(map.fitBoundsToMarkers);
                    searchDevices();
                }).catch(function () {
                    console.error("Failed to load marker popup template");
                    loadData();
                    searchDevices(map.fitBoundsToMarkers);
                });
            } else {
                $scope.errorMessage = localization.localizeServerResponse(settingsResponse);
            }
        }, function () {
            $scope.errorMessage = localization.localize("error.request.failure");
        });
    })
    .controller('PluginDeviceLocationHistoryController', function ($scope, $stateParams, $timeout, $filter,
                                                                   localization, PLUGIN_DEVICE_LOCATIONS_MAP_ICON_CONFIGS,
                                                                   pluginDeviceLocationsService,
                                                                   pluginDeviceLocationsExportService,
                                                                   hmdmMap) {

        const map = hmdmMap.get();

        const clearMessages = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
        };

        // icon configuration
        const iconConfigs = PLUGIN_DEVICE_LOCATIONS_MAP_ICON_CONFIGS;

        /**
         * A default configuration for marker icons.
         *
         * @type {{popupAnchor: *, shadowUrl: string, iconAnchor: *, iconSize: *, iconUrl: string, shadowSize: *}}
         */
        const defaultMarkerIconConfig = {
            iconUrl: 'app/components/plugins/devicelocations/images/marker-29x41.png',
            iconSize: iconConfigs["default"].iconSize,
            shadowUrl: 'app/components/plugins/devicelocations/images/marker-shadow.png',
            shadowSize: iconConfigs["default"].shadowSize,
            iconAnchor: iconConfigs["default"].iconAnchor,
            popupAnchor: iconConfigs["default"].popupAnchor,
            maxClusterRadius: iconConfigs["default"].maxClusterRadius
        };

        /**
         * A reference to currently selected (e.g. last clicked) marker on map and it's original z-index.
         *
         * @type {{}}
         */
        const selectedMarker = {};

        const prepareRequestData = function () {
            const request = angular.copy($scope.formData, {});

            var from = new Date(request.dateFrom.getTime());
            from.setHours(request.timeFrom.getHours());
            from.setMinutes(request.timeFrom.getMinutes());
            from.setSeconds(0, 0);

            delete request.timeFrom;
            request.dateFrom = from.getTime();

            var to = new Date(request.dateTo.getTime());
            to.setHours(request.timeTo.getHours());
            to.setMinutes(request.timeTo.getMinutes());
            to.setSeconds(59, 999);

            delete request.timeTo;
            request.dateTo = to.getTime();

            return request;
        };


        // ********************
        // Scope initialization
        // ********************
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $scope.dateFormat = localization.localize('format.date.plugin.devicelocations.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.devicelocations.createTime');
        $scope.datePickerOptions = {'show-weeks': false};
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
        };

        var deviceId = $stateParams.deviceId;
        $scope.exportClicked = function () {
            clearMessages();
            $scope.loading = true;
            $scope.successMessage = localization.localize('plugin.devicelocations.exporting');

            var exportRequest = prepareRequestData();
            exportRequest.locale = localization.getLocale();

            pluginDeviceLocationsExportService.exportLocationsHistory(exportRequest, function (data) {
                $scope.loading = false;
                clearMessages();

                var downloadableBlob = URL.createObjectURL(data.response);

                var link = document.createElement('a');
                link.href = downloadableBlob;
                link.download = $scope.deviceNumber + '.csv';

                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }, function (response) {
                $scope.loading = false;
                clearMessages();
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };

        $scope.searchClicked = function () {
            clearMessages();

            const request = prepareRequestData();

            if (request.dateFrom > request.dateTo) {
                $scope.errorMessage = localization.localize('error.plugin.devicelocations.date.range.invalid');
                return;
            }

            $scope.loading = true;

            map.removeAllMarkers();
            map.removeAllPolylines();

            pluginDeviceLocationsService.getDeviceLocationHistoryPreview(request, function(response) {
                $scope.loading = false;
                if (response.status === 'OK') {
                    const chunksCount = response.data.pageCount;
                    if (chunksCount > 0) {
                        const requests = [];
                        for (var i = 0; i < chunksCount; i++) {
                            const chunkRequest = angular.copy(request, {});
                            chunkRequest.pageNum = (i + 1);

                            requests.push(loadHistoryRecords(chunkRequest));
                        }

                        Promise.all(requests)
                            .then(function () {
                                $scope.loading = false;
                                map.fitBoundsToMarkers();
                            })
                            .catch(function () {
                                $scope.loading = false;
                                map.fitBoundsToMarkers();
                                $scope.errorMessage = localization.localize('error.request.failure');
                            });
                    } else {
                        $scope.errorMessage = localization.localize("error.plugin.devicelocations.no.history");
                    }
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.loading = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            });
        };

        /**
         * Gets the chunk of history records data from server and adds respective markers to map.
         *
         * @param request a request for chunk of history records to be sent to server.
         * @returns {Promise<unknown>} a promise for retrieving a chunk of history data from server.
         */
        const loadHistoryRecords = function (request) {
            const promise = new Promise(function (resolve, reject) {
                pluginDeviceLocationsService.getDeviceLocationHistory(request, function(response) {
                    $scope.loading = false;

                    if (response.status === 'OK') {
                        const points = response.data;
                        const dateFormatter = $filter('date');
                        const dateFormat = localization.localize('plugin.devicelocations.history.format.date');

                        var pathPoints = [];

                        points.forEach(function (point) {
                            const markerId = 'device-location-' + deviceId + '-' + point.ts;
                            if (!map.isMarkerDisplayed(markerId)) {
                                if ($scope.options.showMarkers) {
                                    const marker = map.addMarker(markerId, point.lat, point.lon, defaultMarkerIconConfig);
                                    const timestamp = dateFormatter(new Date(point.ts), dateFormat);
                                    if ($scope.options.showLabels) {
                                        marker.bindTooltip(timestamp, {permanent: true, sticky: true});
                                    }
                                }
                            }
                            if ($scope.options.showPath) {
                                pathPoints.push([point.lat, point.lon]);
                            }
                        });
                        if ($scope.options.showPath) {
                            map.addPolyline('path-' + deviceId, pathPoints, {color: 'blue'});
                        }

                        resolve();
                    } else {
                        reject();
                    }
                }, function () {
                    reject();
                });
            });

            return promise;
        };

        $scope.openDateCalendar = function( $event, isStartDate ) {
            $event.preventDefault();
            $event.stopPropagation();

            if ( isStartDate ) {
                $scope.openDatePickers.dateFrom = true;
            } else {
                $scope.openDatePickers.dateTo = true;
            }
        };

        const now = new Date();
        $scope.formData = {
            dateFrom: new Date(now.getTime() - 24 * 3600 * 1000),
            dateTo: new Date(now.getTime()),
            timeFrom: new Date(now.getTime() - 24 * 3600 * 1000),
            timeTo: new Date(now.getTime()),
            deviceId: deviceId
        };

        $scope.options = {
            showMarkers: true,
            showLabels: false,
            showPath: true
        };

        // **************
        // Initialization
        // **************
        pluginDeviceLocationsService.getSettings(function (settingsResponse) {
            if (settingsResponse.status === 'OK') {
                map.initMap($scope,"map", settingsResponse.data.tileServerUrl);
            } else {
                $scope.errorMessage = localization.localizeServerResponse(settingsResponse);
            }
        }, function () {
            $scope.errorMessage = localization.localize("error.request.failure");
        });

        pluginDeviceLocationsService.getDevicesLatestLocations({ids: [deviceId]}, function (response) {
            if (response.status === 'OK') {
                const n = response.data.length;
                if (n > 0) {
                    const device = response.data[0].device;
                    const location = response.data[0].location;

                    $scope.deviceNumber = device.number;

                    $scope.formData = {
                        dateFrom: new Date(location.ts - 24 * 3600 * 1000),
                        dateTo: new Date(location.ts),
                        timeFrom: new Date(location.ts - 24 * 3600 * 1000),
                        timeTo: new Date(location.ts),
                        deviceId: deviceId
                    };
                }
            } else {
                $scope.errorMessage = localization.localizeServerResponse(response);
            }
        }, function () {
            $scope.errorMessage = localization.localize("error.request.failure");
        });
    })
    .run(function ($rootScope, $location, externalLibLoader, localization) {
        $rootScope.$on('plugin-devicelocations-device-selected', function (event, device) {
            $location.url('/plugin-devicelocations/' + device.number);
        });

        const loadLibrary = function (libId) {
            if (externalLibLoader.isLibrarySupported(libId)) {
                var loader = externalLibLoader.getLoader(libId);
                return loader.load();
            } else {
                console.error("The following external library is not supported: ", libId);
                return new Promise(function (resolve, reject) {
                    reject();
                })
            }
        };

        // Load Leaflet libraries required by map
        loadLibrary("leaflet");
            // .then(function () {
            //     loadLibrary("leaflet.markercluster")
            //         .then(function () {
            //             loadLibrary("leaflet.markercluster.layersupport")
            //         })
            // });

        localization.loadPluginResourceBundles("devicelocations");

    })
;



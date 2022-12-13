// Localization completed
angular.module('plugin-photo', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-photo', {
                url: "/" + 'plugin-photo',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.photo.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-photo'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-photo', e);
        }

        try {
            $stateProvider.state('plugin-settings-photo', {
                url: "/" + 'plugin-settings-photo',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.photo.settings" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-photo'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-photo', e);
        }

        try {
            $stateProvider.state('plugin-photo-places', {
                url: "/" + 'plugin-photo-places',
                templateUrl: 'app/components/plugins/photo/views/places.html',
                controller: 'PluginPhotoPlacesController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.photo.places" | localize}}', //label to show in breadcrumbs
                    parent: 'plugin-settings-photo'
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-photo', e);
        }

    })
    .factory('pluginPhotoService', function ($resource) {
        return $resource('', {}, {
            getSettings: {url: 'rest/plugins/photo/photo-plugin-settings/private', method: 'GET'},
            saveSettings: {url: 'rest/plugins/photo/photo-plugin-settings/private', method: 'PUT'},
            getPhotos: {url: 'rest/plugins/photo/photo/private/search', method: 'POST'},
            lookupDevices: {url: 'rest/plugins/photo/photo/private/device/search', method: 'POST'},
            deletePhoto: {url: 'rest/plugins/photo/photo/private/:id', method: 'DELETE'},
            parsePlacesFile: {url: 'rest/plugins/photo/place/private/parsePlacesFile', method: 'POST'},
            importPlaces: {url: 'rest/plugins/photo/place/private/importPlaces', method: 'POST'},
            searchPlaces: {url: 'rest/plugins/photo/place/private/search', method: 'POST'},
            cancelFile: {url: 'rest/plugins/photo/place/private/cancelPlacesFile', method: 'POST'},
        });
    })
    .controller('PluginPhotoTabController', function ($scope, $rootScope, pluginPhotoService, confirmModal, authService,
                                                      $location, $http, $modal, $window, $interval, localization) {

        $scope.hasPermission = authService.hasPermission;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.settings = {
            featurePlacesEnabled: false
        };
        
        pluginPhotoService.getSettings(function (response) {
            if (response.status === 'OK') {
                $scope.settings = response.data;
            } else {
                $scope.settings = {
                    featurePlacesEnabled: false
                };
                console.error("Failed to load Photo plugin settings", localization.localize('error.internal.server'));
            }
        });


        // Gets the info on the device parsed from the JSON-string taken from "info" attribute of the device
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

        $scope.getLocation = function(val) {
            return $http.post('rest/plugins/photo/photo/private/device/search', {
                deviceFilter: val,
                pageSize: 10
            }).then(function(response){
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

        $scope.deviceLookupFormatter = function (v) {
            if (v) {
                var pos = v.indexOf('/');
                if (pos > -1) {
                    return v.substr(0, pos);
                }
            }
            return v;
        };

        $scope.getPoints = function(val) {
            return $http.post('rest/plugins/photo/photo/private/place/search', {
                pointFilter: val,
                pageSize: 10
            }).then(function(response){
                if (response.data.status === 'OK') {
                    return response.data.data;
                } else {
                    return [];
                }
            });
        };

        $scope.pointLookupFormatter = function (v) {
            return v;
        };

        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            totalItems: 0,
            deviceFilter: '',
            addressFilter: '',
            pointFilter: null,
            dateFrom: null,
            dateTo: null,
            sortValue: 'createTime'
        };

        $scope.$watch('paging.pageNum', function() {
            $window.scrollTo(0, 0);
        });

        let deviceNumber = ($location.search()).deviceNumber;
        if (deviceNumber) {
            $scope.paging.deviceFilter = deviceNumber;
        }

        $scope.dateFormat = localization.localize('format.date.plugin.photo.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.photo.createTime');
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
        };

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.openDateCalendar = function( $event, isStartDate ) {
            $event.preventDefault();
            $event.stopPropagation();

            if ( isStartDate ) {
                $scope.openDatePickers.dateFrom = true;
            } else {
                $scope.openDatePickers.dateTo = true;
            }
        };

        $scope.removePhoto = function (photo) {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
            confirmModal.getUserConfirmation( localization.localize('question.plugin.photo.delete.photo'), function() {
                pluginPhotoService.deletePhoto({id: photo.id}, function (response) {
                    if (response.status === 'OK') {
                        loadData();
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                });
            } );
        };

        $scope.sortByCreateTime = function() {
            $scope.paging.sortValue = 'createTime';
            $scope.paging.pageNum = 1;

            loadData();
        };

        $scope.sortByDeviceNumber = function() {
            $scope.paging.sortValue = 'deviceNumber';
            $scope.paging.pageNum = 1;

            loadData();
        };

        $scope.search = function () {
            $scope.errorMessage = undefined;

            if ($scope.paging.dateFrom && $scope.paging.dateTo) {
                if ($scope.paging.dateFrom > $scope.paging.dateTo) {
                    $scope.errorMessage = localization.localize('error.plugin.photo.date.range.invalid');
                    return;
                }
            }

            $scope.paging.pageNum = 1;
            loadData();
        };

        $scope.$watch('paging.pageNum', function () {
            loadData();
        });

        $scope.showPhoto = function (photo) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/photo/views/view.modal.html',
                controller: 'PluginPhotoViewPhotoController',
                size: 'lg',
                resolve: {
                    photo: function () {
                        return photo;
                    }
                }
            });

            modalInstance.result.then(function () {
            }, function () {
                console.log('Modal dismissed at: ' + new Date());
            });
        };

        var loading = false;
        var loadData = function () {
            if (loading) {
                console.log("Skipping to query for list of photos since a previous request is pending");
                return;
            }

            loading = true;

            var request = {};
            for (var p in $scope.paging) {
                if ($scope.paging.hasOwnProperty(p)) {
                    request[p] = $scope.paging[p];
                }
            }

            request.deviceFilter = $scope.deviceLookupFormatter(request.deviceFilter);

            pluginPhotoService.getPhotos(request, function (response) {
                loading = false;
                if (response.status === 'OK') {
                    $scope.photos = response.data.items;
                    $scope.paging.totalItems = response.data.totalItemsCount;
                } else {
                    console.error("Failed to retrieve the list of photos", response.message);
                }
            }, function () {
                loading = false;
            })
        };

        loadData();

        $scope.interval = $interval( loadData, 20000 );
        $scope.$on( '$destroy', function() {
            if ( $scope.interval ) $interval.cancel( $scope.interval );
        } );
    })
    .controller('PluginPhotoSettingsController', function ($scope, $rootScope, pluginPhotoService, localization, $state) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;
        $scope.importModeOn = false;

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.settings = {};

        pluginPhotoService.getSettings(function (response) {
            if (response.status === 'OK') {
                $scope.settings = response.data;
                if ($scope.settings.transparency === null || $scope.settings.transparency === undefined) {
                    $scope.settings.transparency = 50;
                }
            } else {
                $scope.errorMessage = localization.localize('error.internal.server');
            }
        });

        $scope.save = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;

            pluginPhotoService.saveSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.plugin.photo.settings.saved');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.importPlaces = function () {
            $scope.importModeOn = true;
        };

        $scope.viewPlaces = function () {
            $state.transitionTo('plugin-photo-places');
        };

        $scope.skipImportPlaces = function () {
            $scope.importModeOn = false;
        };

    })
    .controller('PluginPhotoViewPhotoController', function ($scope, photo, $modalInstance) {
        $scope.photo = photo;
        
        $scope.cancel = function () {
            $modalInstance.close();
        }
    })
    .controller('PluginPhotoPlaceImportController', function ($scope, localization, pluginPhotoService, alertService) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $scope.goMainScreen = function () {
            $scope.$parent.skipImportPlaces();
        };

        $scope.step = 1;

        var columnOptions = [];
        for (var i = 1; i < 27; i++) {
            columnOptions.push({
                id: i,
                name: localization.localize('plugin.photo.column.' + i)
            });
        }
        $scope.columnOptions = columnOptions;

        var clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        $scope.onStartedUpload = function (files) {
            clearMessages();

            $scope.invalidFile = false;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.fileName = files[0].name;
                if ($scope.fileName.endsWith(".xls") || $scope.fileName.endsWith(".xlsx")) {
                    $scope.loading = true;
                    $scope.successMessage = localization.localize('success.uploading.file');
                } else {
                    $scope.errorMessage = localization.localize('plugin.photo.error.xls.file.required');
                    $scope.invalidFile = true;
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
                $scope.errorMessage = localization.localize('plugin.photo.error.xls.file.required');
            }
        };

        $scope.clearFile = function () {
            clearMessages();

            $scope.file = {};
            $scope.fileSelected = false;
            $scope.invalidFile = false;
            $scope.loading = false;

            pluginPhotoService.cancelFile({filePathId: $scope.request.filePathId});

            $scope.request.filePathId = null;
        };

        $scope.doPreview = function () {
            clearMessages();

            if (!$scope.fileSelected || $scope.invalidFile) {
                $scope.errorMessage = localization.localize('plugin.photo.error.file.required');
            } else {
                $scope.loading = true;
                $scope.successMessage = localization.localize('plugin.photo.parsing');

                pluginPhotoService.parsePlacesFile($scope.request, function (response) {
                    $scope.loading = false;
                    clearMessages();

                    if (response.status === 'OK') {
                        var statusOk = localization.localize('plugin.photo.device.status.ok');
                        var statusExists = localization.localize('plugin.photo.device.status.exists');
                        var statusDuplicate = localization.localize('plugin.photo.device.status.repeating');

                        response.data.places.forEach(function (item) {
                            if (item.existingPlaceId) {
                                item.status = statusExists;
                            } else {
                                item.status = statusOk;
                            }
                            if (item.count > 0) {
                                item.status = statusDuplicate;
                            }
                        });
                        $scope.importRequest.uuid = response.data.uuid;
                        $scope.previewResults = {
                            "newRecordsCount": response.data.newRecordsCount,
                            "badRecordsCount": response.data.badRecordsCount,
                            "estimatedDBRecordsCount": response.data.estimatedDBRecordsCount,
                            "existingRecordsCount": response.data.existingRecordsCount,
                            "duplicateRecordsCount": response.data.duplicateRecordsCount
                        };
                        $scope.places = response.data.places;
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
            $scope.successMessage = localization.localize('plugin.photo.importing');

            pluginPhotoService.importPlaces($scope.importRequest, function (response) {
                $scope.loading = false;
                clearMessages();

                if (response.status === 'OK') {
                    $scope.importResult = response.data;
                    $scope.step = 3;
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

        $scope.toStep1 = function () {
            $scope.step = 1;
        };


        var initScope = function () {

            $scope.file = {};
            $scope.loading = false;
            $scope.fileName = null;
            $scope.invalidFile = false;
            $scope.fileSelected = false;
            
            $scope.previewResults = {};

            $scope.request = {
                placeIdColumnIndex: 1,
                latColumnIndex: 2,
                lngColumnIndex: 3,
                addressColumnIndex: 4,
                filePathId: null
            };
            $scope.importRequest = {
                existingMode: 2,
                deleteExisting: false,
                uuid: null
            };
            $scope.importResult = {};

            $scope.step = 1;
        };

        initScope();

    })
    .controller('PluginPhotoPlacesController', function ($scope, $window, pluginPhotoService, localization) {
        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            totalItems: 0,
            filter: ''
        };

        $scope.errorMessage = undefined;

        var loading = false;
        var loadData = function () {
            if (loading) {
                return;
            }
            loading = true;

            $scope.errorMessage = undefined;

            pluginPhotoService.searchPlaces($scope.paging, function (response) {
                loading = false;
                if (response.status === 'OK') {
                    $scope.places = response.data.items;
                    $scope.paging.totalItems = response.data.totalItemsCount;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function (response) {
                loading = false;
                console.error("Error when sending request to server", response);
            });
        };

        $scope.search = function () {
            $scope.errorMessage = undefined;
            $scope.paging.pageNum = 1;
            
            loadData();
        };

        $scope.$watch('paging.pageNum', function() {
            $window.scrollTo(0, 0);
            loadData();
        });

        loadData();

    })
    .run(function ($rootScope, $location, localization) {
        $rootScope.$on('plugin-photo-device-selected', function (event, device) {
            $location.url('/plugin-photo?deviceNumber=' + device.number);
        });

        localization.loadPluginResourceBundles("photo");
    });



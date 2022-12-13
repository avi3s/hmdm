// Localization completed
angular.module('headwind-kiosk')
    .factory('deviceService', function ($resource) {
        return $resource('', {}, {
            getAllDevices: {url: 'rest/private/devices/search', method: 'POST'},
            updateDevice: {url: 'rest/private/devices', method: 'PUT'},
            updateDeviceDesc: {url: 'rest/private/devices/:id/description', method: 'POST'},
            removeDevice: {url: 'rest/private/devices/:id', method: 'DELETE'},
            removeDeviceBulk: {url: 'rest/private/devices/deleteBulk', method: 'POST'},
            getDeviceApplicationSettings: {url: 'rest/private/devices/:id/applicationSettings', method: 'GET'},
            saveDeviceApplicationSettings: {url: 'rest/private/devices/:id/applicationSettings', method: 'POST'},
            notifyDeviceOnAppSettingsUpdate: {url: 'rest/private/devices/:id/applicationSettings/notify', method: 'POST'}
        });
    })
    .factory('configurationService', function ($resource) {
        return $resource('', {}, {
            getAllConfigurations: {url: 'rest/private/configurations/search/:value', method: 'GET'},
            getById: {url: 'rest/private/configurations/:id', method: 'GET'},
            getAllTypicalConfigurations: {url: 'rest/private/configurations/typical/search/:value', method: 'GET'},
            updateConfiguration: {url: 'rest/private/configurations', method: 'PUT'},
            copyConfiguration: {url: 'rest/private/configurations/copy', method: 'PUT'},
            upgradeConfigurationApplication: {url: 'rest/private/configurations/:id/application/:appId/upgrade', method: 'PUT'},
            removeConfiguration: {url: 'rest/private/configurations/:id', method: 'DELETE'},
            getApplications: {url: 'rest/private/configurations/applications/:id', method: 'GET'},
        });
    })
    .factory('customerService', function ($resource) {
        return $resource('', {}, {
            getAllCustomers: {url: 'rest/private/customers/search', method: 'POST'},
            getForUpdate: {url: 'rest/private/customers/:id/edit', method: 'GET'},
            updateCustomer: {url: 'rest/private/customers', method: 'PUT'},
            removeCustomer: {url: 'rest/private/customers/:id', method: 'DELETE'},
            loginAs: {url: 'rest/private/customers/impersonate/:id', method: 'GET'},
            isUsedPrefix: {url: 'rest/private/customers/prefix/:prefix/used', method: 'GET'},
        });
    })
    .factory('iconService', function ($resource) {
        return $resource('', {}, {
            getAllIcons: {url: 'rest/private/icons', method: 'GET'},
            createIcon: {url: 'rest/private/icons', method: 'PUT'},
        });
    })
    .factory('settingsService', function ($resource) {
        return $resource('', {}, {
            getSettings: {url: 'rest/private/settings', method: 'GET'},
            getUserRoleSettings: {url: 'rest/private/settings/userRole/:roleId', method: 'GET'},
            updateDefaultDesignSettings: {url: 'rest/private/settings/design', method: 'POST'},
            updateUserRolesCommonSettings: {url: 'rest/private/settings/userRoles/common', method: 'POST'},
            updateMiscSettings: {url: 'rest/private/settings/misc', method: 'POST'},
            updateLanguageSettings: {url: 'rest/private/settings/lang', method: 'POST'},
        })
    })
    .factory('passwordResetService', function ($resource) {
        return $resource('', {}, {
            getSettings: {url: 'rest/public/passwordReset/settings/:token', method: 'GET'},
            resetPassword: {url: 'rest/public/passwordReset/reset', method: 'POST'},
            canRecover: {url: 'rest/public/passwordReset/canRecover', method: 'GET'},
            recoverPassword: {url: 'rest/public/passwordReset/recover/:username', method: 'GET'}
        })
    })
    .factory('groupService', function ($resource) {
        return $resource('', {}, {
            getAllGroups: {url: 'rest/private/groups/search/:value', method: 'GET'},
            getGroup: {url: 'rest/private/groups/:id', method: 'GET'},
            updateGroup: {url: 'rest/private/groups', method: 'PUT'},
            removeGroup: {url: 'rest/private/groups/:id', method: 'DELETE'}
        })
    })
    .factory('pluginService', function ($resource) {
        return $resource('', {}, {
            getAvailablePlugins: {url: 'rest/plugin/main/private/available', method: 'GET'},
            getRegisteredPlugins: {url: 'rest/plugin/main/public/registered', method: 'GET'},
            getActivePlugins: {url: 'rest/plugin/main/private/active', method: 'GET'},
            disablePlugins: {url: 'rest/plugin/main/private/disabled', method: 'POST'},
        })
    })
    .factory('applicationService', function ($resource) {
        return $resource('', {}, {
            getAllApplications: {url: 'rest/private/applications/search/:value', method: 'GET'},
            getApplicationVersions: {url: 'rest/private/applications/:id/versions', method: 'GET'},
            getAllAdminApplications: {url: 'rest/private/applications/admin/search/:value', method: 'GET'},
            getApplication: {url: 'rest/private/applications/:id', method: 'GET'},
            updateApplication: {url: 'rest/private/applications/android', method: 'PUT'},
            updateWebApplication: {url: 'rest/private/applications/web', method: 'PUT'},
            validateApplicationPackage: {url: 'rest/private/applications/validatePkg', method: 'PUT'},
            updateApplicationVersion: {url: 'rest/private/applications/versions', method: 'PUT'},
            removeApplication: {url: 'rest/private/applications/:id', method: 'DELETE'},
            removeApplicationVersion: {url: 'rest/private/applications/versions/:id', method: 'DELETE'},
            turnIntoCommonApplication: {url: 'rest/private/applications/admin/common/:id', method: 'GET'},
            getConfigurations: {url: 'rest/private/applications/configurations/:id', method: 'GET'},
            getVersionConfigurations: {url: 'rest/private/applications/version/:id/configurations', method: 'GET'},
            updateApplicationConfigurations: {url: 'rest/private/applications/configurations', method: 'POST'},
            updateApplicationVersionConfigurations: {url: 'rest/private/applications/version/configurations', method: 'POST'}
        })
    })
    .factory('fileService', function ($resource) {
        return $resource('', {}, {
            getAllFiles: {url: 'rest/private/web-ui-files/search/:value', method: 'GET'},
            getApps: {url: 'rest/private/web-ui-files/apps/:value', method: 'GET'},
            removeFile: {url: 'rest/private/web-ui-files/remove', method: 'POST'},
            moveFile: {url: 'rest/private/web-ui-files/move', method: 'POST'}
        })
    })
    .factory('appVersionComparisonService', function () {

        var mdmAppVersionComparisonIndex = function (versionText) {
            if (versionText === null || versionText === undefined || versionText.trim().length === 0) {
                return "-1000000";
            }

            var result = '';
            var parts = versionText.split('.');
            var N = parts.length;
            for (var i = 0; i < N; i++) {
                var part = parts[i].replace(/[^0-9]+/g, '');

                if (part.trim().length === 0) {
                    part = '0';
                }

                while (part.length < 10) {
                    part = '0' + part;
                }

                result += part;
            }

            return result;
        };

        return {
            compare: function (v1, v2) {
                var i1 = mdmAppVersionComparisonIndex(v1);
                var i2 = mdmAppVersionComparisonIndex(v2);

                if (i1 === i2) {
                    return 0;
                } else if (i1 < i2) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    })
;

angular.module('headwind-kiosk')
    .controller('RedlistController', function ($scope, $rootScope, $state, $modal, $interval, $cookies, $window, $filter, $timeout,
                                                 confirmModal, deviceService, groupService, settingsService, hintService,
                                                 authService, pluginService, configurationService, alertService,
                                                 spinnerService, localization, dashboardService) {

        var dtSettings = {
            "retrieve": true,
            'paginate': true,
            'searchDelay': 750,
            "bDeferRender": true,
            "responsive": true,
            "autoWidth": false,
            "pageLength":25,
            dom: "<'row'><'row'<'col-md-7'lB><'col-md-5'f>>rt<'row'<'col-md-4'i>><'row'<'#colvis'><'.dt-page-jump'>p>",
            buttons: ['excel', 'pdf','csv','print']
        };
        var table;
        angular.element(document).ready(function () {
            // table = $('.dt-table').DataTable(dtSettings);
        });

        $scope.dashboardData = {};
        $scope.getDashboardData = function (){
            spinnerService.show('spinner2');
            dashboardService.getRKBListData(function (response) {
                spinnerService.close('spinner2');
                if (response.data) {
                    $scope.dashboardData = response.data;
                    setTimeout(function (){
                        table = $('.dt-table').DataTable(dtSettings);
                    })
                }
            });
        };
        $scope.init = function () {
            $scope.getDashboardData();
        };
        $scope.init();
    });


angular.module('headwind-kiosk')
    .controller('DashboardController', function ($scope, $rootScope, $state, $modal, $interval, $cookies, $window, $filter, $timeout,
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
            $('.selectpicker').selectpicker();
        });


        $scope.dashboardData = {};
        $scope.report_months = "today";
        $scope.startDate = '';
        $scope.endDate = '';
        $scope.getDashboardData = function (report_months){
            $scope.dashboardData = {};
            if(table)
            table.destroy();
            console.log(report_months);
            var date = new Date();
            if(report_months=='today'){
              //  date = date.setDate(date.getDate() - 1);
            }else if(report_months=='this_week'){
                    // var d = new Date();
                    // var day = d.getDay(),
                    //     diff = d.getDate() - day + (day == 0 ? -6:1); // adjust when day is sunday
                    // date =  new Date(d.setDate(diff));
                date = date.setDate(date.getDate() - 7);
            }else if(report_months=='this_month'){
                var dm = new Date();
                date = new Date(dm.getFullYear(), dm.getMonth(), 1);
            }else if(report_months=='3'){
                var d3 = new Date();
               date =  d3.setMonth(d3.getMonth() - 3);
            }else if(report_months=='6'){
                var d6 = new Date();
                date =  d6.setMonth(d6.getMonth() - 6);
            }else {

            }

            var startDate = new Date(date).toJSON().slice(0,10).replace(/-/g,'-');
            var endDate = new Date().toJSON().slice(0,10).replace(/-/g,'-');;
            $scope.startDate = startDate;
            $scope.endDate = endDate;
            var request = {
                startDate: startDate+" 00:00:00.000000",
                endDate: endDate+" 00:00:00.000000",
                districtId: authService.getDistrictId() ? authService.getDistrictId() :''
            };
            spinnerService.show('spinner2');
            dashboardService.getDashboardData(request,function (response) {
                spinnerService.close('spinner2');
                console.log(response)
                if (response.data) {

                    $scope.dashboardData = response.data
                    if($scope.dashboardData.dashboardDetails.length===1 && !authService.isJavaUser()){
                        window.location.href = '#/mandal/'+ $scope.dashboardData.dashboardDetails[0].districtId;
                    }
                    setTimeout(function (){
                         table = $('.dt-table').DataTable(dtSettings);

                    })

                }
            });
        };
        $scope.init = function () {
            $scope.getDashboardData('today');
        };
        $scope.init();
    });

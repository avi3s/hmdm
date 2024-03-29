angular.module('headwind-kiosk')
    .controller('ReportsController', function ($scope, $rootScope, $state,$location, $modal, $interval, $cookies, $window, $filter, $timeout,
                                                 confirmModal, deviceService, groupService, settingsService, hintService,
                                                 authService, pluginService, configurationService, alertService,
                                                 spinnerService, localization, dashboardService) {


       // console.log($location.search());
        var districtId = $location.search().districtId ? $location.search().districtId:'';
        var report_months = $location.search().report_months ? $location.search().report_months:'';
        var status = $location.search().status ? $location.search().status:'';
        $scope.dateFrom = '';
        $scope.dateTo = '';
        $scope.startDate = '';
        $scope.endDate = '';
        $scope.formatDate = function (date) {
            var d = new Date(date),
                month = '' + (d.getMonth() + 1),
                day = '' + d.getDate(),
                year = d.getFullYear();

            if (month.length < 2)
                month = '0' + month;
            if (day.length < 2)
                day = '0' + day;

            return [year, month, day].join('-');
        };
        if(report_months) {
            var date = new Date();
            if(report_months=='today'){
                //  date = new Date(date.setDate(date.getDate() - 1));
            }else if(report_months=='this_week'){
                var d = new Date();
                var day = d.getDay(),
                    diff = d.getDate() - day + (day == 0 ? -6:1); // adjust when day is sunday
                date =  new Date(d.setDate(diff));
            }else if(report_months=='this_month'){
                var dm = new Date();
                date = new Date(dm.getFullYear(), dm.getMonth(), 1);
            }else if(report_months=='3'){
                var d3 = new Date();
                date =  new Date(d3.setMonth(d3.getMonth() - 3));
            }else if(report_months=='6'){
                var d6 = new Date();
                date =  new Date(d6.setMonth(d6.getMonth() - 6));
            }else {

            }
            $scope.startDate = $scope.formatDate(date);
            $scope.endDate = $scope.formatDate((new Date()));
           // $scope.dateFrom = date;
           // $scope.dateTo = new Date();
        }

        $scope.dateFormat = "yyyy-MM-dd"
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
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
        $scope.districtId = [];
        if(districtId){
            $scope.districtId.push(districtId);
        }
        $scope.mandal = '';
        $scope.status = [];
        if(status){
            $scope.status.push(status);
        }
        var dtSettings = {
            "retrieve": true,
            'paginate': true,
            'searchDelay': 750,
            "bDeferRender": true,
            "responsive": true,
            "autoWidth": false,
            "pageLength":25,
            dom: "<'row'><'row'<'col-md-7'lB><'col-md-5'f>>rt<'row'<'col-md-4'i>><'row'<'#colvis'><'.dt-page-jump'>p>",
            buttons: [
                {
                    extend: 'collection',
                    text: 'Download',
                    buttons: [
                        'excel','csv','print',
                        {
                            extend: 'pdfHtml5',
                            orientation: 'landscape',
                            pageSize: 'LEGAL'
                        }
                    ]
                }]
        };
        var table;
        angular.element(document).ready(function () {
            $scope.searchReport();
            // $('#mandal').selectpicker();

            if($scope.districtId.length > 0){
                $scope.getMandalList($scope.districtId);
            }
            $('.selectpicker').selectpicker();

        });
        $scope.districtList = [];
        $scope.mandalList = [];
        $scope.statusList = [];
        $scope.dashboardData = {};

        $scope.getDataFunCallInProgress = false;
        $scope.getDashboardData = function () {
            $scope.getDataFunCallInProgress = true;
            $scope.dashboardData = {};
            if(table)
                table.destroy();
            var startDate = '';
            var endDate = '';
            if(report_months && ($scope.dateFrom==='' || $scope.dateTo==='')){
                startDate = $scope.startDate;
                endDate = $scope.endDate;
            }else {
                if ($scope.dateFrom) {
                    startDate = $scope.formatDate($scope.dateFrom);
                    $scope.startDate = startDate;
                    $scope.dateFrom = startDate;
                }
                if ($scope.dateTo) {
                    endDate = $scope.formatDate($scope.dateTo);
                    $scope.endDate = endDate;
                    $scope.dateTo = endDate;
                }
            }

            var request = {
                startDate: startDate? startDate+" 00:00:00.000000":'',
                endDate: endDate? endDate+" 00:00:00.000000":'',
                districtId: $scope.districtId.length > 0 ? $scope.districtId.toString() :'',
                mandalName: $scope.mandal,
                kioskStatus: $scope.status.length > 0 ? $scope.status.toString() :'',
            };
            spinnerService.show('spinner2');
            dashboardService.getReportData(request,function (response) {
                spinnerService.close('spinner2');
                console.log(response)
                if (response.data) {
                    $scope.dashboardData = response.data;
                    setTimeout(function (){
                        table = $('.dt-table').DataTable(dtSettings);
                        $scope.getDataFunCallInProgress = false;
                    },1000)

                }else {
                    $scope.dashboardData = {};
                    setTimeout(function (){
                        table = $('.dt-table').DataTable(dtSettings);
                    },1000)

                }
            });
        };
        $scope.getDistrictList = function () {
            dashboardService.getDistrictList(function (response) {
                if (response.data) {
                    $scope.districtList = response.data;
                    setTimeout(function () {
                        $('#districtId').selectpicker('refresh');
                    },100)
                }
            });

        }
        $scope.getMandalList = function (districtId) {
            dashboardService.getMandalList({districtId:districtId},function (response) {
                if (response.data) {
                    $scope.mandalList = response.data;
                    setTimeout(function () {
                        $('#mandal').selectpicker('refresh');
                    },100)
                }
            });
        }
        $scope.getStatusList = function () {
            dashboardService.getStatusList(function (response) {
                if (response.data) {
                    $scope.statusList = response.data;
                    setTimeout(function () {
                        $('#status').selectpicker('refresh');
                    },100)
                }
            });
        }
        $scope.searchReport = function (){
            $scope.getDashboardData();
        };
        $scope.clear = function () {
            localStorage.setItem('report_months','');
            window.location.href = '#/reports';
            window.location.reload();
        }
        $scope.districtChange = function (){
           // console.log($scope.districtId);
            $scope.mandalList = [];
            $scope.mandal = '';
            if($scope.districtId.length > 0){
                $scope.getMandalList($scope.districtId);
            }
        }
        $scope.rkbDetails = function (rkbId) {
            console.log('rkbId',rkbId);
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/rkbdetails.html',
                controller: 'RKBDetailsModalController',
                windowClass: 'rkb-modal-window',
                resolve: {
                    rkbId: function () {
                        return rkbId;
                    },
                }
            });
            modalInstance.result.then(function () {

            });
        };
        $scope.init = function () {
            $scope.getStatusList();
            $scope.getDistrictList();
        };
        $scope.init();


    }).controller('RKBDetailsModalController',
    function ($scope, $modalInstance, rkbId, dashboardService, spinnerService) {
        console.log('rkbId',rkbId);
        $scope.rkbDetails = {};
        if(rkbId){
            spinnerService.show('spinner2');
            dashboardService.getRKBDetails({rkbId:rkbId},function (response) {
                spinnerService.close('spinner2');
                if (response.data) {
                    $scope.rkbDetails = response.data;
                }
            });
        }
        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };
    });


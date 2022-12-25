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

package com.hmdm.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.admin.*;
import com.hmdm.persistence.mapper.AdminMapper;
import com.hmdm.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class AdminDAO {

    private static final Logger logger = LoggerFactory.getLogger(AdminDAO.class);

    private final AdminMapper adminMapper;
    /**
     * <p>Constructs new <code>AdminDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public AdminDAO(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public Dashboard getDashboard(Input input) {

        validateRequest(input);
        Dashboard result = new Dashboard();
        AtomicReference<Long> totalKioskCount = new AtomicReference<>(0L);
        AtomicReference<Long> functionalCount = new AtomicReference<>(0L);
        AtomicReference<Long> onlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> offlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> nonfunctionalCount = new AtomicReference<>(0L);
        List<DashboardDetails> dashboardList = new CopyOnWriteArrayList<>();
        if (StringUtil.isEmpty(input.getDistrictId())) {
            List<DistrictDetails> districtList = getDistrictLists();
            districtList.forEach(dl -> {
                input.setDistrictId(dl.getId());
                input.setDistrictName(dl.getDistrictName());
                dashboardCalculation(input, totalKioskCount, functionalCount, onlineCount, offlineCount, nonfunctionalCount, dashboardList);
            });
        } else {
            input.setDistrictName(adminMapper.getDistrictById(Integer.valueOf(input.getDistrictId())).getDistrictName());
            dashboardCalculation(input, totalKioskCount, functionalCount, onlineCount, offlineCount, nonfunctionalCount, dashboardList);
        }
        result.setDashboardDetails(dashboardList);
        result.setTotalKioskCount(String.valueOf(totalKioskCount));
        result.setFunctionalCount(String.valueOf(functionalCount));
        result.setOnlineCount(String.valueOf(onlineCount));
        result.setOfflineCount(String.valueOf(offlineCount));
        result.setNonfunctionalCount(String.valueOf(nonfunctionalCount));

        return result;
    }

    private void validateRequest(Input input) {

        if (Objects.isNull(input)) {
                throw new ValidationException("Invalid Input");
        } else {
            if (StringUtil.isEmpty(input.getStartDate())) {
                throw new ValidationException("Please provide StartDate");
            }

            if (StringUtil.isEmpty(input.getEndDate())) {
                throw new ValidationException("Please provide EndDate");
            }
            checkDateDifference(input);

            if (!StringUtil.isEmpty(input.getDistrictId())) {
                try {
                    Integer.valueOf(input.getDistrictId());
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid DistrictId");
                }
            }
        }
    }

    private void checkDateDifference(Input input) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = sdf.parse(input.getStartDate());
            Date d2 = sdf.parse(input.getEndDate());
            if (d2.getTime() < d1.getTime()) {
                throw new ValidationException("EndDate Must Be Greater Than StartDate");
            }
        } catch (ParseException e) {
            throw new ValidationException("Invalid Date");
        }
    }

    private void dashboardCalculation(Input input, AtomicReference<Long> totalKioskCount, AtomicReference<Long> functionalCount, AtomicReference<Long> onlineCount, AtomicReference<Long> offlineCount, AtomicReference<Long> nonfunctionalCount, List<DashboardDetails> dashboardList) {

        DashboardDetails dashboard = getDashBoardReportByDistrict(input);

        // Calculation Of the Table Values
        Long functional = Long.valueOf(dashboard.getOnline()) + Long.valueOf(dashboard.getOffline());
        dashboard.setFunctional(String.valueOf(functional));
        Long nonFunctional = Long.valueOf(dashboard.getInstalled()) - Long.valueOf(dashboard.getFunctional());
        dashboard.setNonFunctional(String.valueOf(nonFunctional));
        Double functionality = Double.valueOf(adminMapper.getTotalKiosks()) / Double.valueOf(dashboard.getInstalled());
        dashboard.setFunctionality(String.valueOf(functionality));

        // Calculation Of the Header Values
        totalKioskCount.set(totalKioskCount.get() + Long.valueOf(dashboard.getInstalled()));
        functionalCount.set(functionalCount.get() + functional);
        onlineCount.set(onlineCount.get() + Long.valueOf(dashboard.getOnline()));
        offlineCount.set(offlineCount.get() + Long.valueOf(dashboard.getOffline()));
        nonfunctionalCount.set(nonfunctionalCount.get() + nonFunctional);
        dashboardList.add(dashboard);
    }

    private DashboardDetails getDashBoardReportByDistrict(Input input) {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        DashboardDetails dashboard = new DashboardDetails();
        dashboard.setDistrictId(input.getDistrictId());
        dashboard.setDistrictName(input.getDistrictName());

        Callable<DashboardDetails> getTotalInstalled = () -> adminMapper.getTotalInstalled(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()));
        Future<DashboardDetails> totalInstalled = executorService.submit(getTotalInstalled);
        try {
            if (Objects.nonNull(totalInstalled.get())) {
                dashboard.setInstalled(totalInstalled.get().getInstalled());
            } else {
                dashboard.setInstalled("0");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Processing Getting Total installed Records");
        }

        Callable<DashboardDetails> getTotalOnline = () -> adminMapper.getTotalOnline(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()));
        Future<DashboardDetails> totalOnline = executorService.submit(getTotalOnline);
        try {
            if (Objects.nonNull(totalOnline.get())) {
                dashboard.setOnline(totalOnline.get().getOnline());
            } else {
                dashboard.setOnline("0");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Processing Getting Online Records");
        }

        Callable<DashboardDetails> getTotalOffline = () -> adminMapper.getTotalOffline(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()));
        Future<DashboardDetails> totalOffline = executorService.submit(getTotalOffline);
        try {
            if (Objects.nonNull(totalOffline.get())) {
                dashboard.setOffline(totalOffline.get().getOffline());
            } else {
                dashboard.setOffline("0");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Processing Getting Offline Records");
        }
        return dashboard;
    }

    public MandalPage getMandalDetailsLists(Input input) {

        validateMandalRequest(input);
        MandalPage result = new MandalPage();
        AtomicReference<Long> totalKioskCount = new AtomicReference<>(0L);
        AtomicReference<Long> functionalCount = new AtomicReference<>(0L);
        AtomicReference<Long> onlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> offlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> nonfunctionalCount = new AtomicReference<>(0L);
        List<Mandal> mandalList = new CopyOnWriteArrayList<>();
        if (StringUtil.isEmpty(input.getMandalName())) {
            List<MandalDetails> mandals = getMandalLists(input.getDistrictId());
            mandals.forEach(dl -> {
                input.setMandalId(dl.getId());
                input.setMandalName(dl.getMandalName());
                mandalCalculation(input, totalKioskCount, functionalCount, onlineCount, offlineCount, nonfunctionalCount, mandalList);
            });
        } else {
            input.setDistrictName(adminMapper.getDistrictById(Integer.valueOf(input.getDistrictId())).getDistrictName());
            mandalCalculation(input, totalKioskCount, functionalCount, onlineCount, offlineCount, nonfunctionalCount, mandalList);
        }
        result.setMandals(mandalList);
        result.setTotalKioskCount(String.valueOf(totalKioskCount));
        result.setFunctionalCount(String.valueOf(functionalCount));
        result.setOnlineCount(String.valueOf(onlineCount));
        result.setOfflineCount(String.valueOf(offlineCount));
        result.setNonfunctionalCount(String.valueOf(nonfunctionalCount));

        return result;
    }

    private void mandalCalculation(Input input, AtomicReference<Long> totalKioskCount, AtomicReference<Long> functionalCount, AtomicReference<Long> onlineCount, AtomicReference<Long> offlineCount, AtomicReference<Long> nonfunctionalCount, List<Mandal> mandalList) {

        Mandal mandal = getMandalReportByDistrict(input);

        // Calculation Of the Table Values
        Long functional = Long.valueOf(mandal.getOnline()) + Long.valueOf(mandal.getOffline());
        mandal.setFunctional(String.valueOf(functional));
        Long nonFunctional = Long.valueOf(mandal.getInstalled()) - Long.valueOf(mandal.getFunctional());
        mandal.setNonFunctional(String.valueOf(nonFunctional));
        Double functionality = Double.valueOf(adminMapper.getTotalMandals(Integer.valueOf(input.getDistrictId()))) / Double.valueOf(mandal.getInstalled());
        mandal.setFunctionality(String.valueOf(functionality));

        // Calculation Of the Header Values
        totalKioskCount.set(totalKioskCount.get() + Long.valueOf(mandal.getInstalled()));
        functionalCount.set(functionalCount.get() + functional);
        onlineCount.set(onlineCount.get() + Long.valueOf(mandal.getOnline()));
        offlineCount.set(offlineCount.get() + Long.valueOf(mandal.getOffline()));
        nonfunctionalCount.set(nonfunctionalCount.get() + nonFunctional);
        mandalList.add(mandal);
    }

    private Mandal getMandalReportByDistrict(Input input) {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Mandal mandal = new Mandal();
        mandal.setMandalName(input.getMandalName());

        Callable<Mandal> getTotalInstalled = () -> adminMapper.getTotalMandalInstalled(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()), input.getMandalName());
        Future<Mandal> totalInstalled = executorService.submit(getTotalInstalled);
        try {
            if (Objects.nonNull(totalInstalled.get())) {
                mandal.setInstalled(totalInstalled.get().getInstalled());
            } else {
                mandal.setInstalled("0");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Processing Getting Total installed Records");
        }

        Callable<Mandal> getTotalOnline = () -> adminMapper.getTotalMandalOnline(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()), input.getMandalName());
        Future<Mandal> totalOnline = executorService.submit(getTotalOnline);
        try {
            if (Objects.nonNull(totalOnline.get())) {
                mandal.setOnline(totalOnline.get().getOnline());
            } else {
                mandal.setOnline("0");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Processing Getting Online Records");
        }

        Callable<Mandal> getTotalOffline = () -> adminMapper.getTotalMandalOffline(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()), input.getMandalName());
        Future<Mandal> totalOffline = executorService.submit(getTotalOffline);
        try {
            if (Objects.nonNull(totalOffline.get())) {
                mandal.setOffline(totalOffline.get().getOffline());
            } else {
                mandal.setOffline("0");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException("Error Processing Getting Offline Records");
        }
        return mandal;
    }

    private void validateMandalRequest(Input input) {

        if (Objects.isNull(input)) {
            throw new ValidationException("Invalid Input");
        } else {
            if (StringUtil.isEmpty(input.getStartDate())) {
                throw new ValidationException("Please provide StartDate");
            }

            if (StringUtil.isEmpty(input.getEndDate())) {
                throw new ValidationException("Please provide EndDate");
            }
            checkDateDifference(input);

            if (StringUtil.isEmpty(input.getDistrictId())) {
                throw new ValidationException("Please Provide DistrictId");
            } else {
                try {
                    Integer.valueOf(input.getDistrictId());
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid DistrictId");
                }
            }
        }
    }

    public List<RBK> getRBKDetailsLists(String mandalId) {
        return null;
    }

    public List<Report> getReports() {
        return null;
    }

    public List<RedList> getRedLists() {
        return null;
    }

    public RBKDetails getRBKDetails(String rbkId) {
        return null;
    }

    public List<DistrictDetails> getDistrictLists() {

        return adminMapper.getDistrictLists();
    }

    public List<MandalDetails> getMandalLists(String districtId) {

        try {
            return adminMapper.getMandalLists(Integer.valueOf(districtId));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid DistrictId");
        }
    }

    public List<Kiosk> getKioskStatus() {
        return adminMapper.getKioskStatus();
    }
}
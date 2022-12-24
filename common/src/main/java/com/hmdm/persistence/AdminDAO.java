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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        Dashboard result = new Dashboard();
        AtomicReference<Long> totalKioskCount = new AtomicReference<>(0L);
        AtomicReference<Long> functionalCount = new AtomicReference<>(0L);
        AtomicReference<Long> onlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> offlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> nonfunctionalCount = new AtomicReference<>(0L);
        List<DashboardDetails> dashboardList = new CopyOnWriteArrayList<>();
        List<DistrictDetails> districtList = getDistrictLists();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        districtList.forEach(dl -> {
            DashboardDetails dashboard = new DashboardDetails();
            dashboard.setDistrictId(dl.getId());
            dashboard.setDistrictName(dl.getDistrictName());

            Callable<DashboardDetails> getTotalInstalled = () -> {
                return adminMapper.getTotalInstalled(input.getStartDate(), input.getEndDate(), Integer.valueOf(dl.getId()));
            };
            Future<DashboardDetails> totalInstalled = executorService.submit(getTotalInstalled);
            try {
                if (Objects.nonNull(totalInstalled.get())) {
                    dashboard.setInstalled(totalInstalled.get().getInstalled());
                } else {
                    dashboard.setInstalled("0");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            Callable<DashboardDetails> getTotalOnline = () -> {
                return adminMapper.getTotalOnline(input.getStartDate(), input.getEndDate(), Integer.valueOf(dl.getId()));
            };
            Future<DashboardDetails> totalOnline = executorService.submit(getTotalOnline);
            try {
                if (Objects.nonNull(totalOnline.get())) {
                    dashboard.setOnline(totalOnline.get().getOnline());
                } else {
                    dashboard.setOnline("0");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            Callable<DashboardDetails> getTotalOffline = () -> {
                return adminMapper.getTotalOffline(input.getStartDate(), input.getEndDate(), Integer.valueOf(dl.getId()));
            };
            Future<DashboardDetails> totalOffline = executorService.submit(getTotalOffline);
            try {
                if (Objects.nonNull(totalOffline.get())) {
                    dashboard.setOffline(totalOffline.get().getOffline());
                } else {
                    dashboard.setOffline("0");
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            Long functional = Long.valueOf(dashboard.getOnline()) + Long.valueOf(dashboard.getOffline());
            dashboard.setFunctional(String.valueOf(functional));
            Long nonFunctional = Long.valueOf(dashboard.getInstalled()) - Long.valueOf(dashboard.getFunctional());
            dashboard.setNonFunctional(String.valueOf(nonFunctional));
            Double functionality = 9483.00 / Double.valueOf(dashboard.getInstalled());
            dashboard.setFunctionality(String.valueOf(functionality));

            totalKioskCount.set(totalKioskCount.get() + Long.valueOf(dashboard.getInstalled()));
            functionalCount.set(functionalCount.get() + functional);
            onlineCount.set(onlineCount.get() + Long.valueOf(dashboard.getOnline()));
            offlineCount.set(offlineCount.get() + Long.valueOf(dashboard.getOffline()));
            nonfunctionalCount.set(nonfunctionalCount.get() + nonFunctional);
            dashboardList.add(dashboard);
        });
        result.setDashboardDetails(dashboardList);
        result.setTotalKioskCount(String.valueOf(totalKioskCount));
        result.setFunctionalCount(String.valueOf(functionalCount));
        result.setOnlineCount(String.valueOf(onlineCount));
        result.setOfflineCount(String.valueOf(offlineCount));
        result.setNonfunctionalCount(String.valueOf(nonfunctionalCount));

        return result;
    }

    public List<Mandal> getMandalDetailsLists(String districtId) {
        return null;
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

        int id = Integer.valueOf(districtId);
        return adminMapper.getMandalLists(id);
    }

    public List<Kiosk> getKioskStatus() {
        return adminMapper.getKioskStatus();
    }
}

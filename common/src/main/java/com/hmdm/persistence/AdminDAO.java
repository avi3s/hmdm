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
import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.admin.*;
import com.hmdm.persistence.mapper.AdminMapper;
import com.hmdm.rest.json.UserCredentials;
import com.hmdm.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ValidationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    public User login(UserCredentials credentials) {

        User user = null;
        StaffUser staffUser = adminMapper.login(credentials.getLogin());
        if (Objects.nonNull(staffUser)) {
            user = new User();
            UserRole userRole = new UserRole();
            user.setUserRole(userRole);
            user.setId(Integer.parseInt(staffUser.getStaffId()));
            user.setEmail(credentials.getLogin());
            user.setPassword(staffUser.getPassword());
            user.setAuthToken(staffUser.getAuthToken());
            user.setName(staffUser.getFirstname() + " " + staffUser.getLastname());
            if (staffUser.getAdmin().equalsIgnoreCase("1")) {
                user.setDistrictId(null);
            } else {
                try {
                    String districtId = adminMapper.fetchStaffDistrict(Integer.parseInt(staffUser.getStaffId()));
                    if (!StringUtil.isEmpty(districtId)) {
                        user.setDistrictId(districtId);
                    } else {
                        user.setDistrictId(null);
                    }
                } catch(Exception e) {
                    user.setDistrictId(null);
                }
            }
        }
        return user;
    }

    public void setToken(User user) {
        adminMapper.setToken(user.getAuthToken(), user.getId());
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
                if (!dl.getId().equalsIgnoreCase("27")) { // Skipping for "districtName": "RBK ID Mismatch"
                    input.setDistrictId(dl.getId());
                    input.setDistrictName(dl.getDistrictName());
                    dashboardCalculation(input, totalKioskCount, functionalCount, onlineCount, offlineCount, nonfunctionalCount, dashboardList);
                }
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
            Calendar c = Calendar.getInstance();
            c.setTime(d2);
            c.add(Calendar.DATE, 1);
            d2 = c.getTime();
            if (d2.getTime() < d1.getTime()) {
                throw new ValidationException("EndDate Must Be Greater Than StartDate");
            } else {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDate = dateFormat.format(d2);
                input.setEndDate(strDate);
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
        Double functionality = 0.0D;
        if (!dashboard.getInstalled().equalsIgnoreCase("0")) {
            functionality = Double.valueOf(functional) / Double.valueOf(dashboard.getInstalled()) * 100;
            functionality = (double) Math.round(functionality * 100) / 100;
        }
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

        Callable<DashboardDetails> getTotalInstalled = () -> adminMapper.getTotalInstalled(Integer.valueOf(input.getDistrictId()));
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
        DistrictDetails districtDetails = adminMapper.getDistrictById(Integer.valueOf(input.getDistrictId()));
        result.setDistrictName(districtDetails.getDistrictName());

        return result;
    }

    private void mandalCalculation(Input input, AtomicReference<Long> totalKioskCount, AtomicReference<Long> functionalCount, AtomicReference<Long> onlineCount, AtomicReference<Long> offlineCount, AtomicReference<Long> nonfunctionalCount, List<Mandal> mandalList) {

        Mandal mandal = getMandalReportByDistrict(input);

        // Calculation Of the Table Values
        Long functional = Long.valueOf(mandal.getOnline()) + Long.valueOf(mandal.getOffline());
        mandal.setFunctional(String.valueOf(functional));
        Long nonFunctional = Long.valueOf(mandal.getInstalled()) - Long.valueOf(mandal.getFunctional());
        mandal.setNonFunctional(String.valueOf(nonFunctional));
        Double functionality = 0.0D;
        if (!mandal.getInstalled().equalsIgnoreCase("0")) {
            functionality = Double.valueOf(functional) / Double.valueOf(mandal.getInstalled()) * 100;
            functionality = (double) Math.round(functionality * 100) / 100;
        }
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

        Callable<Mandal> getTotalInstalled = () -> adminMapper.getTotalMandalInstalled(Integer.valueOf(input.getDistrictId()), input.getMandalName());
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

    public RBKPage getRBKDetailsLists(Input input) {

        List<Kiosk> kiosks = getKioskStatus();
        validateRBKRequest(input);
        AtomicReference<Long> functionalCount = new AtomicReference<>(0L);
        AtomicReference<Long> onlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> offlineCount = new AtomicReference<>(0L);
        AtomicReference<Long> nonfunctionalCount = new AtomicReference<>(0L);
        List<RBK> rbkList = adminMapper.getRBKList(input.getStartDate(), input.getEndDate(), Integer.valueOf(input.getDistrictId()), input.getMandalName());
        rbkList.forEach(rbk -> {
            if (Integer.valueOf(rbk.getStatus()) == 1) {
                filteredKiosk(kiosks, rbk);
                rbk.setStatus("Online");
                rbk.setNonFunctional("No");
                onlineCount.set(onlineCount.get() + 1);
            } else if (Integer.valueOf(rbk.getStatus()) == 5) {
                filteredKiosk(kiosks, rbk);
                rbk.setStatus("Offline");
                rbk.setNonFunctional("Yes");
                offlineCount.set(offlineCount.get() + 1);
            }
        });
        RBKPage rbkPage = new RBKPage();
        rbkPage.setRbks(rbkList);
        rbkPage.setTotalKioskCount(String.valueOf(rbkList.size()));
        rbkPage.setOnlineCount(String.valueOf(onlineCount.get()));
        rbkPage.setOfflineCount(String.valueOf(offlineCount.get()));
        rbkPage.setFunctionalCount(String.valueOf(onlineCount.get() + offlineCount.get()));
        rbkPage.setNonfunctionalCount(String.valueOf(rbkList.size() -  Long.valueOf(rbkPage.getFunctionalCount())));

        return rbkPage;
    }

    private void validateRBKRequest(Input input) {

        validateMandalRequest(input);
        if (StringUtil.isEmpty(input.getMandalName())) {
            throw new ValidationException("Please Provide Mandal Name");
        }
    }

    public List<Report> getReports(Input input) {

        List<Kiosk> kiosks = getKioskStatus();
        List<DistrictDetails> districtDetails = getDistrictLists();
        List<Report> reports = adminMapper.getReport().parallelStream()
                .filter(report -> {
                    if (StringUtil.isEmpty(input.getMandalName())) {
                        return true;
                    } else {
                        return report.getMandalName().equalsIgnoreCase(input.getMandalName());
                    }
                })
                .filter(report -> {
                    if (StringUtil.isEmpty(input.getKioskStatus())) {
                        return true;
                    } else {
                        String statusArray[] = input.getKioskStatus().split(",");
                        if (Arrays.stream(statusArray).anyMatch(Predicate.isEqual("7"))) {
                            return true;
                        } else {
                            return Arrays.stream(statusArray).anyMatch(Predicate.isEqual(report.getStatus()));
                        }
                    }
                })
                .filter(report -> {
                    if (StringUtil.isEmpty(report.getLastContact())) {
                        return true;
                    }
                    if (StringUtil.isEmpty(input.getStartDate()) && StringUtil.isEmpty(input.getEndDate())) {
                        return true;
                    } else {
                        try {
                            checkDateDifference(input);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Date d1 = sdf.parse(input.getStartDate());
                            Date d2 = sdf.parse(input.getEndDate());
                            Date lastContact = sdf.parse(report.getLastContact());
                            if (lastContact.after(d1) && lastContact.before(d2)) {
                                return true;
                            } else {
                                return false;
                            }
                        } catch (Exception e) {
                            logger.error("Date Checking Exception For Lead id {} ==>> and LastContact Date {} ==>> ",report.getId(),report.getLastContact());
                            return false;
                        }
                    }
                })
                .collect(Collectors.toList());

        reports.parallelStream().forEach(r -> {
            if (Integer.valueOf(r.getStatus()) == 1) {
                filteredKiosk(kiosks, r);
                r.setStatus("Online");
            } else if (Integer.valueOf(r.getStatus()) == 5) {
                filteredKiosk(kiosks, r);
                r.setStatus("Offline");
            }
            List<DistrictDetails> filteredDistricts = districtDetails.stream().filter(district -> {
                if (district.getId().equalsIgnoreCase(r.getDistrictName())) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
            r.setDistrictName(filteredDistricts.get(0).getDistrictName());

            if (Integer.valueOf(r.getNetworkType()) == 1) {
                r.setNetworkType("Wireless");
            } else if (Integer.valueOf(r.getNetworkType()) == 2) {
                r.setNetworkType("LAN");
            }
        });
        return reports;
    }

    private void filteredKiosk(List<Kiosk> kiosks, Report r) {
        List<Kiosk> filteredKiosks = kiosks.stream().filter(kiosk -> {
            if (kiosk.getId().equalsIgnoreCase(r.getStatus())) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        r.setStatusColour(filteredKiosks.get(0).getColor());
    }

    private void filteredKiosk(List<Kiosk> kiosks, RBK r) {
        List<Kiosk> filteredKiosks = kiosks.stream().filter(kiosk -> {
            if (kiosk.getId().equalsIgnoreCase(r.getStatus())) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        r.setStatusColour(filteredKiosks.get(0).getColor());
    }

    public List<RedList> getRedLists() {

        List<RedList> redLists = adminMapper.getRedList();
        redLists.forEach(r -> {
            if (Integer.valueOf(r.getStatus()) == 1) {
                r.setStatus("Online");
            } else if (Integer.valueOf(r.getStatus()) == 5) {
                r.setStatus("Offline");
            }
            DistrictDetails districtDetails = adminMapper.getDistrictById(Integer.valueOf(r.getDistrictName()));
            r.setDistrictName(districtDetails.getDistrictName());
            r.setUnderMaintenance("No");
        });
        return redLists;
    }

    public RBKDetails getRBKDetails(String rbkId) {

        try {
            RBKDetails rbkDetails = adminMapper.getRBKDetails(Integer.valueOf(rbkId));
            if (Integer.valueOf(rbkDetails.getKioskStatus()) == 1) {
                rbkDetails.setKioskStatus("Online");
            } else if (Integer.valueOf(rbkDetails.getKioskStatus()) == 5) {
                rbkDetails.setKioskStatus("Offline");
            }
            DistrictDetails districtDetails = adminMapper.getDistrictById(Integer.valueOf(rbkDetails.getDistrictName()));
            rbkDetails.setDistrictName(districtDetails.getDistrictName());
            calculateMonthDiff(rbkDetails);
            calculateMinDiff(rbkDetails);
            return rbkDetails;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid RBKId");
        }
    }

    private void calculateMonthDiff(RBKDetails rbkDetails) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = sdf.parse(rbkDetails.getCreated());
            Date date = new Date();
            Date d2 = sdf.parse(sdf.format(date));
            rbkDetails.setCreated(checkTimeRange(d1, d2));
        } catch (ParseException e) {
            throw new ValidationException("Invalid Date");
        }
    }

    private void calculateMinDiff(RBKDetails rbkDetails) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = sdf.parse(rbkDetails.getLastContact());
            Date date = new Date();
            Date d2 = sdf.parse(sdf.format(date));
            rbkDetails.setLastContact(checkTimeRange(d1, d2));
        } catch (ParseException e) {
            throw new ValidationException("Invalid Date");
        }
    }

    private String checkTimeRange(Date d1, Date d2) {

        String result = null;
        long difference_In_Time = d2.getTime() - d1.getTime();
        long difference_In_Years = TimeUnit.MILLISECONDS.toDays(difference_In_Time) / 365l;
        if (difference_In_Years > 1) {
            result = difference_In_Years + " years ago";
            return result;
        }

        long difference_In_Days = TimeUnit.MILLISECONDS.toDays(difference_In_Time) % 365;
        if (difference_In_Days > 30) {
            result = difference_In_Days / 30 +" months ago";
            return result;
        } else if (difference_In_Days > 1) {
            result = difference_In_Days +" days ago";
            return result;
        }

        long difference_In_Hours = TimeUnit.MILLISECONDS.toHours(difference_In_Time) % 24;
        if (difference_In_Hours > 24) {
            result = difference_In_Hours +" hours ago";
            return result;
        }

        long difference_In_Minutes = (difference_In_Time/ (1000 * 60)) % 60;
        if (difference_In_Minutes > 60) {
            result = difference_In_Minutes +" minutes ago";
            return result;
        }

        long difference_In_Seconds = TimeUnit.MILLISECONDS.toSeconds(difference_In_Time) % 60;
        result = difference_In_Seconds +" seconds ago";
        return result;
    }

    public List<DistrictDetails> getDistrictLists() {

        return adminMapper.getDistrictLists();
    }

    public List<MandalDetails> getMandalLists(String districtId) {

        try {
            List<MandalDetails> mandalDetails = new ArrayList<>();
            String districtArray[] = districtId.split(",");
            for (String id:districtArray) {
                mandalDetails.addAll(adminMapper.getMandalLists(Integer.valueOf(id)));
            }
            return mandalDetails;
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid DistrictId");
        }
    }

    public List<Kiosk> getKioskStatus() {

        List<Kiosk> kiosks = new ArrayList<>();
        for (Kiosk ks : adminMapper.getKioskStatus()) {
            Kiosk kiosk = new Kiosk();
            if(!ks.getId().equalsIgnoreCase("8")) {
                if (ks.getId().equalsIgnoreCase("6")) {
                    kiosk.setId("1,5");
                } else {
                    kiosk.setId(ks.getId());
                }

                kiosk.setName(ks.getName());
                kiosk.setStatusOrder(ks.getStatusOrder());
                kiosk.setColor(ks.getColor());
                kiosks.add(kiosk);
            }
        }
        return kiosks;
    }

    public String updateDisplayStatus(Input input) {

        adminMapper.updateDisplayStatus(input.getKioskId());
        return "Updated";
    }

    public String updateNetworkStatus(Input input) {

        adminMapper.updateNetworkStatus(input.getKioskId());
        return "Updated";
    }
}
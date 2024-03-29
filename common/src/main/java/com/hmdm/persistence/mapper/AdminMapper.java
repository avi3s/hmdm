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

package com.hmdm.persistence.mapper;

import com.hmdm.persistence.domain.admin.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AdminMapper {

    @Select("select id, name as rbkId,city as rbkName,company as vaa,phonenumber as contact,status,country as districtName, state as mandalName, zip as secretariatCode, source as networkType, " +
            "lastcontact as lastContact, phase as phase FROM tblleads where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') " +
            "and state in (CASE WHEN #{state} = '' THEN state ELSE #{state} END) and phase in (CASE WHEN #{phase} = '' THEN phase ELSE #{phase} END) order by lastContact desc")
    List<Report> getReport1(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("state") String state, @Param("phase") String phase);

    @Select("select id, name as rbkId,city as rbkName,company as vaa,phonenumber as contact,status," +
            "country as districtName, state as mandalName, zip as secretariatCode, source as networkType, lastcontact as lastContact, phase as phase FROM tblleads order by lastContact desc")
    List<Report> getReport();

    @Select("select id, name as rbkLoginId,city as rbkName,company as vaa,phonenumber as contact,status," +
            "country as districtName, state as mandalName FROM tblleads where lastcontact = '2021-07-29 08:45:05' order by status asc")
    List<RedList> getRedList();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* RBK Queries Start */
    @Select("select count(*) FROM tblleads where country = #{districtId} and state = #{state}")
    Long getTotalRBKs(@Param("districtId") int districtId, @Param("state") String state);

    @Select("select count(*) FROM tblleads where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} and status = 1")
    Long getOnlineRBKList(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    @Select("select count(*) FROM tblleads where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} and status = 5")
    Long getOfflineRBKList(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    @Select("select id, name as rbkLoginId,city as rbkName,company as vaa,phonenumber as contact,status,last_status_change as lastAccessed FROM tblleads where country = #{districtId} and state = #{state} order by state")
    List<RBK> getRBKList(@Param("districtId") int districtId, @Param("state") String state);

    @Select("select id, name as rbkLoginId, email as emailAddress, phonenumber as phone, company as vaaName, city as rbkName, state as mandalName, country  as districtName, " +
            "zip as secretariatCode, status as kioskStatus, source as networkType, dateadded as created, lastcontact as lastContact FROM tblleads " +
            "where id = #{id}")
    RBKDetails getRBKDetails(@Param("id") int id);
    /* RBK Queries End */
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Mandal Queries Start */
    @Select("select count(*) FROM tblleads where country = #{districtId}")
    Long getTotalMandals(@Param("districtId") int districtId);

    @Select("select count(*) as installed,state as mandalName FROM tblleads where country = #{districtId} and state = #{state} group by state order by state")
    Mandal getTotalMandalInstalled(@Param("districtId") int districtId, @Param("state") String state);

    @Select("select count(*) as online,state as mandalName FROM tblleads  where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} and status = 1 group by state order by state")
    Mandal getTotalMandalOnline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    @Select("select count(*) as offline,state as mandalName FROM tblleads  where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} and status = 5 group by state order by state")
    Mandal getTotalMandalOffline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    /* Mandal Queries End */
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /* DashBoard Queries Start */
    @Select("select count(*) FROM tblleads")
    Long getTotalKiosks();

    @Select("select count(*) as installed,country as districtId FROM tblleads where country = #{districtId} group by country order by country")
    DashboardDetails getTotalInstalled( @Param("districtId") int districtId);

    @Select("select count(*) as online,country as districtId FROM tblleads  where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and status = 1 group by country order by country")
    DashboardDetails getTotalOnline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId);

    @Select("select count(*) as offline,country as districtId FROM tblleads  where lastcontact > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and lastcontact < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and status = 5 group by country order by country")
    DashboardDetails getTotalOffline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId);

    /* DashBoard Queries End */
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Select("SELECT district_id as id,district_name as districtName FROM tbldistrict order by district_id")
    List<DistrictDetails> getDistrictLists();

    @Select("SELECT district_id as id,district_name as districtName FROM tbldistrict where district_id = #{districtId}")
    DistrictDetails getDistrictById(@Param("districtId") int districtId);

    @Select("select distinct (state) as mandalName FROM tblleads where country = #{districtId} group by state, country")
    List<MandalDetails> getMandalLists(@Param("districtId") int districtId);

    @Select("SELECT id,name,statusorder,color FROM tblleads_status order by statusorder")
    List<Kiosk> getKioskStatus();

    @Update({"UPDATE tblleads SET lastcontact=TO_TIMESTAMP(#{lastContact},'yyyy-MM-dd HH24:MI:SS') WHERE name=#{name}"})
    void updateLastContact(@Param("lastContact") String lastContact, @Param("name") String name);

    @Select("SELECT staff_id as staffId, email,firstname,lastname,phonenumber,password,last_login as lastLogin,admin,two_factor_auth_code as authToken FROM tblstaff where active = 1 and email = #{email}")
    StaffUser login(@Param("email") String email);

    @Select("SELECT district_id FROM tblstaff_district where staff_id = #{staffId}")
    String fetchStaffDistrict(@Param("staffId") int staffId);

    @Update({"UPDATE tblstaff SET two_factor_auth_code=#{authToken} WHERE staff_id=#{id}"})
    void setToken(@Param("authToken") String authToken, @Param("id") int id);

    @Select("select id, name as rbkLoginId, lastcontact as lastContact, device_id as uniqueValue FROM tblleads")
    List<LeadsDetails> getAllLeads();

    @Select("select id, name as rbkLoginId, lastcontact as lastContact FROM tblleads where name = #{name}")
    RBKDetails getLeadByDeviceNumber(String name);
}
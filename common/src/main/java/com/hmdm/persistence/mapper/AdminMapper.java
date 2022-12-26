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

    @Select("select id, name as rbkId,city as rbkName,company as vaa,phonenumber as contact,status," +
            "country as districtName, state as mandalName, zip as secretariatCode, source as networkType, last_status_change as lastContact from tblleads order by lastContact")
    List<Report> getReport();

    @Select("select id, name as rbkLoginId,city as rbkName,company as vaa,phonenumber as contact,status," +
            "country as districtName, state as mandalName from tblleads where lastcontact = '2021-07-29 08:45:05' order by status asc")
    List<RedList> getRedList();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* RBK Queries Start */
    @Select("select count(*) from tblleads where country = #{districtId} and state = #{state}")
    Long getTotalRBKs(@Param("districtId") int districtId, @Param("state") String state);

    @Select("select id, name as rbkLoginId,city as rbkName,company as vaa,phonenumber as contact,status,last_status_change as lastAccessed from tblleads where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} order by state")
    List<RBK> getRBKList(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    @Select("select id, name as rbkLoginId, email as emailAddress, phonenumber as phone, company as vaaName, city as rbkName, state as mandalName, country  as districtName, " +
            "zip as secretariatCode, status as kioskStatus, source as networkType, dateadded as created, lastcontact as lastContact from tblleads " +
            "where id = #{id}")
    RBKDetails getRBKDetails(@Param("id") int id);
    /* RBK Queries End */
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* Mandal Queries Start */
    @Select("select count(*) from tblleads where country = #{districtId}")
    Long getTotalMandals(@Param("districtId") int districtId);

    @Select("select count(*) as installed,state as mandalName from tblleads where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} group by state order by state")
    Mandal getTotalMandalInstalled(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    @Select("select count(*) as online,state as mandalName from tblleads  where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} and status = 1 group by state order by state")
    Mandal getTotalMandalOnline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    @Select("select count(*) as offline,state as mandalName from tblleads  where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} and status = 5 group by state order by state")
    Mandal getTotalMandalOffline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId, @Param("state") String state);

    /* Mandal Queries End */
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /* DashBoard Queries Start */
    @Select("select count(*) from tblleads")
    Long getTotalKiosks();

    @Select("select count(*) as installed,country as districtId from tblleads where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} group by country order by country")
    DashboardDetails getTotalInstalled(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId);

    @Select("select count(*) as online,country as districtId from tblleads  where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and status = 1 group by country order by country")
    DashboardDetails getTotalOnline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId);

    @Select("select count(*) as offline,country as districtId from tblleads  where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and status = 5 group by country order by country")
    DashboardDetails getTotalOffline(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("districtId") int districtId);

    /* DashBoard Queries End */
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Select("SELECT district_id as id,district_name as districtName FROM tbldistrict order by district_id")
    List<DistrictDetails> getDistrictLists();

    @Select("SELECT district_id as id,district_name as districtName FROM tbldistrict where district_id = #{districtId}")
    DistrictDetails getDistrictById(@Param("districtId") int districtId);

    @Select("select distinct (state) as mandalName from tblleads where country = #{districtId} group by state, country")
    List<MandalDetails> getMandalLists(@Param("districtId") int districtId);

    @Select("SELECT id,name,statusorder,color FROM tblleads_status order by statusorder")
    List<Kiosk> getKioskStatus();

    @Update({"UPDATE tblleads SET lastcontact=NOW() WHERE id=#{kioskId}"})
    void updateDisplayStatus(@Param("kioskId") String kioskId);

    @Update({"UPDATE tblleads SET lastcontact=NOW() WHERE id=#{kioskId}"})
    void updateNetworkStatus(@Param("kioskId") String kioskId);
}
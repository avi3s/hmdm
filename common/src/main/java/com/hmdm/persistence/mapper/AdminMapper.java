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

import com.hmdm.persistence.domain.User;
import com.hmdm.persistence.domain.UserRole;
import com.hmdm.persistence.domain.admin.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AdminMapper {

   /* User findByLogin(@Param("login") String login);

    User findByEmail(@Param("email") String email);

    User findByPasswordResetToken(@Param("token") String token);

    User findById(@Param("userId") Integer userId);

    List<User> findAll(@Param("customerId") Integer customerId);

    List<User> findAllByFilter(@Param("customerId") Integer customerId, @Param("value") String value);

    List<User> findAllWithOldPassword();

    @Insert({"INSERT INTO users (login, email, name, password, customerId, userRoleId, " +
            "allDevicesAvailable, allConfigAvailable, passwordReset, authToken, passwordResetToken) " +
            "VALUES (#{login}, #{email}, #{name}, #{password}, #{customerId}, #{userRole.id}, " +
            "#{allDevicesAvailable}, #{allConfigAvailable}, #{passwordReset}, #{authToken}, #{passwordResetToken})"})
    @SelectKey( statement = "SELECT currval('users_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insert(User user);

    @Update({"UPDATE users " +
            "SET name = #{name}, login=#{login}, email=#{email}, userRoleId=#{userRole.id}, allDevicesAvailable=#{allDevicesAvailable}, " +
            "allConfigAvailable=#{allConfigAvailable}, passwordReset=#{passwordReset} " +
            "WHERE id=#{id}"})
    void updateUserMainDetails(User user);

    @Update({"UPDATE users SET password=#{password}, passwordReset=#{passwordReset}, " +
            "authToken=#{authToken}, passwordResetToken=#{passwordResetToken} WHERE id=#{id}"})
    void updatePassword(User user);

    @Update({"UPDATE users SET password=#{newPassword}, passwordReset=#{passwordReset}, " +
            "authToken=#{authToken}, passwordResetToken=#{passwordResetToken} WHERE id=#{id}"})
    void setNewPassword(User user);

    @Delete({"DELETE FROM users WHERE id=#{id} AND userRoleId <> 1"})
    void deleteUser(User user);

    List<UserRole> findAllUserRoles(@Param("includeSuperAdmin") boolean inludeSuperAdmin);

    @Delete({"DELETE FROM userDeviceGroupsAccess " +
            "WHERE userId=#{id} " +
            "AND groupId IN (SELECT groups.id FROM groups WHERE groups.customerId=#{customerId})"})
    void removeDeviceGroupsAccessByUserId(@Param("customerId") int customerId, @Param("id") Integer userId);

    void insertUserDeviceGroupsAccess(@Param("id") Integer userId, @Param("groups") List<Integer> groups);

    @Delete({"DELETE FROM userConfigurationAccess " +
            "WHERE userId=#{id} " +
            "AND configurationId IN (SELECT configurations.id FROM configurations WHERE configurations.customerId=#{customerId})"})
    void removeConfigurationAccessByUserId(@Param("customerId") int customerId, @Param("id") Integer userId);

    void insertUserConfigurationAccess(@Param("id") Integer userId, @Param("configurations") List<Integer> configurations);

    @Select("SELECT hintKey FROM userHints WHERE userId = #{id}")
    List<String> getShownHints(@Param("id") Integer userId);

    @Insert("INSERT INTO userHints (userId, hintKey) VALUES (#{userId}, #{hintKey})")
    int insertShownHint(@Param("userId") Integer userId, @Param("hintKey") String hintKey);

    @Delete("DELETE FROM userHints WHERE userId = #{id}")
    int clearHintsHistory(@Param("id") Integer userId);

    @Insert("INSERT INTO userHints (userId, hintKey) SELECT #{id}, hintKey FROM userHintTypes")
    int insertHintsHistoryAll(@Param("id") Integer userId);*/

    @Select("select id,name as rbkId,city as rbkName,company as vaa,phonenumber as contact,status," +
            "country as districtName, state as mandalName, zip as secretariatCode, source as networkType, last_status_change as lastContact from tblleads order by lastContact")
    List<Report> getReport();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /* RBK Queries Start */
    @Select("select count(*) from tblleads where country = #{districtId} and state = #{state}")
    Long getTotalRBKs(@Param("districtId") int districtId, @Param("state") String state);

    @Select("select id,name as rbkLoginId,city as rbkName,company as vaa,phonenumber as contact,status,last_status_change as lastAccessed from tblleads where dateadded > TO_TIMESTAMP(#{startDate},'yyyy-MM-dd HH24:00:00') and dateadded < TO_TIMESTAMP(#{endDate},'yyyy-MM-dd HH24:00:00') and country = #{districtId} and state = #{state} order by state")
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
}
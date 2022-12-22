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
import com.hmdm.persistence.domain.admin.DistrictDetails;
import com.hmdm.persistence.domain.admin.Kiosk;
import com.hmdm.persistence.domain.admin.MandalDetails;
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

    @Select("SELECT district_id as id,district_name as districtName FROM tbldistrict order by district_id")
    List<DistrictDetails> getDistrictLists();

    @Select("select distinct (state) as mandalName from tblleads where country = #{districtId} group by state, country")
    List<MandalDetails> getMandalLists(@Param("districtId") int districtId);

    @Select("SELECT id,name,statusorder,color FROM tblleads_status order by statusorder")
    List<Kiosk> getKioskStatus();
}

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

package com.hmdm.plugins.knox.persistence.mapper;

import com.hmdm.plugins.knox.persistence.domain.Rule;
import com.hmdm.plugins.knox.rest.json.RuleFilter;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>An ORM mapper for {@link Rule} domain objects.</p>
 *
 * @author isv
 */
public interface RuleMapper {


    List<Rule> findAllRules(RuleFilter filter);

    long countAll(RuleFilter filter);

    @Insert("INSERT INTO plugin_knox_rules " +
            "(configurationId, rule, tableType, ruleType) " +
            "VALUES " +
            "(#{configurationId}, #{rule}, #{tableType}, #{ruleType})"
    )
    void insertRule(Rule rule);

    @Update("UPDATE plugin_knox_rules SET configurationId=#{configurationId}, rule=#{rule}, " +
            "tableType=#{tableType}, ruleType=#{ruleType} " +
            "WHERE id=#{id}")
    void updateRule(Rule rule);

    @Select("SELECT rules.*, c.customerId FROM plugin_knox_rules rules " +
            "LEFT JOIN configurations c ON rules.configurationId=c.id " +
            " WHERE configurationId = #{configurationId}")
    List<Rule> getRulesForConfiguration(@Param("configurationId") int configurationId);

    @Delete("DELETE from plugin_knox_rules WHERE id = #{id}")
    void deleteRule(@Param("id") int id);

    @Delete("DELETE FROM plugin_knox_rules WHERE configurationId = #{configurationId}")
    void deleteRulesForConfiguration(@Param("configurationId") int configurationId);

    @Insert("INSERT INTO plugin_knox_rules " +
            "(configurationId, rule, tableType, ruleType) " +
            "SELECT #{newConfigId}, rule, tableType, ruleType FROM plugin_knox_rules " +
            "WHERE configurationId = #{configurationId}")
    void copyRuleProfile(@Param("configurationId") int configurationId, @Param("newConfigId") int newConfigId);
}

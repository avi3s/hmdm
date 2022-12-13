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

package com.hmdm.plugins.knox.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.persistence.domain.CustomerData;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A domain object representing a single record of the firewall rule</p>
 *
 * @author isv
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule implements Serializable, CustomerData {

    private static final long serialVersionUID = 2634901198550234514L;

    @ApiModelProperty("An ID of rule record")
    private Integer id;

    /**
     * <p>An ID of a customer account which the record belongs to.</p>
     */
    @ApiModelProperty("Customer ID")
    private int customerId;

    @ApiModelProperty("Configuration ID")
    private int configurationId;

    @ApiModelProperty("Rule pattern")
    private String rule;

    @ApiModelProperty("Rule table type")
    private RuleTableType tableType = RuleTableType.OUTGOING_CALL;

    @ApiModelProperty("Rule type (blacklist or whitelist)")
    private RuleType ruleType = RuleType.BLACKLIST;

    /**
     * <p>Constructs new <code>Rule</code> instance. This implementation does nothing.</p>
     */
    public Rule() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int getCustomerId() {
        return customerId;
    }

    @Override
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public RuleTableType getTableType() {
        return tableType;
    }

    public void setTableType(RuleTableType tableType) {
        this.tableType = tableType;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", configurationId=" + configurationId +
                ", rule=" + rule +
                ", tableType=" + tableType +
                ", ruleType=" + ruleType +
                '}';
    }
}

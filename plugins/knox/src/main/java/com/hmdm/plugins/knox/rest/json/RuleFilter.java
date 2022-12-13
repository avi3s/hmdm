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

package com.hmdm.plugins.knox.rest.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hmdm.plugins.knox.persistence.domain.RuleTableType;
import com.hmdm.plugins.knox.persistence.domain.RuleType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * <p>A DTO carrying the parameters for filtering the lists of rule objects.</p>
 *
 * @author isv
 */
@ApiModel(description = "The parameters for filtering the lists of rule objects")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleFilter implements Serializable {

    private static final long serialVersionUID = -2102574525702236828L;

    /**
     * <p>A number of records per single page of data to be retrieved.</p>
     */
    @ApiModelProperty("A number of records per single page of data to be retrieved")
    private int pageSize = 50;

    /**
     * <p>A number of page of data to be retrieved.</p>
     */
    @ApiModelProperty("A number of page of data to be retrieved (1-based)")
    private int pageNum = 1;

    /**
     * <p>A filter used for filtering the rules by configuration.</p>
     */
    @ApiModelProperty("A filter used for filtering the rules by configuration")
    private Integer configurationFilter;

    /**
     * <p>A filter used for filtering the rules by pattern.</p>
     */
    @ApiModelProperty("A filter used for filtering the rules by pattern")
    private String patternFilter;

    /**
     * <p>A filter used for filtering the rules by rule table.</p>
     */
    @ApiModelProperty("A filter used for filtering the rules by rule table")
    private RuleTableType tableTypeFilter;

    /**
     * <p>A filter used for filtering the rules by rule table.</p>
     */
    @ApiModelProperty("A filter used for filtering the rules by rule type")
    private RuleType ruleTypeFilter;

    /**
     * <p>An ID of a customer.</p>
     */
    @ApiModelProperty(hidden = true)
    private int customerId;

    /**
     * <p>A name of sorting column.</p>
     */
    @ApiModelProperty("A name of sorting column")
    private String sortValue = "tableType";

    /**
     * <p>A direction to sort results by.</p>
     */
    @ApiModelProperty("A direction to sort rule list")
    private String sortDir = "ASC";


    /**
     * <p>Constructs new <code>RuleFilter</code> instance. This implementation does nothing.</p>
     */
    public RuleFilter() {
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getConfigurationFilter() {
        return configurationFilter;
    }

    public void setConfigurationFilter(Integer configurationFilter) {
        this.configurationFilter = configurationFilter;
    }

    public String getPatternFilter() {
        return patternFilter;
    }

    public void setPatternFilter(String patternFilter) {
        this.patternFilter = patternFilter;
    }

    public RuleTableType getTableTypeFilter() {
        return tableTypeFilter;
    }

    public void setTableTypeFilter(RuleTableType tableTypeFilter) {
        this.tableTypeFilter = tableTypeFilter;
    }

    public RuleType getRuleTypeFilter() {
        return ruleTypeFilter;
    }

    public void setRuleTypeFilter(RuleType ruleTypeFilter) {
        this.ruleTypeFilter = ruleTypeFilter;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    @Override
    public String toString() {
        return "RuleFilter{" +
                "pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                ", configurationFilter='" + configurationFilter + '\'' +
                ", patternFilter='" + patternFilter + '\'' +
                ", tableTypeFilter='" + tableTypeFilter + '\'' +
                ", customerId=" + customerId +
                ", sortValue='" + sortValue + '\'' +
                ", sortDir='" + sortDir + '\'' +
                '}';
    }
}

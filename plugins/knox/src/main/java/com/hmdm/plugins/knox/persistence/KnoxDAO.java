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

package com.hmdm.plugins.knox.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.persistence.ConfigurationDAO;
import com.hmdm.persistence.domain.Configuration;
import com.hmdm.plugins.knox.persistence.domain.Rule;
import com.hmdm.plugins.knox.persistence.mapper.RuleMapper;
import com.hmdm.plugins.knox.rest.json.RuleFilter;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A DAO for {@link Rule} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class KnoxDAO extends AbstractDAO<Rule> {

    private static final Logger logger = LoggerFactory.getLogger(KnoxDAO.class);

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final RuleMapper ruleMapper;

    private final ConfigurationDAO configurationDAO;

    /**
     * <p>Constructs new <code>KnoxDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public KnoxDAO(RuleMapper ruleMapper,
                   ConfigurationDAO configurationDAO) {
        this.ruleMapper = ruleMapper;
        this.configurationDAO = configurationDAO;
    }

    /**
     * <p>Finds the log records matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a list of rules matching the specified filter.
     */
    @Transactional
    public List<Rule> findAll(RuleFilter filter) {
        prepareFilter(filter);

        final List<Rule> result = this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            return this.ruleMapper.findAllRules(filter);
        });

        return new ArrayList<>(result);
    }


    /**
     * <p>Counts the rules matching the specified filter.</p>
     *
     * @param filter a filter used to narrowing down the search results.
     * @return a number of rules matching the specified filter.
     */
    public long countAll(RuleFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.ruleMapper.countAll(filter);
                })
                .orElse(0L);
    }


    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(RuleFilter filter) {
        if (filter.getPatternFilter() != null) {
            if (filter.getPatternFilter().trim().isEmpty()) {
                filter.setPatternFilter(null);
            } else {
                filter.setPatternFilter('%' + filter.getPatternFilter().trim() + '%');
            }
        }
    }

    /**
     * This method is used to return the Knox rules when the device requests it
     * It doesn't have a customer filter so should not used in web app (private) REST API
     * @return
     */
    public List<Rule> getRulesForConfiguration(int configurationId) {
        return this.ruleMapper.getRulesForConfiguration(configurationId);
    }

    /**
     * Adds a new rule
     * @param rule
     */
    public void insertRule(Rule rule) {
        insertRecord(rule, this.ruleMapper::insertRule);
    }

    /**
     * Updates an existing rule
     * @param rule
     */
    public void updateRule(Rule rule) {
        updateRecord(
                rule,
                this.ruleMapper::updateRule,
                s -> SecurityException.onCustomerDataAccessViolation(s.getId(), "KnoxRule")
        );
    }

    /**
     * Deletes a rule
     * @param id
     */
    public void deleteRule(int id) {
        this.ruleMapper.deleteRule(id);
    }


    /**
     * Copies a Knox profile
     * @param configSrc Source configuration ID
     * @param configDst Destination configuration ID
     * @param removeExisting set to true if need to remove existing rules in the destination configuration
     */
    public void copyProfile(int configSrc, int configDst, boolean removeExisting) {
        if (configSrc == configDst) {
            // Nothing to do
            return;
        }

        Configuration src = configurationDAO.getConfigurationById(configSrc);
        Configuration dst = configurationDAO.getConfigurationById(configDst);
        if (src == null || dst == null) {
            logger.error("Failed to copy Knox profile: configuration {} or {} doesn't exist", configSrc, configDst);
            return;
        }
        SecurityContext.get()
                .getCurrentUser()
                .ifPresent(u -> {
                    if (src.getCustomerId() != u.getCustomerId() || dst.getCustomerId() != u.getCustomerId()) {
                        logger.error("Failed to copy Knox profile: configuration {} or {} doesn't belong to customer {}", configSrc, configDst, u.getCustomerId());
                        return;
                    }
                    if (removeExisting) {
                        this.ruleMapper.deleteRulesForConfiguration(configDst);
                    }
                    this.ruleMapper.copyRuleProfile(configSrc, configDst);
                });
    }
}

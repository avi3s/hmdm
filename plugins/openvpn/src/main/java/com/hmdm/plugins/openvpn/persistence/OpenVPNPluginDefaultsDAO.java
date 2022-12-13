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

package com.hmdm.plugins.openvpn.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.openvpn.persistence.domain.OpenVPNPluginDefaults;
import com.hmdm.plugins.openvpn.persistence.mapper.OpenVPNMapper;
import com.hmdm.security.SecurityException;

import javax.inject.Named;
import java.util.Optional;

/**
 * <p>A DAO for {@link OpenVPNPluginDefaults} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class OpenVPNPluginDefaultsDAO extends AbstractDAO<OpenVPNPluginDefaults> {

    /**
     * <p>An ORM mapper for domain object type.</p>
     */
    private final OpenVPNMapper openVPNMapper;

    /**
     * <p>Constructs new <code>OpenVPNPluginDefaultsDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public OpenVPNPluginDefaultsDAO(OpenVPNMapper openVPNMapper) {
        this.openVPNMapper = openVPNMapper;
    }

    /**
     * <p>Gets the OpenVPN default settings for the customer account associated with the current user.</p>
     *
     * @return plugin settings for current customer account or <code>null</code> if there are no such settings found.
     */
    public OpenVPNPluginDefaults getDefaultSettings() {
        OpenVPNPluginDefaults defaults = getSingleRecord(this.openVPNMapper::findSettingsByCustomerId);
        return defaults != null ? defaults : new OpenVPNPluginDefaults();
    }

    /**
     * <p>Inserts the specified plugin settings applying the current security context.</p>
     *
     * @param settings plugin settings to be inserted.
     */
    public void insertDefaultSettings(OpenVPNPluginDefaults settings) {
        insertRecord(settings, this.openVPNMapper::insertSettings);
    }

    /**
     * <p>Updates the specified plugin settings verifying them against the current security context.</p>
     *
     * @param settings plugin settings to be updated.
     */
    public void updateDefaultSettings(OpenVPNPluginDefaults settings) {
        updateRecord(
                settings,
                this.openVPNMapper::updateSettings,
                s -> SecurityException.onCustomerDataAccessViolation(s.getId(), "OpenVPNSettings")
        );
    }

    /**
     * <p>Gets the plugin settings for the specified customer account. This method intended only for usage when handling
     * the requests from the devices.</p>
     *
     * @return plugin settings for specified customer account or default settings if there are no such settings found.
     */
    public OpenVPNPluginDefaults getDefaultSettings(int customerId) {
        return Optional
                .ofNullable(this.openVPNMapper.findSettingsByCustomerId(customerId))
                .orElse(new OpenVPNPluginDefaults());
    }
}

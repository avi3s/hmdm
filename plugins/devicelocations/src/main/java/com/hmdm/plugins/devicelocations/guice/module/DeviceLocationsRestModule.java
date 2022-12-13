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

package com.hmdm.plugins.devicelocations.guice.module;

import com.google.inject.servlet.ServletModule;
import com.hmdm.plugin.rest.PluginAccessFilter;
import com.hmdm.plugins.devicelocations.rest.DeviceLocationsPluginSettingsResource;
import com.hmdm.plugins.devicelocations.rest.DeviceLocationsPublicResource;
import com.hmdm.plugins.devicelocations.rest.DeviceLocationsResource;
import com.hmdm.rest.filter.AuthFilter;
import com.hmdm.security.jwt.JWTFilter;

import java.util.Arrays;
import java.util.List;

/**
 * <p>A <code>Guice</code> module for <code>Device Locations Plugin</code> REST resources.</p>
 *
 * @author isv
 */
public class DeviceLocationsRestModule extends ServletModule {

    /**
     * <p>A list of patterns for URIs for plugin resources which prohibit anonymous access.</p>
     */
    private static final List<String> protectedResources = Arrays.asList(
            "/rest/plugins/devicelocations/devicelocations-plugin-settings/private",
            "/rest/plugins/devicelocations/devicelocations/private/*"
    );

    /**
     * <p>Constructs new <code>DeviceLocationsRestModule</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationsRestModule() {
    }

    /**
     * <p>Configures the <code>Device Locations Plugin</code> REST resources.</p>
     */
    protected void configureServlets() {
        this.filter(protectedResources).through(JWTFilter.class);
        this.filter(protectedResources).through(AuthFilter.class);
        this.filter(protectedResources).through(PluginAccessFilter.class);
        this.bind(DeviceLocationsPluginSettingsResource.class);
        this.bind(DeviceLocationsPublicResource.class);
        this.bind(DeviceLocationsResource.class);
    }

}

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

package com.hmdm.plugins.devicelocations;

import com.google.inject.Module;
import com.hmdm.plugin.PluginConfiguration;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.devicelocations.guice.module.DeviceLocationsLiquibaseModule;
import com.hmdm.plugins.devicelocations.guice.module.DeviceLocationsPersistenceModule;
import com.hmdm.plugins.devicelocations.guice.module.DeviceLocationsRestModule;
import com.hmdm.plugins.devicelocations.guice.module.DeviceLocationsTaskModule;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * <p>A configuration for <code>Device Locations</code> plugin.</p>
 *
 * @author isv
 */
public class DeviceLocationsPluginConfigurationImpl implements PluginConfiguration {

    public static final String PLUGIN_ID = "devicelocations";

    /**
     * <p>Constructs new <code>DeviceLocationsPluginConfigurationImpl</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationsPluginConfigurationImpl() {
    }

    /**
     * <p>Gets the unique identifier for this plugin. This is a sort of logical name for the plugin which is used widely
     * by <code>Plugin Platform</code>.</p>
     *
     * @return a plugin identifier.
     */
    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    /**
     * <p>Gets the root package for the classes comprising the plugin.</p>
     *
     * @return a fully-qualified name of the root package for plugin code.
     */
    @Override
    public String getRootPackage() {
        return "com.hmdm.plugins.devicelocations";
    }

    /**
     * <p>Gets the list of modules to be used for initializing the plugin.</p>
     *
     * @param context a context for plugin usage.
     * @return a list of modules to be used for plugin initialization.
     */
    @Override
    public List<Module> getPluginModules(ServletContext context) {
        List<Module> modules = new ArrayList<>();

        modules.add(new DeviceLocationsLiquibaseModule(context));
        modules.add(new DeviceLocationsPersistenceModule(context));
        modules.add(new DeviceLocationsRestModule());

        return modules;
    }

    /**
     * <p>Gets the list of task modules to be initialized upon application startup.</p>
     *
     * @param context a context for plugin usage.
     * @return an optional list of task modules for plugins.
     */
    @Override
    public Optional<List<Class<? extends PluginTaskModule>>> getTaskModules(ServletContext context) {
        List<Class<? extends PluginTaskModule>> modules = new ArrayList<>();

        modules.add(DeviceLocationsTaskModule.class);

        return Optional.of(modules);
    }

}

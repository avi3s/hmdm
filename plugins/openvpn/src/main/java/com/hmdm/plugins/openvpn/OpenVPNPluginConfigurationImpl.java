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

package com.hmdm.plugins.openvpn;

import com.google.inject.Module;
import com.hmdm.plugin.PluginConfiguration;
import com.hmdm.plugins.openvpn.guice.module.OpenVPNLiquibaseModule;
import com.hmdm.plugins.openvpn.guice.module.OpenVPNPersistenceModule;
import com.hmdm.plugins.openvpn.guice.module.OpenVPNRestModule;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>A configuration for <code>Contacts</code> plugin.</p>
 *
 * @author isv
 */
public class OpenVPNPluginConfigurationImpl implements PluginConfiguration {

    public static final String PLUGIN_ID = "openvpn";

    /**
     * <p>Constructs new <code>ContactsPluginConfigurationImpl</code> instance. This implementation does nothing.</p>
     */
    public OpenVPNPluginConfigurationImpl() {
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
        return "com.hmdm.plugins.openvpn";
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

        modules.add(new OpenVPNLiquibaseModule(context));
        modules.add(new OpenVPNPersistenceModule(context));
        modules.add(new OpenVPNRestModule());

        return modules;
    }
}

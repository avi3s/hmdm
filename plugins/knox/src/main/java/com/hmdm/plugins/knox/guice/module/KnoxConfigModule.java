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

package com.hmdm.plugins.knox.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.hmdm.rest.json.DeviceListHook;
import com.hmdm.rest.json.SyncResponseHook;

import javax.servlet.ServletContext;

/**
 * <p>A <code>Guice</code> module used for configuring the environment for <code>Knox</code> plugin.</p>
 *
 * @author isv
 */
public class KnoxConfigModule extends AbstractModule {

    private final String license = "plugin.knox.license";
    private final ServletContext context;


    /**
     * <p>Constructs new <code>KnoxConfigModule</code> instance. This implementation does nothing.</p>
     */
    public KnoxConfigModule(ServletContext context) {
        this.context = context;
    }

    /**
     * <p>Configures the environment.</p>
     */
    @Override
    protected void configure() {
        String licenseValue = this.context.getInitParameter(license);
        this.bindConstant().annotatedWith(Names.named(license)).to(licenseValue != null ? licenseValue : "");

    }
}

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

package com.hmdm.plugins.photo.guice.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import javax.servlet.ServletContext;

/**
 * <p>A <code>Guice</code> module used for configuring the environment for <code>Photo</code> plugin.</p>
 *
 * @author isv
 */
public class PhotoConfigModule extends AbstractModule {

    /**
     * <p>A context for plugin execution.</p>
     */
    private final ServletContext context;

    /**
     * <p>Constructs new <code>PhotoConfigModule</code> instance. This implementation does nothing.</p>
     */
    public PhotoConfigModule(ServletContext context) {
        this.context = context;
    }

    /**
     * <p>Configures the environment.</p>
     */
    protected void configure() {
        final String enablePlaces = this.context.getInitParameter("plugin.photo.enable.places");
        this.bindConstant().annotatedWith(Names.named("plugin.photo.enable.places")).to(Boolean.parseBoolean(enablePlaces));
    }
}

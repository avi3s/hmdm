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

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.hmdm.plugin.rest.PluginAccessFilter;
import com.hmdm.plugins.photo.rest.resource.PlaceResource;
import com.hmdm.plugins.photo.rest.resource.PhotoPluginSettingsResource;
import com.hmdm.plugins.photo.rest.resource.PhotoResource;
import com.hmdm.rest.filter.AuthFilter;
import com.hmdm.security.jwt.JWTFilter;

/**
 * <p>A <code>Guice</code> module for <code>Photo Plugin</code> REST resources.</p>
 *
 * @author isv
 */
public class PhotoRestModule extends ServletModule {

    /**
     * <p>Constructs new <code>PhotoRestModule</code> instance. This implementation does nothing.</p>
     */
    public PhotoRestModule() {
    }

    /**
     * <p>Configures the <code>Photo Plugin</code> REST resources.</p>
     */
    protected void configureServlets() {
        this.filter("/rest/plugins/photo/photo-plugin-settings/private").through(JWTFilter.class);
        this.filter("/rest/plugins/photo/photo-plugin-settings/private").through(AuthFilter.class);
        this.filter("/rest/plugins/photo/photo-plugin-settings/private").through(PluginAccessFilter.class);
        this.filter("/rest/plugins/photo/photo/private/*").through(JWTFilter.class);
        this.filter("/rest/plugins/photo/photo/private/*").through(AuthFilter.class);
        this.filter("/rest/plugins/photo/photo/private/*").through(PluginAccessFilter.class);
        this.filter("/rest/plugins/photo/place/private/*").through(JWTFilter.class);
        this.filter("/rest/plugins/photo/place/private/*").through(AuthFilter.class);
        this.filter("/rest/plugins/photo/place/private/*").through(PluginAccessFilter.class);
        this.bind(PhotoPluginSettingsResource.class);
        this.bind(PhotoResource.class);
        this.bind(PlaceResource.class);
    }

}

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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hmdm.plugins.knox.persistence.domain.Rule;
import com.hmdm.rest.json.SyncApplicationInt;
import com.hmdm.rest.json.SyncApplicationSettingInt;
import com.hmdm.rest.json.SyncConfigurationFileInt;
import com.hmdm.rest.json.SyncResponseInt;

import java.io.Serializable;
import java.util.List;

/**
 * <p>Returns the Knox features for the device
 * </p>
 *
 * @author isv
 */
public class KnoxSyncResponse implements Serializable {

    private static final long serialVersionUID = 7122574438455964509L;

    /**
     * <p>Knox license</p>
     */
    private String license;

    /**
     * Rules for filtering URLs and calls
     */
    private List<Rule> rules;

    /**
     * <p>Constructs new <code>KnoxSyncResponse</code> instance. This implementation does nothing.</p>
     */
    public KnoxSyncResponse() {
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    @JsonGetter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}

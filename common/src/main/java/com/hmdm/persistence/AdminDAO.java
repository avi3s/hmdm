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

package com.hmdm.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.*;
import com.hmdm.persistence.domain.admin.*;
import com.hmdm.persistence.mapper.*;
import com.hmdm.rest.json.DeviceCreateOptions;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.ws.rs.PathParam;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class AdminDAO {

    private static final Logger logger = LoggerFactory.getLogger(AdminDAO.class);

    private final AdminMapper adminMapper;
    /**
     * <p>Constructs new <code>AdminDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public AdminDAO(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    public List<Dashboard> getDashboard() {
        return null;
    }

    public List<Mandal> getMandalDetailsLists(String districtId) {
        return null;
    }

    public List<RBK> getRBKDetailsLists(String mandalId) {
        return null;
    }

    public List<Report> getReports() {
        return null;
    }

    public List<RedList> getRedLists() {
        return null;
    }

    public RBKDetails getRBKDetails(String rbkId) {
        return null;
    }

    public List<DistrictDetails> getDistrictLists() {

        return adminMapper.getDistrictLists();
    }

    public List<MandalDetails> getMandalLists(String districtId) {

        int id = Integer.valueOf(districtId);
        return adminMapper.getMandalLists(id);
    }

    public List<Kiosk> getKioskStatus() {
        return adminMapper.getKioskStatus();
    }
}

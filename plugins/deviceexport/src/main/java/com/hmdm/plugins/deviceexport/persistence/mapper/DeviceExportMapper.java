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

package com.hmdm.plugins.deviceexport.persistence.mapper;

import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportApplicationDeviceView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportConfigurationView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportGroupDeviceView;
import com.hmdm.plugins.deviceexport.persistence.domain.DeviceExportRecord;
import com.hmdm.plugins.deviceexport.rest.json.DeviceExportRequest;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.cursor.Cursor;

import java.util.List;

/**
 * <p>An ORM mapper for {@link DeviceExportRecord} domain object.</p>
 *
 * @author isv
 */
public interface DeviceExportMapper {

    /**
     * <p>Searches for the device records matching the specified parameters for export.</p>
     *
     * @param deviceSearchRequest the parameters for selecting the device records for export.
     * @return a cursor iterating over the devices to be exported.
     */
    Cursor<DeviceExportRecord> getDevicesForExport(DeviceExportRequest deviceSearchRequest);

    /**
     * <p>Gets the list of configurations for specified customer.</p>
     *
     * @return a list of existing configurations for specified customer.
     */
    List<DeviceExportConfigurationView> getConfigurations(@Param("customerId") int customerId);

    /**
     * <p>Gets the details on applications installed on devices related to specified customer.</p>
     *
     * @param customerId an ID of a customer.
     * @return a list of applications set on all devices for specified customer.
     */
    List<DeviceExportApplicationDeviceView> getDeviceApplicationsByCustomer(@Param("customerId") int customerId);


    /**
     * <p>Gets the details on device groups related to specified customer.</p>
     *
     * @param customerId an ID of a customer.
     * @return a list of groups for specified customer.
     */
    List<DeviceExportGroupDeviceView> getDeviceGroupsByCustomer(@Param("customerId") int customerId);
}

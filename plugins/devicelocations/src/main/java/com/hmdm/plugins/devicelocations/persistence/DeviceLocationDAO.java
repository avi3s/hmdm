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

package com.hmdm.plugins.devicelocations.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.persistence.mapper.DeviceMapper;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;
import com.hmdm.plugins.devicelocations.persistence.mapper.DeviceLocationMapper;
import com.hmdm.plugins.devicelocations.rest.json.DeviceLocationHistoryPreview;
import com.hmdm.plugins.devicelocations.rest.json.DeviceView;
import com.hmdm.rest.json.PaginatedData;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import org.apache.ibatis.annotations.Param;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>A DAO for {@link DeviceLocation} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class DeviceLocationDAO {

    private static final Logger logger = LoggerFactory.getLogger(DeviceLocationDAO.class);

    /**
     * <p>An ORM mapper for device location objects.</p>
     */
    private final DeviceLocationMapper mapper;

    /**
     * <p>An interface to device persistence layer.</p>
     */
    private final DeviceMapper deviceMapper;

    /**
     * <p>Constructs new <code>DeviceLocationDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLocationDAO(DeviceLocationMapper mapper, DeviceMapper deviceMapper) {
        this.mapper = mapper;
        this.deviceMapper = deviceMapper;
    }

    /**
     * <p>Records the specified details on device location to DB.</p>
     *
     * @param location a device location.
     */
    @Transactional
    public void recordLatestDeviceLocation(DeviceLocation location) {
        // Do not check whether the location is latest, just replace
        // because this saves resources and fixes occasional timestamp errors
        this.mapper.saveDeviceLatestLocation(location);
    }

    /**
     * <p>Records the specified details on device location to DB.</p>
     *
     * @param locations list of device locations.
     */
    @Transactional
    public void recordDeviceLocationHistory(List<DeviceLocation> locations) {
        if (locations.size() > 0) {
            this.mapper.saveDeviceLocationHistory(locations);
        }
    }

    /**
     * <p>Deletes the device locations records which are older than number of days configured in customer's profile.</p>
     */
    public void purgeDeviceLocationHistoryRecords() {
        try {
            logger.info("Deleting outdated device location records...");
            final int count = this.mapper.purgeDeviceLocationRecords();
            logger.info("Deleted {} outdated device location records", count);
        } catch (Exception e) {
            logger.error("Unexpected error when purging the device location records", e);
        }
    }

    /**
     * <p>Gets the portion of devices list matching the specified criteria.</p>
     *
     * @param filter a filter to be used for filtering the devices by imei, phone, etc.
     * @param groupId an optional ID of a group to filter devices for.
     * @param pageNum a number of page of data to be returned.
     * @param pageSize a maximum number of records to be returned.
     * @return a portion of device list.
     */
    @Transactional
    public PaginatedData<DeviceView> findDevices(String filter, Integer groupId, int pageNum, int pageSize) {
        PaginatedData<DeviceView> result = SecurityContext.get().getCurrentUser().map(user -> {
            DeviceSearchRequest searchRequest = new DeviceSearchRequest();
            searchRequest.setValue(filter);
            searchRequest.setPageSize(pageSize);
            searchRequest.setPageNum(pageNum);
            searchRequest.setCustomerId(user.getCustomerId());
            searchRequest.setUserId(user.getId());
            searchRequest.setGroupId(groupId);

            final List<DeviceView> devices = this.deviceMapper.getAllDevices(searchRequest)
                    .stream()
                    .map(DeviceView::new)
                    .collect(Collectors.toList());
            final Long count = this.deviceMapper.countAllDevices(searchRequest);

            return new PaginatedData<>(devices, count);
        }).orElseThrow(SecurityException::onAnonymousAccess);
        
        return result;
    }

    /**
     * <p>Gets the latest location for the specified device.</p>
     *
     * @param deviceId an ID of a device to get latest location for.
     * @return an optional reference to latest device location.
     * @throws SecurityException on anonymous access.
     */
    public Optional<DeviceLocation> getLatestDeviceLocation(int deviceId) {
        return SecurityContext.get().getCurrentUser()
                .map(user -> Optional.ofNullable(this.mapper.getLatestDeviceLocation(deviceId, user.getCustomerId())))
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    /**
     * <p>Gets the latest location for the specified devices.</p>
     *
     * @param deviceIds  a list of IDs of a device to get latest location for.
     * @return a list of latest device locations.
     * @throws SecurityException on anonymous access.
     */
    public List<DeviceLocation> getLatestDeviceLocations(List<Integer> deviceIds) {
        return SecurityContext.get().getCurrentUser()
                .map(user -> this.mapper.getLatestDeviceLocations(deviceIds, user.getCustomerId()))
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    /**
     * <p>Gets the portion of devices list matching the specified criteria.</p>
     *
     * @return a portion of device list.
     */
    @Transactional
    public List<DeviceView> findDevices(List<Integer> deviceIds) {
        List<DeviceView> result = SecurityContext.get().getCurrentUser().map(user -> {
            final List<DeviceView> devices = this.mapper.getDevices(deviceIds, user.getCustomerId())
                    .stream()
                    .map(DeviceView::new)
                    .collect(Collectors.toList());

            return devices;
        }).orElseThrow(SecurityException::onAnonymousAccess);

        return result;
    }

    /**
     * <p>A maximum number of records to be returned by a single query for device location history records.</p>
     */
    private static final int LOCATIONS_HISTORY_FETCH_SIZE = 50;

    /**
     * <p>Gets the device location history records for the specified chunk.</p>
     *
     * @param deviceId an ID of a device to count records for.
     * @param dateFrom a lower bound of date range (in milliseconds since epoch time).
     * @param dateTo an upper bound of date range (in milliseconds since epoch time).
     * @param pageNum a number of chunk of data to retrieve.
     * @return a list of device location history records matching the query.
     */
    public List<DeviceLocation> getDeviceLocationsHistory(Integer deviceId, Long dateFrom, Long dateTo, Integer pageNum) {
        final List<DeviceLocation> locations = SecurityContext.get().getCurrentUser().map(user -> {
            List<DeviceLocation> result = this.mapper.getDeviceLocationHistoryRecords(
                    deviceId, user.getCustomerId(), dateFrom, dateTo, pageNum, LOCATIONS_HISTORY_FETCH_SIZE
            );
            return result;
        }).orElseThrow(SecurityException::onAnonymousAccess);

        return locations;
    }

    /**
     * <p>Gets the preview for characteristics of potential query for device location history records.</p>
     *
     * @param deviceId an ID of a device to count records for.
     * @param dateFrom a lower bound of date range (in milliseconds since epoch time).
     * @param dateTo an upper bound of date range (in milliseconds since epoch time).
     * @return a preview for potential query result.
     */
    public DeviceLocationHistoryPreview getDeviceLocationsHistoryPreview(Integer deviceId, Long dateFrom, Long dateTo) {
        final Integer totalItemsCount = SecurityContext.get().getCurrentUser().map(user -> {
            int count = this.mapper.countDeviceLocationHistoryRecords(
                    deviceId, user.getCustomerId(), dateFrom, dateTo
            );
            return count;
        }).orElseThrow(SecurityException::onAnonymousAccess);

        return new DeviceLocationHistoryPreview(totalItemsCount, LOCATIONS_HISTORY_FETCH_SIZE);
    }
}

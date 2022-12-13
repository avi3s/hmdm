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

import com.hmdm.event.DeviceLocationUpdatedEvent;
import com.hmdm.event.EventListener;
import com.hmdm.event.EventType;
import com.hmdm.plugins.devicelocations.persistence.domain.DeviceLocation;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>A listener for the events of {@link DeviceLocationUpdatedEvent} type. This listener is responsible for saving the
 * details on device location to DB.</p>
 *
 * @author isv
 */
public class DeviceLocationUpdatedEventListener implements EventListener<DeviceLocationUpdatedEvent> {

    /**
     * <p>An interface to the persistence layer.</p>
     */
    private final DeviceLocationDAO deviceLocationDAO;

    /**
     * <p>Constructs new <code>DeviceLocationUpdatedEventListener</code> instance. This implementation does nothing.</p>
     */
    public DeviceLocationUpdatedEventListener(DeviceLocationDAO deviceLocationDAO) {
        this.deviceLocationDAO = deviceLocationDAO;
    }

    /**
     * <p>Handles the event. Saves the details on device location to DB.</p>
     *
     * @param event an event fired from the external source.
     */
    @Override
    public void onEvent(DeviceLocationUpdatedEvent event) {
        if (event.isFromDetailedInfo()) {
            List<DeviceLocation> pluginLocations = new LinkedList<>();
            for (com.hmdm.rest.json.DeviceLocation location : event.getLocations()) {
                if (location.getLat() != 0 || location.getLon() != 0) {
                    pluginLocations.add(new DeviceLocation(location, event.getDeviceId()));
                }
            }
            try {
                this.deviceLocationDAO.recordDeviceLocationHistory(pluginLocations);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (event.getLocations().size() < 1) {
                return;
            }
            try {
                DeviceLocation location = new DeviceLocation(event.getLocations().get(0), event.getDeviceId());
                if (location.getLat() != 0 || location.getLon() != 0) {
                    this.deviceLocationDAO.recordLatestDeviceLocation(location);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Gets the type of supported events.</p>
     *
     * @return {@link EventType#DEVICE_LOCATION_UPDATED} always.
     */
    @Override
    public EventType getSupportedEventType() {
        return EventType.DEVICE_LOCATION_UPDATED;
    }
}

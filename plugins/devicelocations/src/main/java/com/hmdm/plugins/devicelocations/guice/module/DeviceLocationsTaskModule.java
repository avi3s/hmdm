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

package com.hmdm.plugins.devicelocations.guice.module;

import com.google.inject.Inject;
import com.hmdm.event.EventService;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.devicelocations.persistence.DeviceLocationDAO;
import com.hmdm.plugins.devicelocations.persistence.DeviceLocationUpdatedEventListener;
import com.hmdm.util.BackgroundTaskRunnerService;

import java.util.concurrent.TimeUnit;

/**
 * <p>A module used for initializing the tasks to be executed in background.</p>
 *
 * @author isv
 */
public class DeviceLocationsTaskModule implements PluginTaskModule {

    /**
     * <p>A runner for the repeatable tasks.</p>
     */
    private final BackgroundTaskRunnerService taskRunner;

    /**
     * <p>An interface to application's events.</p>
     */
    private final EventService eventService;

    /**
     * <p>An interface to the persistence layer.</p>
     */
    private final DeviceLocationDAO deviceLocationDAO;

    /**
     * <p>Constructs new <code>DeviceLocationsTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public DeviceLocationsTaskModule(DeviceLocationDAO deviceLocationDAO,
                                     BackgroundTaskRunnerService taskRunner,
                                     EventService eventService, EventService eventService1) {
        this.deviceLocationDAO = deviceLocationDAO;
        this.taskRunner = taskRunner;
        this.eventService = eventService1;
    }

    /**
     * <p>Initializes this module. Schedules the task for purging the outdated device location records from DB on a
     * daily basis.</p>
     */
    @Override
    public void init() {
        taskRunner.submitRepeatableTask(deviceLocationDAO::purgeDeviceLocationHistoryRecords, 1, 24, TimeUnit.HOURS);
        this.eventService.addEventListener(new DeviceLocationUpdatedEventListener(this.deviceLocationDAO));
    }

}

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

import com.google.inject.Inject;
import com.hmdm.persistence.UnsecureDAO;
import com.hmdm.persistence.domain.Customer;
import com.hmdm.plugin.PluginTaskModule;
import com.hmdm.plugins.photo.persistence.PhotoDAO;
import com.hmdm.plugins.photo.persistence.PhotoPluginSettingsDAO;
import com.hmdm.plugins.photo.persistence.domain.Photo;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.plugins.photo.rest.json.PhotoPluginDeviceSettings;
import com.hmdm.security.SecurityContext;
import com.hmdm.util.BackgroundTaskRunnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>A module used for initializing the tasks to be executed in background.</p>
 *
 * @author isv
 */
public class PhotoTaskModule implements PluginTaskModule {

    private static final Logger logger = LoggerFactory.getLogger(PhotoTaskModule.class);

    // A base directory for file storage
    private final String filesDirectory;

    /**
     * <p>An interface to persistence layer.</p>
     */
    private final PhotoDAO photoDAO;

    private final PhotoPluginSettingsDAO settingsDAO;

    private final UnsecureDAO unsecureDAO;

    /**
     * <p>A runner for the repeatable tasks.</p>
     */
    private final BackgroundTaskRunnerService taskRunner;

    /**
     * <p>Constructs new <code>DeviceInfoTaskModule</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PhotoTaskModule(@Named("plugins.files.directory") String filesDirectory,
                           PhotoDAO photoDAO,
                           PhotoPluginSettingsDAO settingsDAO,
                           UnsecureDAO unsecureDAO,
                           BackgroundTaskRunnerService taskRunner) {
        this.filesDirectory = filesDirectory;
        this.photoDAO = photoDAO;
        this.settingsDAO = settingsDAO;
        this.unsecureDAO = unsecureDAO;
        this.taskRunner = taskRunner;
    }

    /**
     * <p>Initializes this module. Schedules the task for purging the outdated device info records from DB on a daily
     * basis.</p>
     */
    @Override
    public void init() {
        taskRunner.submitRepeatableTask(this::clearOldFiles, 1, 24, TimeUnit.HOURS);
    }


    /**
     * <p>Deletes the photo files which are older than number of days configured in customer's profile.</p>
     */
    public void clearOldFiles() {
        try {
            logger.info("Deleting outdated photos...");

            List<Customer> customers = unsecureDAO.getAllCustomersUnsecure();
            for (Customer c : customers) {
                clearOldFilesForCustomer(c.getId());
            }

        } catch (Exception e) {
            logger.error("Unexpected error when purging the old photos", e);
        }
    }

    private void clearOldFilesForCustomer(int customerId) {
        PhotoPluginSettings settings = settingsDAO.getPluginSettings(customerId);
        if (settings.getPurgeDays() <= 0) {
            logger.info("Max age for photos not set for customer {}", customerId);
            return;
        }

        List<Photo> oldPhotos = photoDAO.getOldPhotos(customerId);
        int count = 0;
        for (Photo photo : oldPhotos) {
            if (photo.getThumbnailImagePath() != null) {
                try {
                    File file = new File(this.filesDirectory + File.separator + photo.getThumbnailImagePath());
                    file.delete();
                } catch (Exception e) {
                    logger.error("Failed to delete photo file " + photo.getThumbnailImagePath());
                }
            }
            if (photo.getPath() != null) {
                try {
                    File file = new File(this.filesDirectory + File.separator + photo.getPath());
                    file.delete();
                } catch (Exception e) {
                    logger.error("Failed to delete photo file " + photo.getPath());
                }
            }
            try {
                photoDAO.deletePhotoUnsecure(photo.getId());
                count++;
            } catch (Exception e) {
                logger.error("Failed to delete photo id " + photo.getId());
            }
        }
        if (count > 0) {
            logger.info("Deleted {} old photos for customer {}", count, customerId);
        }
    }
}

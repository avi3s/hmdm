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

package com.hmdm.plugins.photo.task;

import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hmdm.plugins.photo.persistence.PhotoDAO;
import com.hmdm.plugins.photo.persistence.domain.Photo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * <p>A standalone task to be used for generating the thumbnail image for uploaded photo.</p>
 *
 * @author isv
 */
public class ThumbnailImageGenerationTask implements Runnable {

    /**
     * <p>A logger to be used for logging the events.</p>
     */
    private static final Logger log = LoggerFactory.getLogger("ThumbnailImageGenerationTask");

    /**
     * <p>A photo to generate the thumbnail image for.</p>
     */
    private final Photo photo;

    /**
     * <p>A base directory for file storage.</p>
     */
    private final String baseFilesDir;

    /**
     * <p>A relative path of the image.</p>
     */
    private final String relativeDir;

    /**
     * <p>A main (w/o extension) name of the image.</p>
     */
    private final String fileNameMain;

    /**
     * <p>A DAO used for updating the photo objects in DB.</p>
     */
    private final PhotoDAO photoDAO;

    /**
     * <p>Constructs new <code>ThumbnailImageGenerationTask</code> instance. This implementation does nothing.</p>
     */
    public ThumbnailImageGenerationTask(Photo photo, String baseFilesDir, PhotoDAO photoDAO, String relativeDir, String fileNameMain) {
        if ((baseFilesDir == null) || (baseFilesDir.trim().length() == 0)) {
            throw new IllegalArgumentException("The parameter [baseFilesDir] is not valid. [" + baseFilesDir + "]");
        }
        if (photo == null) {
            throw new IllegalArgumentException("The parameter [photo] is NULL");
        }
        if (photoDAO == null) {
            throw new IllegalArgumentException("The parameter [photoDAO] is NULL");
        }

        this.photo = photo;
        this.baseFilesDir = baseFilesDir;
        this.photoDAO = photoDAO;
        this.relativeDir = relativeDir;
        this.fileNameMain = fileNameMain;
    }

    /**
     * <p>Generates the thumbnail image for the specified photo.</p>
     */
    @Override
    public void run() {
        log.debug("Starting execution of task: {}", this);
        try {
            BufferedImage img = ImageIO.read(new File(this.baseFilesDir + File.separator + this.photo.getPath()));
            BufferedImage scaledImage = Scalr.resize(img, 200);

            DecimalFormat numberFormat = new DecimalFormat("##00");
            LocalDateTime date = LocalDateTime.ofEpochSecond(
                    photo.getCreateTime().getTime() / 1000, 0, ZoneOffset.UTC
            );
            String yearDir = numberFormat.format(date.getYear());
            String monthDir = numberFormat.format(date.getMonthValue());
            String dayDir = numberFormat.format(date.getDayOfMonth());

            java.nio.file.Path uploadFilePathDir = Paths.get(this.baseFilesDir, "plugin", "photo", "thumbnail", relativeDir);
            uploadFilePathDir = Files.createDirectories(uploadFilePathDir);

            String localFileName = fileNameMain + ".png";

            File thumbNailFile = new File(uploadFilePathDir.toFile(), localFileName);
            ImageIO.write(scaledImage, "png", thumbNailFile);

            photoDAO.setThumbnailImagePath(
                    photo.getId(),
                    Paths.get( "plugin", "photo", "thumbnail", yearDir, monthDir, dayDir, localFileName).toString()
            );

            log.debug("Generated thumbnail image for photo #{}", this.photo.getId());
        } catch (Throwable e) {
            log.error("Unexpected error while generating thumbnail image for photo #{}", this.photo.getId(), e);
        }
    }

    @Override
    public String toString() {
        return "ThumbnailImageGenerationTask{" +
                "photo=" + photo +
                ", baseFilesDir='" + baseFilesDir + '\'' +
                '}';
    }
}

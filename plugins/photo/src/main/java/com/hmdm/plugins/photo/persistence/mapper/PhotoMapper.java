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

package com.hmdm.plugins.photo.persistence.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;
import com.hmdm.plugins.photo.persistence.domain.Photo;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.plugins.photo.rest.json.PhotoFilter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * <p>An ORM mapper for {@link Photo} domain object.</p>
 *
 * @author isv
 */
public interface PhotoMapper {

    List<Photo> findAllPhotosByCustomerId(PhotoFilter filter);

    Long countAll(PhotoFilter filter);

    @Insert("INSERT INTO plugin_photo_photo (createTime, lat, lng, path, deviceId, customerId, contentType, address) " +
            "VALUES (#{createTime}, #{lat}, #{lng}, #{path}, #{deviceId}, #{customerId}, #{contentType}, #{address})")
    @SelectKey( statement = "SELECT currval('plugin_photo_photo_id_seq')", keyColumn = "id", keyProperty = "id", before = false, resultType = int.class )
    void insertPhoto(Photo photo);

    @Insert("INSERT INTO plugin_photo_photo_places (photoId, pointAddress, pointId) " +
            "VALUES (#{id}, #{pointAddress}, #{pointId})")
    void insertPhotoPlace(Photo photo);

    @Update("UPDATE plugin_photo_photo SET thumbnailImagePath = #{thumbnailImagePath} WHERE id=#{id}")
    void setThumbnailImagePath(@Param("id") int photoId, @Param("thumbnailImagePath") String thumbnailImagePath);

    @Delete("DELETE FROM plugin_photo_photo WHERE id=#{id}")
    void deletePhoto(@Param("id") int id);

    @Select("SELECT photo.*, devices.number AS deviceNumber, places.pointId, places.pointAddress " +
            "FROM plugin_photo_photo photo " +
            "INNER JOIN devices ON devices.id = photo.deviceid " +
            "LEFT JOIN plugin_photo_photo_places places ON places.photoId = photo.id " +
            "WHERE photo.id = #{id}")
    Photo findById(@Param("id") int id);

    // ------------ photo plugin settings ------------------------------------------------------------------------------
    @Select("SELECT settings.* " +
            "FROM plugin_photo_settings settings " +
            "WHERE customerId=#{customerId}")
    PhotoPluginSettings findPluginSettingsByCustomerId(@Param("customerId") Integer customerId);

    @Insert("INSERT INTO plugin_photo_settings (customerId, trackLocation, trackingOffWarning, sendPhoto, imagePaths, " +
            "imageDeletionDelay, addText, backgroundColor, textColor, transparency, textContent, linkPhotoToPlace, " +
            "searchPlaceRadius, nonTransmittedPaths, includeStandardImagePaths, fileTypes, directory, purgeDays, nameTemplate) " +
            "VALUES (#{customerId}, #{trackLocation}, #{trackingOffWarning}, #{sendPhoto}, #{imagePaths}, " +
            "#{imageDeletionDelay}, #{addText}, #{backgroundColor}, #{textColor}, #{transparency}, #{textContent}, " +
            "#{linkPhotoToPlace}, #{searchPlaceRadius}, #{nonTransmittedPaths}, #{includeStandardImagePaths}, " +
            " #{fileTypes}, #{directory}, #{purgeDays}, #{nameTemplate})")
    void insertPluginSettings(PhotoPluginSettings settings);

    @Update("UPDATE plugin_photo_settings " +
            "SET " +
            "trackLocation = #{trackLocation}, " +
            "trackingOffWarning = #{trackingOffWarning}, " +
            "sendPhoto = #{sendPhoto}, " +
            "imagePaths = #{imagePaths}, " +
            "imageDeletionDelay = #{imageDeletionDelay}, " +
            "addText = #{addText}, " +
            "backgroundColor = #{backgroundColor}, " +
            "textColor = #{textColor}, " +
            "transparency = #{transparency}, " +
            "textContent = #{textContent}," +
            "linkPhotoToPlace = #{linkPhotoToPlace}, " +
            "searchPlaceRadius = #{searchPlaceRadius}, " +
            "nonTransmittedPaths = #{nonTransmittedPaths}, " +
            "includeStandardImagePaths = #{includeStandardImagePaths}, " +
            "fileTypes = #{fileTypes}, " +
            "directory = #{directory}, " +
            "purgeDays = #{purgeDays}, " +
            "nameTemplate = #{nameTemplate} " +
            "WHERE id=#{id} AND customerId=#{customerId}")
    void updatePluginSettings(PhotoPluginSettings settings);

    @Select("SELECT COUNT(*) FROM plugin_photo_photo WHERE deviceId = #{deviceId} AND createTime = #{createTime}")
    long countPhotos(@Param("deviceId") Integer deviceId, @Param("createTime") Date createTime);

    @Select("SELECT * FROM plugin_photo_photo " +
            "WHERE createtime < (SELECT DATE_TRUNC('day', NOW() - (ps.purgeDays || ' day')::INTERVAL) " +
            "            FROM plugin_photo_settings ps " +
            "            WHERE ps.customerId =  #{customerId})")
    List<Photo> getOldPhotos(@Param("customerId") int customerId);
}

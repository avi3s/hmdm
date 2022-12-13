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

package com.hmdm.plugins.photo.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.inject.Named;
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.photo.persistence.domain.Photo;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.plugins.photo.persistence.mapper.PhotoMapper;
import com.hmdm.plugins.photo.rest.json.PhotoFilter;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import org.mybatis.guice.transactional.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * <p>A DAO for {@link Photo} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class PhotoDAO extends AbstractDAO<Photo> {

    /**
     * <p>An ORM mapper for domain object type.</p>
     */
    private final PhotoMapper photoMapper;

    /**
     * <p>A flag indicating if <code>Places</code> feature of plugin is enabled or not.</p>
     */
    private final boolean featurePlacesEnabled;

    /**
     * <p>Constructs new <code>PhotoDAO</code> instance. This implementation does nothing.</p>
     *
     * @param photoMapper an ORM mapper for {@link Photo} domain object type.
     */
    @Inject
    public PhotoDAO(PhotoMapper photoMapper,
                    @Named("plugin.photo.enable.places") boolean featurePlacesEnabled) {
        this.photoMapper = photoMapper;
        this.featurePlacesEnabled = featurePlacesEnabled;
    }

    /**
     * <p>Gets the list of all photos related to customer account associated with the current user.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a list of photo objects.
     */
    public List<Photo> findAll(PhotoFilter filter) {
        prepareFilter(filter);
        return this.getListWithCurrentUser(currentUser -> {
            final PhotoPluginSettings settings = this.photoMapper.findPluginSettingsByCustomerId(currentUser.getCustomerId());
            filter.setCustomerId(currentUser.getCustomerId());
            filter.setUserId(currentUser.getId());
            filter.setFeaturePlacesEnabled(this.featurePlacesEnabled);
            if (settings != null) {
                filter.setLinkPhotoToPlace(settings.isLinkPhotoToPlace());
            }
            return this.photoMapper.findAllPhotosByCustomerId(filter);
        });
    }

    /**
     * <p>Gets the number of all photos related to customer account associated with the current user.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a list of photo objects.
     */
    public long countAll(PhotoFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    final PhotoPluginSettings settings = this.photoMapper.findPluginSettingsByCustomerId(user.getCustomerId());
                    filter.setCustomerId(user.getCustomerId());
                    filter.setFeaturePlacesEnabled(this.featurePlacesEnabled);
                    if (settings != null) {
                        filter.setLinkPhotoToPlace(settings.isLinkPhotoToPlace());
                    }
                    return this.photoMapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Inserts the specified photo to database. This method intended only for usage when handling the requests from
     * the devices.</p>
     *
     * @param photo the details for new photo object.
     * @throws DuplicatePhotoException if photo with specified create time already exists for device.
     */
    @Transactional
    public void insertPhoto(Photo photo) {
        long count = this.countPhotos(photo.getDeviceId(), photo.getCreateTime());

        if (count > 0) {
            throw new DuplicatePhotoException(photo.getDeviceId(), photo.getCreateTime());
        }

        this.photoMapper.insertPhoto(photo);
        if (photo.getPointAddress() != null) {
            if (photo.getPointId() != null) {
                this.photoMapper.insertPhotoPlace(photo);
            }
        }
    }

    /**
     * <p>Sets the thumbnail image for the specified photo.</p>
     *
     * @param photoId an ID of a photo image object in DB.
     * @param thumbnailImagePath a path for thumbnail image for photo.
     */
    public void setThumbnailImagePath(int photoId, String thumbnailImagePath) {
        this.photoMapper.setThumbnailImagePath(photoId, thumbnailImagePath);
    }

    /**
     * <p>Deletes the photo referenced by the specified ID.</p>
     *
     * @param photoId an ID of a photo to be deleted.
     */
    public void deletePhoto(int photoId) {
        updateById(
                photoId,
                this.photoMapper::findById,
                photo -> this.photoMapper.deletePhoto(photo.getId()),
                (photo) -> SecurityException.onCustomerDataAccessViolation(photo.getId(), "photo")
        );
    }

    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(PhotoFilter filter) {
        if (filter.getDeviceFilter() != null) {
            if (filter.getDeviceFilter().trim().isEmpty()) {
                filter.setDeviceFilter(null);
            } else {
                filter.setDeviceFilter('%' + filter.getDeviceFilter().trim() + '%');
            }
        }
        if (filter.getAddressFilter() != null) {
            if (filter.getAddressFilter().trim().isEmpty()) {
                filter.setAddressFilter(null);
            } else {
                filter.setAddressFilter('%' + filter.getAddressFilter().trim() + '%');
            }
        }
        if (filter.getPointFilter() != null) {
            if (filter.getPointFilter().trim().isEmpty()) {
                filter.setPointFilter(null);
            } else {
                filter.setPointFilter('%' + filter.getPointFilter().trim() + '%');
            }
        }
    }

    /**
     * <p>Finds the photo by ID.</p>
     *
     * @param id an ID of a photo.
     * @return a photo matching the specified ID or <code>null</code> if there is no such photo.
     */
    public Optional<Photo> findById(int id) {
        return Optional.ofNullable(getSingleRecord(
                () -> this.photoMapper.findById(id),
                (photo) -> SecurityException.onCustomerDataAccessViolation(photo.getId(), "photo")
        ));
    }

    public long countPhotos(Integer deviceId, Date createTime) {
        return this.photoMapper.countPhotos(deviceId, createTime);
    }

    public List<Photo> getOldPhotos(int customerId) {
        return this.photoMapper.getOldPhotos(customerId);
    }

    /**
     * <p>Deletes the photo referenced by the specified ID.
     *    Doesn't check the customer ID so should not be used in public web methods
     *  </p>
     *
     * @param photoId an ID of a photo to be deleted.
     */
    public void deletePhotoUnsecure(int photoId) {
        this.photoMapper.deletePhoto(photoId);
    }
}

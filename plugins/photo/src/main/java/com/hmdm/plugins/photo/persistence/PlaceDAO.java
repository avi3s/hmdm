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
import com.hmdm.persistence.AbstractDAO;
import com.hmdm.plugins.photo.persistence.domain.Photo;
import com.hmdm.plugins.photo.persistence.domain.PhotoPluginSettings;
import com.hmdm.plugins.photo.persistence.domain.Place;
import com.hmdm.plugins.photo.persistence.mapper.PlaceMapper;
import com.hmdm.plugins.photo.rest.json.PhotoFilter;
import com.hmdm.plugins.photo.rest.json.PlaceFilter;
import com.hmdm.plugins.photo.rest.json.PlaceSearchResultItem;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>A DAO for {@link Place} domain objects.</p>
 *
 * @author isv
 */
@Singleton
public class PlaceDAO extends AbstractDAO<Place> {

    /**
     * <p>An ORM mapper for place objects.</p>
     */
    private final PlaceMapper placeMapper;

    /**
     * <p>Constructs new <code>PlaceDAO</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PlaceDAO(PlaceMapper placeMapper) {
        this.placeMapper = placeMapper;
    }

    public List<LookupItem> getPlacesForLookup() {
        return SecurityContext.get().getCurrentUser()
                .map(u -> this.placeMapper.getPlacesForLookup(u.getCustomerId()))
                .orElse(new ArrayList<>());
    }

    public void insertPlace(Place newPlace) {
        insertRecord(newPlace, this.placeMapper::insertPlace);
    }

    public void updatePlace(Place dbPlace) {
        SecurityContext.get().getCurrentUser()
                .map(u -> {
                    dbPlace.setCustomerId(u.getCustomerId());
                    this.placeMapper.updatePlace(dbPlace);
                    return null;
                });
    }

    public List<String> findPlaces(String filter, int resultsCount) {
        String searchFilter = '%' + filter.trim() + '%';
        return SecurityContext.get().getCurrentUser()
                .map(u -> this.placeMapper.lookupPlaces(u.getCustomerId(), searchFilter, resultsCount))
                .orElse(new ArrayList<>());
    }

    public List<PlaceSearchResultItem> findNearestPlaces(int customerId, double lat, double lng, int radius) {
        return this.placeMapper.findNearestPlaces(customerId, lat, lng, radius);
    }

    /**
     * <p>Deletes all existing place records for current user from database.</p>
     *
     * @return the number of deleted records.
     */
    public int deleteAllPlaces() {
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    return this.placeMapper.deleteAllPlaces(user.getCustomerId());
                })
                .orElseThrow(SecurityException::onAnonymousAccess);
    }

    /**
     * <p>Gets the list of all places related to customer account associated with the current user.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a list of place objects.
     */
    public List<Place> findAll(PlaceFilter filter) {
        prepareFilter(filter);
        return this.getListWithCurrentUser(currentUser -> {
            filter.setCustomerId(currentUser.getCustomerId());
            return this.placeMapper.findAllPlacesByCustomerId(filter);
        });
    }

    /**
     * <p>Gets the number of all places related to customer account associated with the current user.</p>
     *
     * @param filter a filter to be used for filtering the records.
     * @return a list of places objects.
     */
    public long countAll(PlaceFilter filter) {
        prepareFilter(filter);
        return SecurityContext.get().getCurrentUser()
                .map(user -> {
                    filter.setCustomerId(user.getCustomerId());
                    return this.placeMapper.countAll(filter);
                })
                .orElse(0L);
    }

    /**
     * <p>Prepares the filter for usage by mapper.</p>
     *
     * @param filter a filter provided by request.
     */
    private static void prepareFilter(PlaceFilter filter) {
        if (filter.getFilter() != null) {
            if (filter.getFilter().trim().isEmpty()) {
                filter.setFilter(null);
            } else {
                filter.setFilter('%' + filter.getFilter().trim() + '%');
            }
        }
    }
}

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

import com.hmdm.plugins.photo.persistence.domain.Place;
import com.hmdm.plugins.photo.rest.json.PlaceFilter;
import com.hmdm.plugins.photo.rest.json.PlaceSearchResultItem;
import com.hmdm.rest.json.LookupItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>An ORM mapper for {@link Place} domain object.</p>
 *
 * @author isv
 */
public interface PlaceMapper {

    @Select("SELECT id, placeId AS name FROM plugin_photo_places WHERE customerId = #{customerId}")
    List<LookupItem> getPlacesForLookup(@Param("customerId") int customerId);

    @Insert("INSERT INTO plugin_photo_places (placeId, lat, lng, address, reserve, customerId) " +
            "VALUES (#{placeId}, #{lat}, #{lng}, #{address}, #{reserve}, #{customerId})")
    int insertPlace(Place place);

    @Insert("UPDATE plugin_photo_places SET " +
            "placeId = #{placeId}, lat = #{lat}, lng = #{lng}, address = #{address}, reserve = #{reserve} " +
            "WHERE id = #{id}")
    int updatePlace(Place dbPlace);

    @Select("SELECT placeId " +
            "FROM plugin_photo_places " +
            "WHERE customerId = #{customerId} AND placeId ILIKE #{filter} " +
            "ORDER BY placeId " +
            "LIMIT #{pageSize} "
            )
    List<String> lookupPlaces(@Param("customerId") int customerId, @Param("filter") String searchFilter, @Param("pageSize") int resultsCount);

    @Select("SELECT " +
            "    places.placeId AS pointId, " +
            "    places.address AS pointAddress, " +
            "    places.lat AS lat, " +
            "    places.lng AS lng, " +
            "    places.distance AS distance " +
            "FROM " +
            "(SELECT " +
            "      plugin_photo_places.*, " +
            "      ST_Distance_Sphere(ST_MakePoint(lng, lat), ST_MakePoint(#{lng}, #{lat})) AS distance " +
            "FROM plugin_photo_places " +
            "WHERE plugin_photo_places.customerId = #{customerId}) places " +
            "WHERE places.distance <= #{radius} " +
            "ORDER BY places.distance ASC " +
            "LIMIT 1")
    List<PlaceSearchResultItem> findNearestPlaces(@Param("customerId") Integer customerId, @Param("lat") double lat,
                                                  @Param("lng") double lng, @Param("radius") int radius);

    @Delete("DELETE FROM plugin_photo_places WHERE customerId = #{customerId}")
    int deleteAllPlaces(@Param("customerId") Integer customerId);

    long countAll(PlaceFilter filter);

    List<Place> findAllPlacesByCustomerId(PlaceFilter filter);
}

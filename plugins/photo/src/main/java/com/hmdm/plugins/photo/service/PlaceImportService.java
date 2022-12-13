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

package com.hmdm.plugins.photo.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hmdm.plugins.photo.persistence.PlaceDAO;
import com.hmdm.plugins.photo.persistence.domain.Place;
import com.hmdm.plugins.photo.rest.json.PlaceExcelFileParsingResult;
import com.hmdm.plugins.photo.rest.json.PlaceImportConfirmRequest;
import com.hmdm.plugins.photo.rest.json.PlaceImportRequest;
import com.hmdm.plugins.photo.rest.json.PlaceImportResult;
import com.hmdm.plugins.photo.rest.json.PlaceImportStatusItem;
import com.hmdm.rest.json.LookupItem;
import com.hmdm.security.SecurityException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.mybatis.guice.transactional.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hmdm.util.FileUtil.writeToFile;

/**
 * <p>A service used for supporting places import process.</p>
 *
 * @author isv
 */
@Singleton
public class PlaceImportService {

    private static final Logger logger = LoggerFactory.getLogger(PlaceImportService.class);
    private static final int ALIVE_TIME_AMOUNT = 30;
    private static final TimeUnit ALIVE_TIME_UNIT = TimeUnit.MINUTES;

    private final PlaceDAO placeDAO;

    /**
     * <p>A mapping from unique UUIDs to paths to files uploaded to server.</p>
     */
    private static final Cache<String, String> uploadedFilesPaths = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .removalListener((RemovalListener<String, String>) notification -> {
                final String fileAbsolutePath = notification.getValue();
                try {
                    final boolean deleted = Files.deleteIfExists(new File(fileAbsolutePath).toPath());
                    if (deleted) {
                        logger.debug("Deleted temporary Excel file: {}", fileAbsolutePath);
                    }
                } catch (IOException e) {
                    logger.error("Failed to delete temporary Excel file: {}", fileAbsolutePath, e);
                }

            })
            .build();

    /**
     * <p>A mapping from unique UUIDs to results of parsing the Excel file.</p>
     */
    private static final Cache<String, List<PlaceImportStatusItem>> uploadedFilesParsingResults = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .build();

    /**
     * <p>A mapping from unique UUIDs to requests of parsing the Excel file.</p>
     */
    private static final Cache<String, PlaceImportRequest> importRequests = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .build();

    /**
     * <p>A mapping from UUID of the file uploaded to sever to UUID of most recent results of parsing of that file.</p>
     */
    private static final Cache<String, String> filesLatestResults = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .build();

    /**
     * <p>A mapping from UUID of results of parsing the file to UUID for the path of the file.</p>
     */
    private static final Cache<String, String> resultsFiles = CacheBuilder.newBuilder()
            .initialCapacity(5)
            .maximumSize(50)
            .expireAfterAccess(ALIVE_TIME_AMOUNT, ALIVE_TIME_UNIT)
            .build();


    /**
     * <p>Constructs new <code>PlaceImportService</code> instance. This implementation does nothing.</p>
     */
    @Inject
    public PlaceImportService(PlaceDAO placeDAO) {
        this.placeDAO = placeDAO;
    }


    /**
     * <p>Uploads the file to server. Returns a reference to path for uploaded file.</p>
     *
     * @param uploadedInputStream an input stream providing the content of the file to upload.
     * @param fileDetail the details for uploaded file.
     * @return a mapping for filename and file ID.
     * @throws IOException if an I/O error occurs while storing uploaded file on file system.
     */
    public Map<String, String> onFileUploaded(InputStream uploadedInputStream,
                                              FormDataContentDisposition fileDetail) throws IOException {

        // For some reason, the browser sends the file name in ISO_8859_1, so we use a workaround to convert
        // it to UTF_8 and enable non-ASCII characters
        // https://stackoverflow.com/questions/50582435/jersey-filename-encoded
        String fileName = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        File uploadFile = File.createTempFile(fileName, "temp");
        writeToFile(uploadedInputStream, uploadFile.getAbsolutePath());

        final ConcurrentMap<String, String> uploadedFilesPathsMap = uploadedFilesPaths.asMap();

        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (uploadedFilesPathsMap.putIfAbsent(uuid, uploadFile.getAbsolutePath()) != null);

        Map<String, String> result = new HashMap<>();

        result.put("uuid", uuid);
        result.put("fileName", fileName);

        return result;
    }


    /**
     * <p>Parses the uploaded file and analyzes the list of extracted places to be imported into DB.</p>
     *
     * @param request the parameters of place import process.
     * @return the results of parsing and analyzing the details for places to be imported.
     */
    public PlaceExcelFileParsingResult prepareImport(PlaceImportRequest request) throws IOException {
        final Function<Row, String> placeIdExtractor
                = row -> cellStringValueExtractor.apply(row.getCell(request.getPlaceIdColumnIndex() - 1));
        final Function<Row, Double> latExtractor
                = row -> cellDecimalValueExtractor.apply(row.getCell(request.getLatColumnIndex() - 1));
        final Function<Row, Double> lngExtractor
                = row -> cellDecimalValueExtractor.apply(row.getCell(request.getLngColumnIndex() - 1));
        final Function<Row, String> addressExtractor
                = row -> cellStringValueExtractor.apply(row.getCell(request.getAddressColumnIndex() - 1));

        Map<String, Integer> existingPlaces
                = this.placeDAO.getPlacesForLookup().stream().collect(Collectors.toMap(p -> p.getName().toLowerCase(), LookupItem::getId, (r1, r2) -> r1));

        // Parse file
        final String filePathId = request.getFilePathId();

        // Clean-up any previous parsing results and requests for that file as they are no longer going to be used
        // so that they do not take the memory volume anymore
        final String previousFileResultsId = filesLatestResults.getIfPresent(filePathId);
        if (previousFileResultsId != null) {
            uploadedFilesParsingResults.invalidate(previousFileResultsId);
            importRequests.invalidate(previousFileResultsId);
            resultsFiles.invalidate(previousFileResultsId);
            filesLatestResults.invalidate(filePathId);
        }

        final String filePath = uploadedFilesPaths.getIfPresent(filePathId);
        if (filePath == null) {
            throw new PlaceImportServiceException("plugin.photo.error.file.expired");
        } else {
            final File excelFile = new File(filePath);
            if (!excelFile.exists() || !excelFile.isFile()) {
                throw new PlaceImportServiceException("plugin.photo.error.file.expired");
            } else {

                final AtomicInteger newRecordsCount = new AtomicInteger(0);
                final AtomicInteger badRecordsCount = new AtomicInteger(0);
                final AtomicInteger existingRecordsCount = new AtomicInteger(0);
                final AtomicInteger duplicateRecordsCount = new AtomicInteger(0);

                final Map<String, AtomicInteger> placeIDCounts = new HashMap<>();
                final List<PlaceImportStatusItem> items = new ArrayList<>();

                try (final Workbook workbook = WorkbookFactory.create(excelFile)) {
                    final Sheet sheet = workbook.getSheetAt(0);

                    sheet.rowIterator().forEachRemaining(row -> {
                        final String placeId = placeIdExtractor.apply(row);
                        final Double lat = latExtractor.apply(row);
                        final Double lng = lngExtractor.apply(row);
                        final String address = addressExtractor.apply(row);

                        if (placeId != null) {
                            PlaceImportStatusItem item = new PlaceImportStatusItem();
                            item.setPlaceId(placeId);
                            item.setLat(lat);
                            item.setLng(lng);
                            item.setAddress(address);

                            // Validate if all required data is present
                            if (lat == null || lng == null || address == null || address.trim().isEmpty()) {
                                badRecordsCount.incrementAndGet();
                                return;
                            }

                            try {

                                final String placeIdLowerCase = placeId.toLowerCase();
                                if (!placeIDCounts.containsKey(placeIdLowerCase)) {
                                    placeIDCounts.put(placeIdLowerCase, new AtomicInteger(0));
                                }

                                item.setCount(placeIDCounts.get(placeIdLowerCase).get());

                                placeIDCounts.get(placeIdLowerCase).incrementAndGet();

                                items.add(item);

                                final Integer existingPlaceId = existingPlaces.get(placeId.toLowerCase());
                                if (existingPlaceId != null) {
                                    item.setExistingPlaceId(existingPlaceId);
                                }

                                if (item.getExistingPlaceId() == null && item.getCount() == 0) {
                                    newRecordsCount.incrementAndGet();
                                }
                                if (item.getExistingPlaceId() != null && item.getCount() == 0) {
                                    existingRecordsCount.incrementAndGet();
                                }

                                if (item.getCount() > 0) {
                                    duplicateRecordsCount.incrementAndGet();
                                }

                            } catch (SecurityException e) {
                                logger.error("Place {} belongs to a different customer", placeId);
                            } catch (Exception e) {
                                logger.error("Skipping place {} due to unexpected error", placeId, e);
                            }
                        }
                    });
                }

                // Put the parsing results to mapping for further use by next steps of place import process
                final ConcurrentMap<String, List<PlaceImportStatusItem>> uploadedFilesParsingResultsMap
                        = uploadedFilesParsingResults.asMap();
                String uuid;
                do {
                    uuid = UUID.randomUUID().toString();
                } while (uploadedFilesParsingResultsMap.putIfAbsent(uuid, items) != null);
                importRequests.put(uuid, request);

                filesLatestResults.put(filePathId, uuid);
                resultsFiles.put(uuid, filePathId);

                return new PlaceExcelFileParsingResult(uuid, items.subList(0, Math.min(10, items.size())),
                        newRecordsCount.get(), badRecordsCount.get(), newRecordsCount.get() + existingPlaces.size(),
                        existingRecordsCount.get(), duplicateRecordsCount.get());
            }
        }
    }

    /**
     * <p>Imports the places from the previously parsed Excel file to database.</p>
     *
     * @param request the parameters of places import process.
     * @return the results of places import.
     */
    @Transactional
    public PlaceImportResult importPlaces(PlaceImportConfirmRequest request) {
        final String processId = request.getUuid();
        final boolean overrideExisting = (request.getExistingMode() == 1);
        final List<PlaceImportStatusItem> parsedItems = uploadedFilesParsingResults.getIfPresent(processId);
        final PlaceImportRequest importParameters = importRequests.getIfPresent(processId);

        // Clean-up the caches and remove the temporary file uploaded to server
        try {
            uploadedFilesParsingResults.invalidate(processId);
            importRequests.invalidate(processId);

            final String filePathId = resultsFiles.getIfPresent(processId);
            resultsFiles.invalidate(processId);

            if (filePathId != null) {
                uploadedFilesPaths.invalidate(filePathId);
                filesLatestResults.invalidate(filePathId);
            }
        } catch (Exception e) {
            logger.error("Failed to clean-up the caches properly", e);
        }

        if (parsedItems == null || importParameters == null) {
            throw new PlaceImportServiceException("plugin.photo.error.file.expired");
        } else {
            if (request.isDeleteExisting()) {
                final int deletedCount = this.placeDAO.deleteAllPlaces();
                logger.debug("Deleted {} of existing place records from database", deletedCount);
            }

            AtomicInteger newPlacesCount = new AtomicInteger(0);
            AtomicInteger existingPlacesUpdated = new AtomicInteger(0);
            AtomicInteger existingPlacesSkipped = new AtomicInteger(0);
            AtomicInteger repeatedRecordsSkipped = new AtomicInteger(0);

            Set<String> processedPlaceIDs = new HashSet<>();

            Map<String, Integer> existingPlaces
                    = this.placeDAO.getPlacesForLookup().stream().collect(Collectors.toMap(p -> p.getName().toLowerCase(), LookupItem::getId, (r1, r2) -> r1));

            parsedItems.forEach(item -> {
                final String placeIdLowerCase = item.getPlaceId().toLowerCase();
                if (processedPlaceIDs.contains(placeIdLowerCase)) {
                    repeatedRecordsSkipped.incrementAndGet();
                } else {
                    processedPlaceIDs.add(placeIdLowerCase);
                    try {
                        final Integer dbPlaceId = existingPlaces.get(item.getPlaceId().toLowerCase());
                        if (dbPlaceId != null) {
                            if (!dbPlaceId.equals(item.getExistingPlaceId())) {
                                logger.warn("The place {} has changed it's unique ID in DB between the parsing of " +
                                                "Excel file and importing the places into DB." +
                                                " The ID at file parsing time was #{}, " +
                                                "the ID at place import time is #{}",
                                        item.getPlaceId(), item.getExistingPlaceId(), dbPlaceId);
                            }

                            if (overrideExisting) {
                                Place dbPlace = new Place();
                                dbPlace.setId(dbPlaceId);
                                dbPlace.setPlaceId(item.getPlaceId());
                                dbPlace.setLat(item.getLat());
                                dbPlace.setLng(item.getLng());
                                dbPlace.setAddress(item.getAddress());

                                this.placeDAO.updatePlace(dbPlace);

                                existingPlacesUpdated.incrementAndGet();
                            } else {
                                existingPlacesSkipped.incrementAndGet();
                            }
                        } else {
                            Place newPlace = new Place();
                            newPlace.setAddress(item.getAddress());
                            newPlace.setLat(item.getLat());
                            newPlace.setLng(item.getLng());
                            newPlace.setPlaceId(item.getPlaceId());

                            this.placeDAO.insertPlace(newPlace);

                            newPlacesCount.incrementAndGet();

                            logger.info("Adding new place {} during place import", newPlace.getPlaceId());
                        }
                    } catch (SecurityException e) {
                        logger.error("Place {} belongs to a different customer", item.getPlaceId());
                    } catch (PersistenceException e) {
                        logger.error("Got an SQL error when importing place {}. Terminating entire import process.",
                                item.getPlaceId(), e);
                        throw e;
                    } catch (Exception e) {
                        logger.error("Skipping importing the place {} due to unexpected error", item.getPlaceId(), e);
                    }
                }
            });

            PlaceImportResult result = new PlaceImportResult();
            result.setNewPlacesCount(newPlacesCount.get());
            result.setExistingPlacesUpdated(existingPlacesUpdated.get());
            result.setExistingPlacesSkipped(existingPlacesSkipped.get());
            result.setRepeatedRecordsSkipped(repeatedRecordsSkipped.get());

            return result;
        }
    }


    /**
     * <p>A function used for extracting the textual value of specified cell from Excel workbook sheet.</p>
     */
    private final static Function<Cell, String> cellStringValueExtractor = cell -> {
        DecimalFormat format = new DecimalFormat("#############################0.##############");
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    final double numericCellValue = cell.getNumericCellValue();
                    return format.format(numericCellValue).trim();
                case STRING:
                    final String stringCellValue = cell.getStringCellValue();
                    return stringCellValue == null ? null : stringCellValue.trim();
                case FORMULA:
                    final CellType formulaResultType = cell.getCachedFormulaResultType();
                    switch (formulaResultType) {
                        case NUMERIC:
                            final double numericCellFormulaValue = cell.getNumericCellValue();
                            return format.format(numericCellFormulaValue).trim();
                        case STRING:
                            final String stringCellFormulaValue = cell.getStringCellValue();
                            return stringCellFormulaValue == null ? null : stringCellFormulaValue.trim();
                    }
            }
        }

        return null;
    };

    /**
     * <p>A function used for extracting the decimal value of specified cell from Excel workbook sheet.</p>
     */
    private final static Function<Cell, Double> cellDecimalValueExtractor = cell -> {
        if (cell != null) {
            switch (cell.getCellType()) {
                case NUMERIC:
                    return cell.getNumericCellValue();
                case STRING:
                    final String stringCellValue = cell.getStringCellValue();
                    try {
                        return stringCellValue == null ? null : new BigDecimal(stringCellValue.trim()).doubleValue();
                    } catch (NumberFormatException e) {
                        return null;
                    }
                case FORMULA:
                    final CellType formulaResultType = cell.getCachedFormulaResultType();
                    switch (formulaResultType) {
                        case NUMERIC:
                            return cell.getNumericCellValue();
                        case STRING:
                            final String stringCellFormulaValue = cell.getStringCellValue();
                            try {
                                return stringCellFormulaValue == null ? null : new BigDecimal(stringCellFormulaValue.trim()).doubleValue();
                            } catch (NumberFormatException e) {
                                return null;
                            }
                    }
            }
        }

        return null;
    };

    /**
     * <p>Cancels the further processing of specified uploaded file. Clears all the cache entries related to specified
     * file.</p>
     *
     * @param filePathId a UUID referencing the uploaded file to cancel further processing for.
     */
    public void cancelFile(String filePathId) {
        final String resultId = filesLatestResults.getIfPresent(filePathId);
        if (resultId != null) {
            uploadedFilesParsingResults.invalidate(resultId);
            importRequests.invalidate(resultId);
            resultsFiles.invalidate(resultId);
        }
        filesLatestResults.invalidate(filePathId);
        uploadedFilesPaths.invalidate(filePathId);
    }
}

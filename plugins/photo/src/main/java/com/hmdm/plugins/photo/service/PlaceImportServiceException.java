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

/**
 * <p>A runtime exception to be thrown in case an error condition is encountered during place import process.</p>
 *
 * @author isv
 */
public class PlaceImportServiceException extends RuntimeException {

    /**
     * <p>An error code identifying the cause of exception.</p>
     */
    private final String errorCode;

    /**
     * <p>Constructs new <code>PlaceImportServiceException</code> instance. This implementation does nothing.</p>
     */
    public PlaceImportServiceException(String errorCode) {
        super("An error encountered during place import process: " + errorCode);
        this.errorCode = errorCode;
    }

    /**
     * <p>Gets the error code identifying the cause of error.</p>
     *
     * @return an error code.
     */
    public String getErrorCode() {
        return errorCode;
    }
}

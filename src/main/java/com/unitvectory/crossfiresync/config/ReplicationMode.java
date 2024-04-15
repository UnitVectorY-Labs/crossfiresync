/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.unitvectory.crossfiresync.config;

/**
 * The ReplicationMode is used to specify how replication should be performed between regions.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public enum ReplicationMode {

    /**
     * No replication is performed, indicates application is not configured.
     */
    NONE,

    /**
     * All writes must go to the primary region and changes are replicated to other regions.
     */
    SINGLE_REGION_PRIMARY,

    /**
     * Writes can go to any region and changes are replicated to other regions.
     * 
     * However, additional attributes must be added to the document for bookkeeping.
     */
    MULTI_REGION_PRIMARY,

    ;

    /**
     * Parse the ReplicationMode, but if the value is invalid NONE will be used
     * 
     * @param value the value
     * @return the ReplicationMode
     */
    public static ReplicationMode parseFallbackToNone(String value) {
        if (value == null) {
            return NONE;
        }

        try {
            return ReplicationMode.valueOf(value);
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }
}

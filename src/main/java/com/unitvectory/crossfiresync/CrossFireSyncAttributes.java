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
package com.unitvectory.crossfiresync;

import lombok.experimental.UtilityClass;

/**
 * The CrossFireSyncAttributes class.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@UtilityClass
class CrossFireSyncAttributes {

    /**
     * Name of the timestamp attribute
     */
    public static final String TIMESTAMP_FIELD = "crossfiresync:timestamp";

    /**
     * Name of the source database attribute
     */
    public static final String SOURCE_DATABASE_FIELD = "crossfiresync:sourcedatabase";

    /**
     * Name of the delete attribute
     */
    public static final String DELETE_FIELD = "crossfiresync:delete";
}

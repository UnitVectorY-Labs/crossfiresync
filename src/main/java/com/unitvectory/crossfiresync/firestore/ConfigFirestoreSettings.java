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
package com.unitvectory.crossfiresync.firestore;

import com.unitvectory.crossfiresync.FirestoreChangeConfig;
import com.unitvectory.crossfiresync.PubSubChangeConfig;
import lombok.Builder;
import lombok.Value;

/**
 * The configuration of the Firestore settings.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Value
@Builder
public class ConfigFirestoreSettings {

    /**
     * The database name.
     */
    private final String databaseName;

    /**
     * Builds the Firestore settings from the Firestore change configuration.
     * 
     * @param config the Firestore change configuration
     * @return the Firestore settings
     */
    public static ConfigFirestoreSettings build(FirestoreChangeConfig config) {
        return ConfigFirestoreSettings.builder().databaseName(config.getDatabaseName()).build();
    }

    /**
     * Builds the Firestore settings from the PubSub change configuration.
     * 
     * @param config the PubSub change configuration
     * @return the Firestore settings
     */
    public static ConfigFirestoreSettings build(PubSubChangeConfig config) {
        return ConfigFirestoreSettings.builder().databaseName(config.getDatabaseName()).build();
    }
}

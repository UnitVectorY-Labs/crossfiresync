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
package com.unitvectory.crossfiresync.pubsub;

import com.unitvectory.crossfiresync.FirestoreChangeConfig;
import lombok.Builder;
import lombok.Value;

/**
 * The configuration of the publisher settings.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Value
@Builder
public class ConfigPublisherSettings {

    /**
     * The GCP project.
     */
    private final String project;

    /**
     * The Pub/Sub topic.
     */
    private final String topic;

    /**
     * Builds the publisher settings from the Firestore change configuration.
     * 
     * @param config the Firestore change configuration
     * @return the publisher settings
     */
    public static ConfigPublisherSettings build(FirestoreChangeConfig config) {
        return ConfigPublisherSettings.builder().project(config.getProject())
                .topic(config.getTopic()).build();
    }
}

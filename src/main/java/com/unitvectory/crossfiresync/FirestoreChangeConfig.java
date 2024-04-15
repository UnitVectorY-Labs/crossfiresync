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

import com.unitvectory.crossfiresync.config.ReplicationMode;
import com.unitvectory.crossfiresync.firestore.ConfigFirestoreFactory;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherFactory;
import lombok.Builder;
import lombok.Value;

/**
 * The configuration for FirestoreChangePublisher allows for customization.
 * 
 * By default, the configuration will use the following environment variables:
 * 
 * <ul>
 * <li><b>REPLICATION_MODE</b>: The type of replication</li>
 * <li><b>DATABASE</b>: The Firestore database name</li>
 * <li><b>GOOGLE_CLOUD_PROJECT</b>: The GCP project</li>
 * <li><b>TOPIC</b>: The PubSub topic</li>
 * </ul>
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Value
@Builder
public class FirestoreChangeConfig {

    /**
     * The type of replication
     * 
     * Use the REPLICATION_MODE environment variable to set this value.
     * 
     * @see ReplicationMode
     */
    @Builder.Default
    private final ReplicationMode replicationMode =
            ReplicationMode.parseFallbackToNone(System.getenv("REPLICATION_MODE"));

    /**
     * The Firestore database name
     * 
     * Use the DATABASE environment variable to set this value.
     */
    @Builder.Default
    private final String databaseName = System.getenv("DATABASE");

    /**
     * The GCP project
     * 
     * Use the GOOGLE_CLOUD_PROJECT environment variable to set this value.
     */
    @Builder.Default
    private final String project = System.getenv("GOOGLE_CLOUD_PROJECT");

    /**
     * The PubSub topic
     * 
     * Use the TOPIC environment variable to set this value.
     */
    @Builder.Default
    private final String topic = System.getenv("TOPIC");

    /**
     * The Firestore factory
     * 
     * Implement the ConfigFirestoreFactory interface to create a custom factory to provide a
     * customized Firestore instance.
     */
    @Builder.Default
    private final ConfigFirestoreFactory firestoreFactory = new ConfigFirestoreFactoryDefault();

    /**
     * The publisher factory
     * 
     * Implement the ConfigPublisherFactory interface to create a custom factory to provide a
     * customized Publisher instance.
     */
    @Builder.Default
    private final ConfigPublisherFactory publisherFactory = new ConfigPublisherFactoryDefault();
}

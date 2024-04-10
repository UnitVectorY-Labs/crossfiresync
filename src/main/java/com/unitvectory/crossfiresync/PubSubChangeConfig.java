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

import lombok.Builder;
import lombok.Value;

/**
 * The configuration of the publisher.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Value
@Builder
public class PubSubChangeConfig {

    /**
     * The type of replication
     */
    @Builder.Default
    private final ReplicationMode replicationMode =
            ReplicationMode.parseDefaultNone(System.getenv("REPLICATION_MODE"));

    /**
     * The Firestore database name
     */
    @Builder.Default
    private final String databaseName = System.getenv("DATABASE");

    /**
     * The Firestore factory
     */
    @Builder.Default
    private final ConfigFirestoreFactory firestoreFactory = new ConfigFirestoreFactoryDefault();

}

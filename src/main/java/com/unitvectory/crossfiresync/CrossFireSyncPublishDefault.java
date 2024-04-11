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

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import lombok.AllArgsConstructor;

/**
 * The CrossFireSyncPublish Default implementation
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@AllArgsConstructor
class CrossFireSyncPublishDefault implements CrossFireSyncPublish {

    private static final Logger logger =
            Logger.getLogger(CrossFireSyncPublishDefault.class.getName());

    private final Publisher publisher;

    /**
     * Publish the message to Pub/Sub
     * 
     * @param message the message
     */
    public String publishMessage(PubsubMessage message) {
        try {
            ApiFuture<String> future = publisher.publish(message);
            publisher.publishAllOutstanding();
            String messageId = future.get();

            // Wait on the future to ensure message is sent
            logger.fine("Published message ID: " + messageId);
            return messageId;
        } catch (Exception e) {
            logger.severe("Failed to publish to Pub/Sub: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

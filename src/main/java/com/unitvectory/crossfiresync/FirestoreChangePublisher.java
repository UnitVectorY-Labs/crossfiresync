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

import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import io.cloudevents.CloudEvent;
import com.google.protobuf.ByteString;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is responsible for consuming changes from Firestore and publishing
 * them to a PubSub topic.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class FirestoreChangePublisher implements CloudEventsFunction {

    private static final Logger logger = Logger.getLogger(FirestoreChangePublisher.class.getName());

    private final String region;

    private final ProjectTopicName topicName;

    public FirestoreChangePublisher() {
        this.region = System.getenv("FUNCTION_REGION");

        String project = System.getenv("GOOGLE_CLOUD_PROJECT");

        String topic = System.getenv("TOPIC");
        if (topic == null) {
            topic = "crossfiresync";
        }

        this.topicName = ProjectTopicName.of(project, topic);
    }

    public FirestoreChangePublisher(String region, ProjectTopicName topicName) {
        this.region = region;
        this.topicName = topicName;
    }

    @Override
    public void accept(CloudEvent event) throws InvalidProtocolBufferException {

        String subject = event.getSubject();
        String[] subjectParts = subject.split("/");

        String collection = subjectParts[subjectParts.length - 2];
        String documentId = subjectParts[subjectParts.length - 1];

        // Preparing attributes for Pub/Sub message
        Map<String, String> attributes = new HashMap<>();
        attributes.put("collection", collection);
        attributes.put("documentId", documentId);
        attributes.put("region", region);

        // Prepare the message to be published
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setData(ByteString.copyFrom(event.getData().toBytes()))
                .putAllAttributes(attributes)
                .build();

        publishMessage(pubsubMessage);
    }

    private void publishMessage(PubsubMessage message) {

        Publisher publisher = null;
        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();
            publisher.publish(message).get();
            logger.info("Message published");
        } catch (Exception e) {
            logger.severe("Error publishing message: " + e.getMessage());
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                try {
                    publisher.shutdown();
                } catch (Exception e) {
                    logger.severe("Error shutting down publisher: " + e.getMessage());
                }
            }
        }
    }
}

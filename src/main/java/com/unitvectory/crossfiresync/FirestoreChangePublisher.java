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

import com.google.api.core.ApiFuture;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import io.cloudevents.CloudEvent;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * This class is responsible for consuming changes from Firestore and publishing
 * them to a PubSub topic.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class FirestoreChangePublisher implements CloudEventsFunction {

    private static final Logger logger = Logger.getLogger(FirestoreChangePublisher.class.getName());

    private final Publisher publisher;

    public FirestoreChangePublisher() {
        this(System.getenv("GOOGLE_CLOUD_PROJECT"),
                System.getenv("TOPIC"));
    }

    public FirestoreChangePublisher(String project, String topic) {
        ProjectTopicName topicName = ProjectTopicName.of(project, topic);

        try {
            publisher = Publisher.newBuilder(topicName).setEnableMessageOrdering(true).build();
        } catch (IOException e) {
            logger.severe("Failed to create publisher: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(CloudEvent event) throws InvalidProtocolBufferException {

        // Parse the Firestore data
        DocumentEventData firestoreEventData = DocumentEventData.parseFrom(event.getData().toBytes());

        // Get the resource name for the document for insert/update/delete
        String resourceName = null;
        if (firestoreEventData.hasValue()) {
            resourceName = firestoreEventData.getValue().getName();
        } else if (firestoreEventData.hasOldValue()) {
            resourceName = firestoreEventData.getOldValue().getName();
        }

        // Invalid input, no resource name means cannot process
        if (resourceName == null) {
            logger.warning("resourceName is null");
            return;
        }

        // Extract the database name
        String database = DocumentResourceNameUtil.getDatabaseId(resourceName);
        if (database == null) {
            logger.warning("extracted database is null");
            return;
        }

        // Extract the document path
        String documentPath = DocumentResourceNameUtil.getDocumentPath(resourceName);
        if (documentPath == null) {
            logger.warning("extracted documentPath is null");
            return;
        }

        // Preparing attributes for Pub/Sub message
        Map<String, String> attributes = new HashMap<>();
        attributes.put("database", database);

        // Prepare the message to be published
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder()
                .setOrderingKey(documentPath)
                .setData(ByteString.copyFrom(event.getData().toBytes()))
                .putAllAttributes(attributes)
                .build();

        // Publish the message
        publishMessage(pubsubMessage);
    }

    private void publishMessage(PubsubMessage message) {
        try {
            ApiFuture<String> future = publisher.publish(message);
            publisher.publishAllOutstanding();
            String messageId = future.get();

            // Wait on the future to ensure message is sent
            logger.fine("Published message ID: " + messageId);
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Failed to publish to Pub/Sub: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

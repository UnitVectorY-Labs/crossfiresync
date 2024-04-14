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
import com.google.events.cloud.firestore.v1.Document;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.events.cloud.firestore.v1.Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.PubsubMessage;

import io.cloudevents.CloudEvent;
import lombok.NonNull;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is responsible for consuming changes from Firestore and publishing them to a PubSub
 * topic.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@SuppressWarnings("null")
public class FirestoreChangePublisher implements CloudEventsFunction {

    private static final Logger logger = Logger.getLogger(FirestoreChangePublisher.class.getName());

    private final String database;

    private final CrossFireSyncFirestore firestore;

    private final CrossFireSyncPublish publisher;

    /**
     * Create a new FirestoreChangePublisher.
     */
    public FirestoreChangePublisher() {
        this(FirestoreChangeConfig.builder().build());
    }

    /**
     * Create a new FirestoreChangePublisher.
     * 
     * @param config the configuration
     */
    public FirestoreChangePublisher(@NonNull FirestoreChangeConfig config) {
        this.database = config.getDatabaseName();

        this.firestore =
                config.getFirestoreFactory().getFirestore(ConfigFirestoreSettings.build(config));

        try {
            this.publisher = config.getPublisherFactory()
                    .getPublisher(ConfigPublisherSettings.build(config));
        } catch (IOException e) {
            logger.severe("Failed to create Publisher: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void accept(CloudEvent event) throws InvalidProtocolBufferException {

        // Parse the Firestore data
        DocumentEventData firestoreEventData =
                DocumentEventData.parseFrom(event.getData().toBytes());

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

        // Check to see if this is a delete
        if (firestoreEventData.hasValue() && firestoreEventData.getValue()
                .containsFields(CrossFireSyncAttributes.DELETE_FIELD)) {
            // The delete field being present is the signal to delete the record in the
            // local region without publishing to the PubSub topic.
            this.firestore.deleteDocument(documentPath);
            return;
        }

        // Check to see if the record should be replicated
        if (!shouldReplicate(firestoreEventData)) {
            logger.fine("Skipping " + documentPath);
            return;
        }

        // Preparing attributes for Pub/Sub message
        Map<String, String> attributes = new HashMap<>();
        attributes.put("database", database);

        // Prepare the message to be published
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setOrderingKey(documentPath)
                .setData(ByteString.copyFrom(event.getData().toBytes()))
                .putAllAttributes(attributes).build();

        // Publish the message
        this.publisher.publishMessage(pubsubMessage);
    }

    /**
     * Test if the change to the firestore record should be replicated to Pub/Sub.
     * 
     * @param firestoreEventData the document
     * @return true if record should replicate, otherwise false
     */
    boolean shouldReplicate(DocumentEventData firestoreEventData) {

        if (firestoreEventData.hasValue()) {
            if (!firestoreEventData.hasOldValue()) {
                // Insert
                Document document = firestoreEventData.getValue();

                // Inserted without the database replication field, replicate it
                Value sourceValue = document
                        .getFieldsOrDefault(CrossFireSyncAttributes.SOURCE_DATABASE_FIELD, null);
                if (sourceValue == null || !sourceValue.hasStringValue()) {
                    return true;
                }

                // Inserted without the timestamp replication field, replicate it
                Value timestampValue =
                        document.getFieldsOrDefault(CrossFireSyncAttributes.TIMESTAMP_FIELD, null);
                if (timestampValue == null || !timestampValue.hasTimestampValue()) {
                    return true;
                }

                // If the source database happens to match the local database region then
                // replicate the record, otherwise don't replicate
                return this.database.equals(sourceValue.getStringValue());
            } else {
                // Update

                // There is no database replication field in the update, replicate it
                // It definitely want a replicated record
                Value newDatabase = firestoreEventData.getValue()
                        .getFieldsOrDefault(CrossFireSyncAttributes.SOURCE_DATABASE_FIELD, null);
                if (newDatabase == null || !newDatabase.hasStringValue()) {
                    return true;
                }

                // There is no new timestamp replication field in the update, replicate it
                // It definitely want a replicated record
                Value newTimestamp = firestoreEventData.getValue()
                        .getFieldsOrDefault(CrossFireSyncAttributes.TIMESTAMP_FIELD, null);
                if (newTimestamp == null || !newTimestamp.hasTimestampValue()) {
                    return true;
                }

                Timestamp newTs = newTimestamp.getTimestampValue();

                Value oldTimestamp = firestoreEventData.getOldValue()
                        .getFieldsOrDefault(CrossFireSyncAttributes.TIMESTAMP_FIELD, null);
                Timestamp oldTs = null;
                if (oldTimestamp == null || !oldTimestamp.hasTimestampValue()) {
                    // There is a new timestamp (previous check) but there was no old timestamp this
                    // was a replicated record that was updated so do not replicate again
                    return false;
                } else {
                    oldTs = oldTimestamp.getTimestampValue();
                }

                // The record was updated but the timestamps are the same, this means a user
                // updated the record so we replicate it out
                return newTs.equals(oldTs);
            }
        } else {
            // Delete

            // Skip field that have the delete field set, these are not replicated to other
            // regions; only deleting in local region
            return !firestoreEventData.getOldValue()
                    .containsFields(CrossFireSyncAttributes.DELETE_FIELD);
        }
    }
}

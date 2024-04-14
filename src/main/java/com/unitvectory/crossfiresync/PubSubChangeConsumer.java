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

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.cloud.firestore.v1.Document;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.unitvectory.firestoreproto2map.FirestoreProto2Map;
import io.cloudevents.CloudEvent;
import lombok.NonNull;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is tasked with receiving changes from a PubSub topic and writing them to Firestore in
 * another region.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@SuppressWarnings("null")
public class PubSubChangeConsumer implements CloudEventsFunction {

    private static final Logger logger = Logger.getLogger(PubSubChangeConsumer.class.getName());

    private static final Gson gson = new Gson();

    private final String database;

    private final CrossFireSyncFirestore firestore;

    private final FirestoreProto2Map firestoreProto2Map;

    /**
     * Create a new PubSubChangeConsumer.
     */
    public PubSubChangeConsumer() {
        this(PubSubChangeConfig.builder().build());
    }

    /**
     * Create a new PubSubChangeConsumer.
     * 
     * @param config The configuration for the consumer
     */
    public PubSubChangeConsumer(@NonNull PubSubChangeConfig config) {
        this.database = config.getDatabaseName();
        this.firestore =
                config.getFirestoreFactory().getFirestore(ConfigFirestoreSettings.build(config));
        this.firestoreProto2Map = new FirestoreProto2Map(this.firestore);
    }

    @Override
    public void accept(CloudEvent event) throws InvalidProtocolBufferException {

        // Parse the payload
        String cloudEventData = new String(event.getData().toBytes());
        PubSubPublish data = gson.fromJson(cloudEventData, PubSubPublish.class);

        String pubsubDatabase = data.getMessage().getAttribute("database");

        // Do not process updates when database change is for the same region
        if (pubsubDatabase == null) {
            logger.info("PubSub message missing 'database' attribute");
            return;
        } else if (this.database.equals(pubsubDatabase)) {
            logger.info("Same database " + this.database + " skipping");
            return;
        }

        // Parse the Firestore Document change
        DocumentEventData firestoreEventData = DocumentEventData
                .parseFrom(Base64.getDecoder().decode(data.getMessage().getData()));

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

        // Get the document path for the local database
        String documentPath = DocumentResourceNameUtil.getDocumentPath(resourceName);
        if (documentPath == null) {
            logger.warning("documentPath is null");
            return;
        }

        DocumentReference documentReference = this.firestore.getDocument(documentPath);
        if (firestoreEventData.hasValue()) {
            // Perform the update

            Document document = firestoreEventData.getValue();
            Map<String, Object> record = this.firestoreProto2Map.convert(document);

            Timestamp updatedTime = Timestamp.fromProto(document.getUpdateTime());

            // For cross region replication to work properly two additional attributes must
            // be written to the document to indicate what region the replicated attribute
            // came from and the timestamp of when the record was updated
            record.put(CrossFireSyncAttributes.TIMESTAMP_FIELD, updatedTime);
            record.put(CrossFireSyncAttributes.SOURCE_DATABASE_FIELD, pubsubDatabase);

            // Perform the update
            this.firestore.updateTransaction(documentReference, updatedTime, record);

            // documentReference.set().get();
            logger.info("Document set: " + documentPath);
        } else {
            // Flag as delete

            // NOTE: Ideally this should be the timestamp of the delete not the current time, but
            // the protocol buffer for deletes do not have the delete timestamp
            Timestamp deleteTimestamp = this.firestore.now();

            // Prepare the updates, set the deleted flag instead of actually deleting so the
            // delete in the remote regions will not redundantly cascade to other regions.
            Map<String, Object> updates = new HashMap<>();
            updates.put(CrossFireSyncAttributes.TIMESTAMP_FIELD, deleteTimestamp);
            updates.put(CrossFireSyncAttributes.DELETE_FIELD, true);
            updates.put(CrossFireSyncAttributes.SOURCE_DATABASE_FIELD, pubsubDatabase);

            boolean flagged = this.firestore.deleteFlagTransaction(documentReference, updates);

            if (flagged) {
                logger.info("Document deleted: " + documentPath);
            }
        }

        logger.info("Pub/Sub message: " + event);
    }

}

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

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.cloud.firestore.v1.Document;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;

import io.cloudevents.CloudEvent;
import lombok.NonNull;

import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * This class is tasked with receiving changes from a PubSub topic and writing
 * them to Firestore in another region.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@SuppressWarnings("null")
public class PubSubChangeConsumer implements CloudEventsFunction {

    private static final Logger logger = Logger.getLogger(PubSubChangeConsumer.class.getName());

    private static final Gson gson = new Gson();

    private final String database;

    private final Firestore db;

    public PubSubChangeConsumer() {
        this(System.getenv("DATABASE"));
    }

    public PubSubChangeConsumer(@NonNull String database) {
        this.database = database;
        this.db = FirestoreOptions.newBuilder()
                .setDatabaseId(database)
                .build().getService();
    }

    public PubSubChangeConsumer(@NonNull Firestore db, @NonNull String database) {
        this.db = db;
        this.database = database;
    }

    @Override
    public void accept(CloudEvent event) throws InvalidProtocolBufferException {

        // Parse the payload
        String cloudEventData = new String(event.getData().toBytes());
        PubSubPublish data = gson.fromJson(cloudEventData, PubSubPublish.class);

        String pubsubDatabase = data.getMessage().getAttribute("database");

        // Do not process updates when database change is for the same region
        if (this.database.equals(pubsubDatabase)) {
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

        DocumentReference documentReference = this.db.document(documentPath);
        try {
            if (firestoreEventData.hasValue()) {
                Document document = firestoreEventData.getValue();
                documentReference.set(FirestoreDocumentConverter.convert(db, document)).get();
                logger.info("Document set: " + documentPath);
            } else {
                documentReference.delete().get();
                logger.info("Document deleted: " + documentPath);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Failed: " + e.getMessage());
        }

        logger.info("Pub/Sub message: " + event);
    }
}

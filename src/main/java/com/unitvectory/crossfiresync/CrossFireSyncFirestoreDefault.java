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

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import com.unitvectory.crossfiresync.exception.CrossFireSyncException;
import com.unitvectory.crossfiresync.firestore.CrossFireSyncFirestore;
import lombok.AllArgsConstructor;

/**
 * The CrossFireSyncFirestore Default implementation.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@AllArgsConstructor
class CrossFireSyncFirestoreDefault implements CrossFireSyncFirestore {

    private static final Logger logger =
            Logger.getLogger(CrossFireSyncFirestoreDefault.class.getName());

    private final Firestore db;

    @Override
    public DocumentReference getDocument(String documentPath) {
        try {
            return this.db.document(documentPath);
        } catch (Exception e) {
            logger.severe("Failed to get document: " + documentPath);
            throw new CrossFireSyncException("Failed to get document.", e);
        }
    }

    @Override
    public Timestamp now() {
        return Timestamp.now();
    }

    @Override
    public void updateTransaction(DocumentReference documentReference, Timestamp updatedTime,
            Map<String, Object> record) {
        ApiFuture<Void> transaction = db.runTransaction((Transaction.Function<Void>) t -> {
            // Attempt to retrieve the existing document
            DocumentSnapshot snapshot = t.get(documentReference).get();

            boolean shouldWrite = false;
            if (!snapshot.exists()) {
                // Document does not exist
                shouldWrite = true;
            } else {
                // Check if the timestamp is older or not present
                Timestamp existingTimestamp =
                        snapshot.contains(CrossFireSyncAttributes.TIMESTAMP_FIELD)
                                ? snapshot.getTimestamp(CrossFireSyncAttributes.TIMESTAMP_FIELD)
                                : null;
                if (existingTimestamp == null || updatedTime.compareTo(existingTimestamp) > 0) {
                    shouldWrite = true;
                }
            }

            // If conditions are met, proceed to write
            if (shouldWrite) {
                t.set(documentReference, record);
            }

            return null; // Transaction must return null if void
        });

        // Wait for the transaction to complete
        try {
            transaction.get();
        } catch (Exception e) {
            logger.severe("Failed to update transaction: " + documentReference.getPath());
            throw new CrossFireSyncException("Failed to update transaction.", e);
        }
    }

    @Override
    public boolean deleteFlagTransaction(DocumentReference documentReference,
            Map<String, Object> updates) {
        ApiFuture<Boolean> transaction = db.runTransaction((Transaction.Function<Boolean>) t -> {
            // Attempt to retrieve the existing document
            DocumentSnapshot snapshot = t.get(documentReference).get();

            // If the document exists flag it for delete
            if (snapshot.exists()) {
                t.update(documentReference, updates);
                return true;
            } else {
                return false;
            }
        });

        // Wait for the transaction to complete
        try {
            return transaction.get();
        } catch (Exception e) {
            logger.severe("Failed to delete flag transaction: " + documentReference.getPath());
            throw new CrossFireSyncException("Failed to delete flag transaction.", e);
        }
    }

    @Override
    public void deleteDocument(String documentPath) {
        DocumentReference documentReference = this.db.document(documentPath);
        try {
            documentReference.delete().get();
            logger.info("Deleted: "
                    + DocumentResourceNameUtil.getDocumentPath(documentReference.getId()));
        } catch (InterruptedException | ExecutionException e) {
            logger.severe("Failed to delete document: " + documentPath);
            throw new CrossFireSyncException("Failed to delete document.", e);
        }
    }
}

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
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.unitvectory.firestoreproto2map.ValueToDocumentReferenceMapper;

/**
 * The CrossFireSyncFirestore interface
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public interface CrossFireSyncFirestore extends ValueToDocumentReferenceMapper {

    /**
     * Gets the current time
     * 
     * @return the current time
     */
    Timestamp now();

    /**
     * Gets the document reference
     * 
     * @param documentPath the document path
     * @return the document reference
     */
    DocumentReference getDocument(String documentPath);

    /**
     * Flag a Firestore document for deletion with a transaction.
     * 
     * @param documentReference The document reference
     * @param updates The updates to apply
     * @return True if the document was flagged for deletion
     * @throws InterruptedException If the transaction is interrupted
     * @throws ExecutionException If the transaction fails
     */
    boolean deleteFlagTransaction(DocumentReference documentReference, Map<String, Object> updates)
            throws InterruptedException, ExecutionException;

    /**
     * Update a Firestore document with a transaction.
     * 
     * @param documentReference The document reference
     * @param updatedTime The updated time
     * @param record The record to update
     * @throws InterruptedException If the transaction is interrupted
     * @throws ExecutionException If the transaction fails
     */
    void updateTransaction(DocumentReference documentReference, Timestamp updatedTime,
            Map<String, Object> record) throws InterruptedException, ExecutionException;

    /**
     * Deletes the document
     * 
     * @param documentPath the document to delete
     */
    void deleteDocument(String documentPath);

    default DocumentReference convert(String referenceValue, String documentPath) {
        return getDocument(documentPath);
    }
}

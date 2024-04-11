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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.ExecutionException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import com.google.cloud.firestore.WriteResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * The CrossFireSyncFirestore Default implementation tests.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class CrossFireSyncFirestoreDefaultTest {

    @Mock
    private Firestore firestore;

    @Mock
    private DocumentReference documentReference;

    @Mock
    private Transaction transaction;

    private CrossFireSyncFirestoreDefault firestoreDefault;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        firestoreDefault = new CrossFireSyncFirestoreDefault(firestore);
    }

    @Test
    public void testGetDocument() {
        String documentPath = "path/to/document";
        when(firestore.document(documentPath)).thenReturn(documentReference);

        DocumentReference result = firestoreDefault.getDocument(documentPath);

        assertEquals(documentReference, result);
    }

    @Test
    public void testNow() {
        com.google.cloud.Timestamp now = firestoreDefault.now();
        assertNotNull(now);
    }

    @Test
    public void testDeleteDocument() throws InterruptedException, ExecutionException {
        String documentPath = "path/to/document";

        when(documentReference.getId()).thenReturn(documentPath);

        when(firestore.document(documentPath)).thenReturn(documentReference);

        @SuppressWarnings("unchecked")
        ApiFuture<WriteResult> apiFuture = mock(ApiFuture.class);
        when(documentReference.delete()).thenReturn(apiFuture);

        WriteResult writeResult = mock(WriteResult.class);
        when(apiFuture.get()).thenReturn(writeResult);

        firestoreDefault.deleteDocument(documentPath);

        verify(documentReference).delete();
    }
}

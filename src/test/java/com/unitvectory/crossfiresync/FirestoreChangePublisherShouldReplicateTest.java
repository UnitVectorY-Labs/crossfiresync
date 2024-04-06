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

import java.util.Base64;

import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mockito;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.unitvectory.fileparamunit.ListFileSource;
import com.unitvectory.jsonparamunit.JsonClassParamUnit;

/**
 * The FirestoreChangePublisher utility class test for shouldReplicate.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class FirestoreChangePublisherShouldReplicateTest extends JsonClassParamUnit<TestRecord, TestRecord> {

    private FirestoreChangePublisher firestoreChangePublisher;

    protected FirestoreChangePublisherShouldReplicateTest() {
        super(TestRecord.class);

        Publisher publisher = Mockito.mock(Publisher.class);
        Firestore db = Mockito.mock(Firestore.class);
        this.firestoreChangePublisher = new FirestoreChangePublisher(publisher, db, "test");
    }

    @ParameterizedTest
    @ListFileSource(resources = "/shouldReplicate/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @Override
    protected TestRecord process(TestRecord input, String context) {
        try {
            DocumentEventData firestoreEventData = DocumentEventData
                    .parseFrom(Base64.getDecoder().decode(input.getValue()));
            boolean shouldReplicate = firestoreChangePublisher.shouldReplicate(firestoreEventData);
            return TestRecord.builder().value(Boolean.toString(shouldReplicate)).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

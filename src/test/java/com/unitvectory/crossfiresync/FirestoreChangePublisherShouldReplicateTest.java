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
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.events.cloud.firestore.v1.DocumentEventData;
import com.unitvectory.fileparamunit.ListFileSource;
import com.unitvectory.jsonparamunit.JsonNodeParamUnit;

/**
 * The FirestoreChangePublisher utility class test for shouldReplicate.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class FirestoreChangePublisherShouldReplicateTest extends JsonNodeParamUnit {

    private static final ObjectMapper mapper = new ObjectMapper();

    private Firestore db;

    private FirestoreChangePublisher firestoreChangePublisher;

    protected FirestoreChangePublisherShouldReplicateTest() {
        super();

        Publisher publisher = Mockito.mock(Publisher.class);
        this.db = Mockito.mock(Firestore.class);
        this.firestoreChangePublisher = new FirestoreChangePublisher(publisher, db, "test");
    }

    @ParameterizedTest
    @ListFileSource(resources = "/shouldReplicate/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @Override
    protected JsonNode process(JsonNode input, String context) {

        try {
            DocumentEventData firestoreEventData = DocumentEventData
                    .parseFrom(Base64.getDecoder().decode(input.asText()));
            boolean shouldReplicate = firestoreChangePublisher.shouldReplicate(firestoreEventData);

            ObjectNode output = mapper.createObjectNode();
            output.put("shouldReplicate", shouldReplicate);

            if (firestoreEventData.hasOldValue()) {
                Map<String, Object> oldValue = FirestoreDocumentConverter.convert(db, firestoreEventData.getOldValue());
                output.putPOJO("oldValue", oldValue);
            } else {
                output.putNull("oldValue");
            }

            if(firestoreEventData.hasValue()){
                Map<String, Object> value = FirestoreDocumentConverter.convert(db, firestoreEventData.getValue());
                output.putPOJO("value", value);
            } else {
                output.putNull("value");
            }

            return output;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

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
import com.unitvectory.crossfiresync.config.ReplicationMode;
import com.unitvectory.crossfiresync.firestore.ConfigFirestoreFactory;
import com.unitvectory.crossfiresync.firestore.ConfigFirestoreSettings;
import com.unitvectory.crossfiresync.firestore.CrossFireSyncFirestore;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherFactory;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherSettings;
import com.unitvectory.crossfiresync.pubsub.CrossFireSyncPublish;
import com.unitvectory.fileparamunit.ListFileSource;
import com.unitvectory.firestoreproto2map.FirestoreProto2Map;
import com.unitvectory.jsonparamunit.JsonNodeParamUnit;

/**
 * The FirestoreChangePublisher utility class test for shouldReplicate.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class FirestoreChangePublisherShouldReplicateTest extends JsonNodeParamUnit {

    private static final ObjectMapper mapper = new ObjectMapper();

    private CrossFireSyncFirestore firestore;

    private FirestoreChangePublisher firestoreChangePublisher;

    private final FirestoreProto2Map firestoreProto2Map;

    protected FirestoreChangePublisherShouldReplicateTest() {
        super();

        String databaseName = "test";
        Publisher publisher = Mockito.mock(Publisher.class);
        Firestore db = Mockito.mock(Firestore.class);
        this.firestore = new CrossFireSyncFirestoreDefault(db);
        this.firestoreChangePublisher = new FirestoreChangePublisher(FirestoreChangeConfig.builder()
                .replicationMode(ReplicationMode.MULTI_REGION_PRIMARY).databaseName(databaseName)
                .firestoreFactory(new ConfigFirestoreFactory() {
                    @Override
                    public CrossFireSyncFirestore getFirestore(ConfigFirestoreSettings settings) {
                        return firestore;
                    }

                }).publisherFactory(new ConfigPublisherFactory() {
                    @Override
                    public CrossFireSyncPublish getPublisher(ConfigPublisherSettings settings) {
                        return new CrossFireSyncPublishDefault(publisher);
                    }

                }).build());
        this.firestoreProto2Map = new FirestoreProto2Map(this.firestore);
    }

    @ParameterizedTest
    @ListFileSource(resources = "/shouldReplicate/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @Override
    protected JsonNode process(JsonNode input, String context) {

        try {
            DocumentEventData firestoreEventData =
                    DocumentEventData.parseFrom(Base64.getDecoder().decode(input.asText()));
            boolean shouldReplicate = firestoreChangePublisher.shouldReplicate(firestoreEventData);

            ObjectNode output = mapper.createObjectNode();
            output.put("shouldReplicate", shouldReplicate);

            if (firestoreEventData.hasOldValue()) {
                Map<String, Object> oldValue =
                        this.firestoreProto2Map.convert(firestoreEventData.getOldValue());
                output.putPOJO("oldValue", oldValue);
            } else {
                output.putNull("oldValue");
            }

            if (firestoreEventData.hasValue()) {
                Map<String, Object> value =
                        this.firestoreProto2Map.convert(firestoreEventData.getValue());
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

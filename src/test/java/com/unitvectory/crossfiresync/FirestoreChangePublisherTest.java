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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Map.Entry;

import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.unitvectory.crossfiresync.config.ReplicationMode;
import com.unitvectory.crossfiresync.firestore.ConfigFirestoreFactory;
import com.unitvectory.crossfiresync.firestore.ConfigFirestoreSettings;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherFactory;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherSettings;
import com.unitvectory.crossfiresync.pubsub.CrossFireSyncPublish;
import com.unitvectory.fileparamunit.ListFileSource;
import com.unitvectory.jsonparamunit.JsonNodeParamUnit;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;

/**
 * FirestoreChangePublisherTest
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class FirestoreChangePublisherTest extends JsonNodeParamUnit {

    private static final ObjectMapper mapper = new ObjectMapper();

    protected FirestoreChangePublisherTest() {
        super();
    }

    @ParameterizedTest
    @ListFileSource(resources = "/publisher/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @SuppressWarnings("null")
    @Override
    protected JsonNode process(JsonNode input, String context) {

        try {

            ReplicationMode replicationMode =
                    ReplicationMode.valueOf(input.get("replicationMode").asText());

            Publisher publisher = Mockito.mock(Publisher.class);

            CrossFireSyncPublish publish = spy(new CrossFireSyncPublishDefault(publisher));

            @SuppressWarnings("unchecked")
            ApiFuture<String> apiFuture = Mockito.mock(ApiFuture.class);
            when(apiFuture.get()).thenReturn("id");

            ArgumentCaptor<PubsubMessage> pubsubCaptor =
                    ArgumentCaptor.forClass(PubsubMessage.class);
            when(publisher.publish(pubsubCaptor.capture())).thenReturn(apiFuture);

            Firestore db = Mockito.mock(Firestore.class);
            CrossFireSyncFirestoreDefault firestore = spy(new CrossFireSyncFirestoreDefault(db));

            when(db.document(anyString())).thenAnswer(new Answer<DocumentReference>() {
                @Override
                public DocumentReference answer(InvocationOnMock invocation) throws Throwable {
                    // Capture the input string
                    String inputString = invocation.getArgument(0);

                    // Mock DocumentReference
                    DocumentReference mockDocumentRef = Mockito.mock(DocumentReference.class);
                    // Configure mock to return the input string as ID
                    when(mockDocumentRef.getId()).thenReturn(
                            "projects/example/databases/" + context + "/documents/" + inputString);

                    return mockDocumentRef;
                }
            });

            FirestoreChangePublisher firestoreChangePublisher = spy(new FirestoreChangePublisher(
                    FirestoreChangeConfig.builder().replicationMode(replicationMode)
                            .databaseName(context).firestoreFactory(new ConfigFirestoreFactory() {
                                @Override
                                public CrossFireSyncFirestoreDefault getFirestore(
                                        ConfigFirestoreSettings settings) {
                                    return firestore;
                                }

                            }).publisherFactory(new ConfigPublisherFactory() {
                                @Override
                                public CrossFireSyncPublish getPublisher(
                                        ConfigPublisherSettings settings) {
                                    return publish;
                                }

                            }).build()));

            // Capture the delete document
            ArgumentCaptor<String> deleteDocumentCaptor = ArgumentCaptor.forClass(String.class);

            doNothing().when(firestore).deleteDocument(deleteDocumentCaptor.capture());

            byte[] inputBytes = Base64.getDecoder().decode(input.get("protobuf").asText());

            CloudEvent cloudEvent = Mockito.mock(CloudEvent.class);
            CloudEventData cloudEventData = Mockito.mock(CloudEventData.class);
            when(cloudEvent.getData()).thenAnswer(c -> cloudEventData);
            when(cloudEventData.toBytes()).thenAnswer(c -> inputBytes);

            firestoreChangePublisher.accept(cloudEvent);

            ObjectNode output = mapper.createObjectNode();

            try {
                PubsubMessage pubsubMessage = pubsubCaptor.getValue();

                ObjectNode publishNode = mapper.createObjectNode();

                publishNode.put("data",
                        Base64.getEncoder().encodeToString(pubsubMessage.getData().toByteArray()));

                publishNode.put("orderingKey", pubsubMessage.getOrderingKey());

                ObjectNode attributes = mapper.createObjectNode();
                for (Entry<String, String> att : pubsubMessage.getAttributesMap().entrySet()) {
                    attributes.put(att.getKey(), att.getValue());
                }

                publishNode.set("attributes", attributes);

                output.set("publish", publishNode);

            } catch (MockitoException e) {
                // If there is no publish method
            }

            try {
                String deleteDocument = deleteDocumentCaptor.getValue();
                output.put("deleteDocument", deleteDocument);
            } catch (MockitoException e) {
                // If there is no update method called
            }

            return output;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

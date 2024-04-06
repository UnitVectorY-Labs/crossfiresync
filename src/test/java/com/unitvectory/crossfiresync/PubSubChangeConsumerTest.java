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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.Firestore;
import com.unitvectory.fileparamunit.ListFileSource;
import com.unitvectory.jsonparamunit.JsonNodeParamUnit;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;

/**
 * PubSubChangeConsumerTest
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class PubSubChangeConsumerTest extends JsonNodeParamUnit {

    private static final ObjectMapper mapper = new ObjectMapper();

    public PubSubChangeConsumerTest() {
        super();
    }

    @ParameterizedTest
    @ListFileSource(resources = "/consumer/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @Override
    protected JsonNode process(JsonNode input, String context) {
        try {
            Firestore db = Mockito.mock(Firestore.class);
            PubSubChangeConsumer pubSubChangeConsumer = spy(new PubSubChangeConsumer(db, context));

            // Prevent the actual Firestore transaction from being performed
            doNothing().when(pubSubChangeConsumer).updateTransaction(any(), any(), any());

            // Needing to capture the update argment to use as part of the test case output
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Map<String, Object>> updateCaptor = ArgumentCaptor.forClass(Map.class);

            doNothing().when(pubSubChangeConsumer).updateTransaction(any(), any(), updateCaptor.capture());

            byte[] inputBytes = mapper.writeValueAsString(input).getBytes();

            CloudEvent cloudEvent = Mockito.mock(CloudEvent.class);
            CloudEventData cloudEventData = Mockito.mock(CloudEventData.class);
            when(cloudEvent.getData()).thenAnswer(c -> cloudEventData);
            when(cloudEventData.toBytes()).thenAnswer(c -> inputBytes);

            pubSubChangeConsumer.accept(cloudEvent);

            ObjectNode output = mapper.createObjectNode();

            try {
                Map<String, Object> update = updateCaptor.getValue();
                output.putPOJO("update", update);
            } catch (MockitoException e) {
                // If there is no update method called
            }

            return output;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}

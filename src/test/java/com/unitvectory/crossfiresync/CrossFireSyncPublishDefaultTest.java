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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.google.api.core.ApiFuture;
import com.google.pubsub.v1.PubsubMessage;
import com.google.cloud.pubsub.v1.Publisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;

/**
 * The CrossFireSyncPublish Default implementation tests.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class CrossFireSyncPublishDefaultTest {

    @Mock
    private Publisher mockedPublisher;

    private CrossFireSyncPublishDefault crossFireSyncPublishDefault;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        crossFireSyncPublishDefault = new CrossFireSyncPublishDefault(mockedPublisher);
    }

    @Test
    public void testPublishMessage() throws InterruptedException, ExecutionException {
        // Arrange
        PubsubMessage message = PubsubMessage.newBuilder().build();

        @SuppressWarnings("unchecked")
        ApiFuture<String> future = mock(ApiFuture.class);
        String messageId = "messageId";

        when(mockedPublisher.publish(message)).thenReturn(future);
        when(future.get()).thenReturn(messageId);

        String actualMessageId = crossFireSyncPublishDefault.publishMessage(message);

        verify(mockedPublisher).publish(message);
        verify(mockedPublisher).publishAllOutstanding();
        verify(future).get();
        assertEquals(messageId, actualMessageId);
    }

    @Test
    public void testPublishMessageWithExecutionException()
            throws InterruptedException, ExecutionException {
        // Arrange
        PubsubMessage message = PubsubMessage.newBuilder().build();
        @SuppressWarnings("unchecked")
        ApiFuture<String> future = mock(ApiFuture.class);
        ExecutionException exception =
                new ExecutionException("Error occurred during publishing", new RuntimeException());
        when(mockedPublisher.publish(message)).thenReturn(future);
        when(future.get()).thenThrow(exception);

        // Act and Assert
        assertThrows(RuntimeException.class, () -> {
            crossFireSyncPublishDefault.publishMessage(message);
        });

        verify(mockedPublisher).publish(message);
        verify(mockedPublisher).publishAllOutstanding();
        verify(future).get();
    }

}

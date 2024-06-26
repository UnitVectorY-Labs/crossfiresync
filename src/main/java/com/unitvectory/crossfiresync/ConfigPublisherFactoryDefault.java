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

import java.io.IOException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherFactory;
import com.unitvectory.crossfiresync.pubsub.ConfigPublisherSettings;
import com.unitvectory.crossfiresync.pubsub.CrossFireSyncPublish;

/**
 * The default factory for the Publisher configuration.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
class ConfigPublisherFactoryDefault implements ConfigPublisherFactory {

    @Override
    public CrossFireSyncPublish getPublisher(ConfigPublisherSettings settings) throws IOException {
        ProjectTopicName topicName =
                ProjectTopicName.of(settings.getProject(), settings.getTopic());
        Publisher publish = Publisher.newBuilder(topicName).setEnableMessageOrdering(true).build();
        return new CrossFireSyncPublishDefault(publish);
    }
}

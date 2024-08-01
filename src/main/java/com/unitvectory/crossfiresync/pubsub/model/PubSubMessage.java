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
package com.unitvectory.crossfiresync.pubsub.model;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The PubSubMessage class.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@Data
@NoArgsConstructor
public class PubSubMessage {

    private Map<String, String> attributes;

    private String data;

    private String messageId;

    private String orderingKey;

    private String publishTime;

    /**
     * Gets the attribute value.
     * 
     * @param name the attribute name
     * @return the attribute value
     */
    public String getAttribute(String name) {
        if (this.attributes == null) {
            return null;
        }

        return this.attributes.get(name);
    }
}

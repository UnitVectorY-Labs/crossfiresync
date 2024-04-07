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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.cloud.firestore.DocumentReference;

/**
 * Custom serializer to convert a DocumentReference directly into the id String.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class DocumentReferenceSerializer extends StdSerializer<DocumentReference> {

    public DocumentReferenceSerializer() {
        super(DocumentReference.class);
    }

    @Override
    public void serialize(DocumentReference value, JsonGenerator jsonGenerator, SerializerProvider serializers)
            throws IOException {
        jsonGenerator.writeString(value.getId());
    }
}

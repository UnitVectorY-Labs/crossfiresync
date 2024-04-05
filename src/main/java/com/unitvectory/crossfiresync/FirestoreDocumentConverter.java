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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.GeoPoint;
import com.google.events.cloud.firestore.v1.Document;
import com.google.events.cloud.firestore.v1.Value;
import com.google.type.LatLng;

import lombok.experimental.UtilityClass;

/**
 * Utility to convert the Protocol Buffere Firestore Document from eventarc to a
 * represenatation that can be written to Firestore.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
@UtilityClass
@SuppressWarnings("null")
class FirestoreDocumentConverter {

    public static Map<String, Object> convert(Firestore db, Document document) {
        return convertMap(db, document.getFieldsMap());
    }

    private static Map<String, Object> convertMap(Firestore db, Map<String, Value> document) {
        Map<String, Object> map = new HashMap<>();

        for (Entry<String, Value> entry : document.entrySet()) {
            String key = entry.getKey();
            Value value = entry.getValue();

            switch (value.getValueTypeCase()) {
                case ARRAY_VALUE:
                    map.put(key, convertArray(db, value.getArrayValue().getValuesList()));
                    break;
                case BOOLEAN_VALUE:
                    map.put(key, value.getBooleanValue());
                    break;
                case BYTES_VALUE:
                    map.put(key, value.getBytesValue());
                    break;
                case DOUBLE_VALUE:
                    map.put(key, value.getDoubleValue());
                    break;
                case GEO_POINT_VALUE:
                    map.put(key, convert(value.getGeoPointValue()));
                    break;
                case INTEGER_VALUE:
                    map.put(key, value.getIntegerValue());
                    break;
                case MAP_VALUE:
                    map.put(key, convertMap(db, value.getMapValue().getFieldsMap()));
                    break;
                case NULL_VALUE:
                    map.put(key, null);
                    break;
                case REFERENCE_VALUE:
                    map.put(key, db.document(value.getReferenceValue()));
                    break;
                case STRING_VALUE:
                    map.put(key, value.getStringValue());
                    break;
                case TIMESTAMP_VALUE:
                    map.put(key, convert(value.getTimestampValue()));
                    break;
                case VALUETYPE_NOT_SET:
                    // No need to convert this type
                    break;
                default:
                    // An unknown type was encountered, this is not good
                    break;
            }
        }

        return map;
    }

    private static List<Object> convertArray(Firestore db, List<Value> document) {
        List<Object> list = new ArrayList<>();
        for (Value value : document) {

            switch (value.getValueTypeCase()) {
                case ARRAY_VALUE:
                    list.add(convertArray(db, value.getArrayValue().getValuesList()));
                    break;
                case BOOLEAN_VALUE:
                    list.add(value.getBooleanValue());
                    break;
                case BYTES_VALUE:
                    list.add(value.getBytesValue());
                    break;
                case DOUBLE_VALUE:
                    list.add(value.getDoubleValue());
                    break;
                case GEO_POINT_VALUE:
                    list.add(convert(value.getGeoPointValue()));
                    break;
                case INTEGER_VALUE:
                    list.add(value.getIntegerValue());
                    break;
                case MAP_VALUE:
                    list.add(convertMap(db, value.getMapValue().getFieldsMap()));
                    break;
                case NULL_VALUE:
                    list.add(null);
                    break;
                case REFERENCE_VALUE:
                    list.add(db.document(value.getReferenceValue()));
                    break;
                case STRING_VALUE:
                    list.add(value.getStringValue());
                    break;
                case TIMESTAMP_VALUE:
                    list.add(convert(value.getTimestampValue()));
                    break;
                case VALUETYPE_NOT_SET:
                    break;
                default:
                    break;
            }
        }

        return list;
    }

    private static GeoPoint convert(LatLng latLng) {
        return new GeoPoint(latLng.getLatitude(), latLng.getLongitude());
    }

    private static com.google.cloud.Timestamp convert(com.google.protobuf.Timestamp timestamp) {
        return com.google.cloud.Timestamp.ofTimeSecondsAndNanos(timestamp.getSeconds(), timestamp.getNanos());
    }
}

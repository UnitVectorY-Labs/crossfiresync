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
import org.junit.jupiter.api.Test;
import com.unitvectory.crossfiresync.config.ReplicationMode;

/**
 * The ReplicationMode test class.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class ReplicationModeTest {

    @Test
    public void nullTest() {
        assertEquals(ReplicationMode.NONE, ReplicationMode.parseFallbackToNone(null));
    }

    @Test
    public void invalidTest() {
        assertEquals(ReplicationMode.NONE, ReplicationMode.parseFallbackToNone("invalid"));
    }

    @Test
    public void singleRegionPrimaryTest() {
        assertEquals(ReplicationMode.SINGLE_REGION_PRIMARY,
                ReplicationMode.parseFallbackToNone("SINGLE_REGION_PRIMARY"));
    }

    @Test
    public void multiRegionPrimaryTest() {
        assertEquals(ReplicationMode.MULTI_REGION_PRIMARY,
                ReplicationMode.parseFallbackToNone("MULTI_REGION_PRIMARY"));
    }
}

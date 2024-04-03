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

import org.junit.jupiter.params.ParameterizedTest;

import com.unitvectory.fileparamunit.ListFileSource;
import com.unitvectory.jsonparamunit.JsonClassParamUnit;

/**
 * The DocumentResourceNameDocumentPathUtil utility class test.
 * 
 * @author Jared Hatfield (UnitVectorY Labs)
 */
public class DocumentResourceNameUtilDocumentPathTest extends JsonClassParamUnit<TestRecord, TestRecord> {

    protected DocumentResourceNameUtilDocumentPathTest() {
        super(TestRecord.class);
    }

    @ParameterizedTest
    @ListFileSource(resources = "/documentPath/", fileExtension = ".json", recurse = false)
    public void testIt(String file) {
        run(file);
    }

    @Override
    protected TestRecord process(TestRecord input, String context) {
        return TestRecord.builder().value(DocumentResourceNameUtil.getDocumentPath(input.getValue())).build();
    }
}

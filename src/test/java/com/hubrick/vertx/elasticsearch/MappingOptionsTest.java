/**
 * Copyright (C) 2016 Etaia AS (oss@hubrick.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubrick.vertx.elasticsearch;

import com.hubrick.vertx.elasticsearch.MappingOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link MappingOptions}
 */
public class MappingOptionsTest {

    @Test
    public void testToJson() throws Exception {

        MappingOptions options = new MappingOptions();

        JsonObject json1 = options.toJson();
        assertEquals(0, json1.fieldNames().size());

        options.setIgnoreConflicts(true);
        json1 = options.toJson();
        assertEquals(true, json1.getBoolean(MappingOptions.JSON_FIELD_IGNORE_CONFLICTS));

        MappingOptions options2 = new MappingOptions(json1);
        JsonObject json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

        options2 = new MappingOptions(options);
        json2 = options2.toJson();

        assertEquals(json1.encode(), json2.encode());

    }

}

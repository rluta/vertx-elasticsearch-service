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

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author marcus
 * @since 1.0.0
 */
public class SuggestOptionsTest {

    @Test
    public void testSuggestOptions() throws Exception {
        final SuggestOptions options = new SuggestOptions();
        final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption().setField("field").setText("text").setSize(10);
        options.addSuggestion("name", completionSuggestOption);

        final JsonObject json = options.toJson();

        assertEquals(4, json.getJsonObject("suggestions").getJsonObject("name").fieldNames().size());

        final SuggestOptions other = new SuggestOptions(options);
        final SuggestOptions otherFromJson = new SuggestOptions(json);

        assertEquals(json.encode(),other.toJson().encode());
        assertEquals(json.encode(),otherFromJson.toJson().encode());

    }
}

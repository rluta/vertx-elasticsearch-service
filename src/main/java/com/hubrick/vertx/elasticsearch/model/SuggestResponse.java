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
package com.hubrick.vertx.elasticsearch.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

@DataObject
public class SuggestResponse extends AbstractSearchResponse<SuggestResponse> {

    private Map<String, Suggestion> suggestions = new HashMap<>();

    public static final String JSON_FIELD_SUGGESTION = "suggestions";

    public SuggestResponse() {
    }

    public SuggestResponse(SuggestResponse other) {
        super(other);

        this.suggestions = other.getSuggestions();
    }

    public SuggestResponse(JsonObject json) {
        super(json);

        final JsonObject jsonSuggestions = json.getJsonObject(JSON_FIELD_SUGGESTION);
        if (jsonSuggestions != null) {
            for (String name : jsonSuggestions.fieldNames()) {
                this.suggestions.put(name, new Suggestion(jsonSuggestions.getJsonObject(name)));
            }
        }
    }

    @GenIgnore
    public SuggestResponse addSuggestion(String name, Suggestion suggestion) {
        suggestions.put(name, suggestion);
        return this;
    }

    public Map<String, Suggestion> getSuggestions() {
        return suggestions;
    }

    public SuggestResponse setSuggestions(Map<String, Suggestion> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (suggestions != null) {
            final JsonObject jsonSuggestions = new JsonObject();
            suggestions.entrySet().forEach(e -> jsonSuggestions.put(e.getKey(), e.getValue().toJson()));
            json.put(JSON_FIELD_SUGGESTION, jsonSuggestions);
        }

        return json.mergeIn(super.toJson());
    }
}

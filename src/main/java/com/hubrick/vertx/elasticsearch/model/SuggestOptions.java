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

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class SuggestOptions extends AbstractOptions<SuggestOptions> {

    public static final String JSON_FIELD_SUGGESTIONS = "suggestions";

    private Map<String, BaseSuggestOption> suggestions = new HashMap<>();

    public SuggestOptions() {
    }

    public SuggestOptions(SuggestOptions other) {
        super(other);

        suggestions = other.getSuggestions();
    }

    public SuggestOptions(JsonObject json) {
        super(json);

        final JsonObject suggestOptionsJson = json.getJsonObject(JSON_FIELD_SUGGESTIONS);
        if (JSON_FIELD_SUGGESTIONS != null) {
            for (String suggestionName : suggestOptionsJson.fieldNames()) {
                suggestions.put(suggestionName, BaseSuggestOption.parseJson(suggestOptionsJson.getJsonObject(suggestionName)));
            }
        }
    }

    public Map<String, BaseSuggestOption> getSuggestions() {
        return suggestions;
    }

    @GenIgnore
    public SuggestOptions addSuggestion(String name, BaseSuggestOption baseSuggestOption) {
        suggestions.put(name, baseSuggestOption);
        return this;
    }

    @Override
    public JsonObject toJson() {
        final JsonObject json = super.toJson();

        if (!suggestions.isEmpty()) {
            final JsonObject jsonSuggestions = new JsonObject();
            suggestions.entrySet().forEach(suggestion -> jsonSuggestions.put(suggestion.getKey(), suggestion.getValue().toJson()));
            json.put(JSON_FIELD_SUGGESTIONS, jsonSuggestions);
        }

        super.toJson().mergeIn(json);
        return json;
    }
}

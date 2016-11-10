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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public abstract class BaseSuggestOption {

    private SuggestType suggestType;

    public static final String JSON_FIELD_SUGGEST_TYPE = "suggestType";

    protected BaseSuggestOption(SuggestType suggestType) {
        this.suggestType = suggestType;
    }

    public BaseSuggestOption(BaseSuggestOption other) {
        suggestType = other.getSuggestType();
    }

    public BaseSuggestOption(JsonObject json) {
        try {
            suggestType = SuggestType.valueOf(json.getString(JSON_FIELD_SUGGEST_TYPE));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type " + json.getString(JSON_FIELD_SUGGEST_TYPE) + " is not supported");
        }
    }

    public SuggestType getSuggestType() {
        return suggestType;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_FIELD_SUGGEST_TYPE, suggestType.name());
    }

    public static BaseSuggestOption parseJson(JsonObject jsonObject) {
        try {
            final SuggestType suggestType = SuggestType.valueOf(jsonObject.getString(JSON_FIELD_SUGGEST_TYPE));
            switch (suggestType) {
                case COMPLETION:
                    return new CompletionSuggestOption(jsonObject);
                default:
                    throw new IllegalArgumentException("SuggestType " + jsonObject.getString(JSON_FIELD_SUGGEST_TYPE) + " is not supported");
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("SuggestType " + jsonObject.getString(JSON_FIELD_SUGGEST_TYPE) + " is not supported");
        }
    }

    public enum SuggestType {
        COMPLETION
    }
}

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
import io.vertx.core.json.JsonObject;

/**
 * Options for admin put mapping operations
 */
@DataObject
public class MappingOptions {

    private Boolean ignoreConflicts;

    public static final String JSON_FIELD_IGNORE_CONFLICTS = "ignoreConflicts";

    public MappingOptions() {
    }

    public MappingOptions(MappingOptions other) {
        ignoreConflicts = other.ignoreConflicts;
    }

    public MappingOptions(JsonObject json) {

        ignoreConflicts = json.getBoolean(JSON_FIELD_IGNORE_CONFLICTS);

    }

    public Boolean shouldIgnoreConflicts() {
        return ignoreConflicts;
    }

    public MappingOptions setIgnoreConflicts(Boolean ignoreConflicts) {
        this.ignoreConflicts = ignoreConflicts;
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (ignoreConflicts != null) json.put(JSON_FIELD_IGNORE_CONFLICTS, ignoreConflicts);

        return json;
    }

}

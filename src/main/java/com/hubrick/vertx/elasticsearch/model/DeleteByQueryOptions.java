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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Delete by query operation options
 */
@DataObject
public class DeleteByQueryOptions {

    private List<String> types = new ArrayList<>();
    private String timeout;
    private String routing;

    public static final String JSON_FIELD_TYPES = "types";
    public static final String JSON_FIELD_TIMEOUT = "timeout";
    public static final String JSON_FIELD_ROUTING = "routing";

    public DeleteByQueryOptions() {
    }

    public DeleteByQueryOptions(DeleteByQueryOptions other) {
        types = other.getTypes();
        timeout = other.getTimeout();
        routing = other.getRouting();
    }

    public DeleteByQueryOptions(JsonObject json) {
        types = json.getJsonArray(JSON_FIELD_TYPES, new JsonArray()).getList();
        timeout = json.getString(JSON_FIELD_TIMEOUT);
        routing = json.getString(JSON_FIELD_ROUTING);
    }

    public List<String> getTypes() {
        return types;
    }

    public DeleteByQueryOptions addType(String type) {
        types.add(type);
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public DeleteByQueryOptions setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }

    public String getRouting() {
        return routing;
    }

    public DeleteByQueryOptions setRouting(String routing) {
        this.routing = routing;
        return this;
    }

    public JsonObject toJson() {

        JsonObject json = new JsonObject();

        if (!types.isEmpty()) json.put(JSON_FIELD_TYPES, new JsonArray(types));
        if (timeout != null) json.put(JSON_FIELD_TIMEOUT, timeout);
        if (routing != null) json.put(JSON_FIELD_ROUTING, routing);

        return json;
    }

}

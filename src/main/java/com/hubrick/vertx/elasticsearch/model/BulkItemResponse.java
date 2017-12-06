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

import io.vertx.core.json.JsonObject;

/**
 * @author sp@hubrick.com
 * @since 06.12.17
 */
public class BulkItemResponse {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_SHARDS = "shards";

    private String id;
    private Shards shards;

    public BulkItemResponse() {
    }

    public BulkItemResponse(JsonObject json)  {
        this.id = json.getString(JSON_FIELD_ID);
        this.shards = new Shards(json.getJsonObject(JSON_FIELD_SHARDS));
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Shards getShards() {
        return shards;
    }

    public void setShards(final Shards shards) {
        this.shards = shards;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (id != null) json.put(JSON_FIELD_ID, id);
        if (shards != null) json.put(JSON_FIELD_SHARDS, shards.toJson());

        return json;
    }
}

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

@DataObject
public class DeleteByQueryResponse extends AbstractRawResponse<DeleteByQueryResponse> {

    private Long took;
    private Boolean timedOut;
    private Long totalFound;
    private Long totalDeleted;
    private Long totalMissing;
    private Long totalFailed;

    public static final String JSON_FIELD_TOOK = "took";
    public static final String JSON_FIELD_TIMED_OUT = "timedOut";
    public static final String JSON_FIELD_TOTAL_FOUND = "totalFound";
    public static final String JSON_FIELD_TOTAL_DELETED = "totalDeleted";
    public static final String JSON_FIELD_TOTAL_MISSING = "totalMissing";
    public static final String JSON_FIELD_TOTAL_FAILED = "totalFailed";

    public DeleteByQueryResponse() {
    }

    public DeleteByQueryResponse(DeleteByQueryResponse other) {
        super(other);

        this.took = other.getTook();
        this.timedOut = other.getTimedOut();
        this.totalFound = other.getTotalFound();
        this.totalMissing = other.getTotalMissing();
        this.totalFailed = other.getTotalFailed();
    }

    public DeleteByQueryResponse(JsonObject json) {
        super(json);

        this.took = json.getLong(JSON_FIELD_TOOK);
        this.timedOut = json.getBoolean(JSON_FIELD_TIMED_OUT);
        this.totalFound = json.getLong(JSON_FIELD_TOTAL_FOUND);
        this.totalDeleted = json.getLong(JSON_FIELD_TOTAL_DELETED);
        this.totalMissing = json.getLong(JSON_FIELD_TOTAL_MISSING);
        this.totalFailed = json.getLong(JSON_FIELD_TOTAL_FAILED);
    }

    public Long getTook() {
        return took;
    }

    public DeleteByQueryResponse setTook(Long took) {
        this.took = took;
        return this;
    }

    public Boolean getTimedOut() {
        return timedOut;
    }

    public DeleteByQueryResponse setTimedOut(Boolean timedOut) {
        this.timedOut = timedOut;
        return this;
    }

    public Long getTotalFound() {
        return totalFound;
    }

    public DeleteByQueryResponse setTotalFound(Long totalFound) {
        this.totalFound = totalFound;
        return this;
    }

    public Long getTotalDeleted() {
        return totalDeleted;
    }

    public DeleteByQueryResponse setTotalDeleted(Long totalDeleted) {
        this.totalDeleted = totalDeleted;
        return this;
    }

    public Long getTotalMissing() {
        return totalMissing;
    }

    public DeleteByQueryResponse setTotalMissing(Long totalMissing) {
        this.totalMissing = totalMissing;
        return this;
    }

    public Long getTotalFailed() {
        return totalFailed;
    }

    public DeleteByQueryResponse setTotalFailed(Long totalFailed) {
        this.totalFailed = totalFailed;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (took != null) json.put(JSON_FIELD_TOOK, took);
        if (timedOut != null) json.put(JSON_FIELD_TIMED_OUT, timedOut);
        if (totalFound != null) json.put(JSON_FIELD_TOTAL_FOUND, totalFound);
        if (totalDeleted != null) json.put(JSON_FIELD_TOTAL_DELETED, totalDeleted);
        if (totalMissing != null) json.put(JSON_FIELD_TOTAL_MISSING, totalMissing);
        if (totalFailed != null) json.put(JSON_FIELD_TOTAL_FAILED, totalFailed);

        return json.mergeIn(super.toJson());
    }
}

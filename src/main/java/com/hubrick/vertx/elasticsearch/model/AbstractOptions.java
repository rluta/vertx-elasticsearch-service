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
import org.elasticsearch.index.VersionType;

/**
 * Base options for all elasticsearch operations
 */
public class AbstractOptions<T extends AbstractOptions<T>> {

    private String routing;
    private String parent;
    private Boolean refresh;
    private Long version;
    private VersionType versionType;

    public static final String FIELD_ROUTING = "routing";
    public static final String FIELD_PARENT = "parent";
    public static final String FIELD_REFRESH = "refresh";
    public static final String FIELD_VERSION = "version";
    public static final String FIELD_VERSION_TYPE = "versionType";

    protected AbstractOptions() {
    }

    protected AbstractOptions(T other) {
        routing = other.getRouting();
        parent = other.getParent();
        refresh = other.isRefresh();
        version = other.getVersion();
        versionType = other.getVersionType();
    }

    protected AbstractOptions(JsonObject json) {

        routing = json.getString(FIELD_ROUTING);
        parent = json.getString(FIELD_PARENT);
        refresh = json.getBoolean(FIELD_REFRESH);
        version = json.getLong(FIELD_VERSION);

        String s = json.getString(FIELD_VERSION_TYPE);
        if (s != null) versionType = VersionType.fromString(s);

    }

    public String getRouting() {
        return routing;
    }

    public T setRouting(String routing) {
        this.routing = routing;
        return returnThis();
    }

    public String getParent() {
        return parent;
    }

    public T setParent(String parent) {
        this.parent = parent;
        return returnThis();
    }

    public Boolean isRefresh() {
        return refresh;
    }

    public T setRefresh(Boolean refresh) {
        this.refresh = refresh;
        return returnThis();
    }

    public Long getVersion() {
        return version;
    }

    public T setVersion(Long version) {
        this.version = version;
        return returnThis();
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public T setVersionType(VersionType versionType) {
        this.versionType = versionType;
        return returnThis();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (getRouting() != null) json.put(FIELD_ROUTING, getRouting());
        if (getParent() != null) json.put(FIELD_PARENT, getParent());
        if (isRefresh() != null) json.put(FIELD_REFRESH, isRefresh());
        if (getVersion() != null) json.put(FIELD_VERSION, getVersion());
        if (getVersionType() != null) json.put(FIELD_VERSION_TYPE, getVersionType().toString().toLowerCase());

        return json;
    }

    @SuppressWarnings("unchecked")
    protected T returnThis() {
        return (T) this;
    }

}

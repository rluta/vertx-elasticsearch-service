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

import com.hubrick.vertx.elasticsearch.impl.DefaultRx2ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteIndexOptions;
import com.hubrick.vertx.elasticsearch.model.MappingOptions;
import com.hubrick.vertx.elasticsearch.model.TemplateOptions;
import io.reactivex.Completable;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Collections;
import java.util.List;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public interface Rx2ElasticSearchAdminService {

    static Rx2ElasticSearchAdminService createEventBusProxy(Vertx vertx, String address) {
        return new DefaultRx2ElasticSearchAdminService(ProxyHelper.createProxy(ElasticSearchAdminService.class, vertx, address));
    }

    default Completable putMapping(String index, String type, JsonObject source) {
        return putMapping(Collections.singletonList(index), type, source);
    }

    default Completable putMapping(String index, String type, JsonObject source, MappingOptions options) {
        return putMapping(Collections.singletonList(index), type, source, options);
    }

    default Completable putMapping(List<String> indices, String type, JsonObject source) {
        return putMapping(indices, type, source, null);
    }

    Completable putMapping(List<String> indices, String type, JsonObject source, MappingOptions options);

    Completable createIndex(String index, JsonObject source, CreateIndexOptions options);

    default Completable deleteIndex(String index) {
        return deleteIndex(Collections.singletonList(index), new DeleteIndexOptions());
    }

    default Completable deleteIndex(String index, DeleteIndexOptions options) {
        return deleteIndex(Collections.singletonList(index), options);
    }

    default Completable deleteIndex(List<String> indices) {
        return deleteIndex(indices, new DeleteIndexOptions());
    }

    Completable deleteIndex(List<String> indices, DeleteIndexOptions options);

    Completable putTemplate(String name, JsonObject source, TemplateOptions options);

    Completable deleteTemplate(String name, TemplateOptions options);
}

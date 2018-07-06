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
package com.hubrick.vertx.elasticsearch.impl;

import com.hubrick.vertx.elasticsearch.ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.Rx2ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteIndexOptions;
import com.hubrick.vertx.elasticsearch.model.MappingOptions;
import com.hubrick.vertx.elasticsearch.model.TemplateOptions;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public class DefaultRx2ElasticSearchAdminService implements Rx2ElasticSearchAdminService {

    private final ElasticSearchAdminService elasticSearchAdminService;

    public DefaultRx2ElasticSearchAdminService(ElasticSearchAdminService elasticSearchAdminService) {
        checkNotNull(elasticSearchAdminService, "elasticSearchAdminService must not be null");

        this.elasticSearchAdminService = elasticSearchAdminService;
    }

    @Override
    public Single<Void> putMapping(List<String> indices, String type, JsonObject source, MappingOptions options) {
        return Single.create(handler -> {
            elasticSearchAdminService.putMapping(indices, type, source, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<Void> createIndex(String index, JsonObject source, CreateIndexOptions options) {
        return Single.create(handler -> {
            elasticSearchAdminService.createIndex(index, source, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<Void> deleteIndex(List<String> indices, DeleteIndexOptions options) {
        return Single.create(handler -> {
            elasticSearchAdminService.deleteIndex(indices, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<Void> putTemplate(String name, JsonObject source, TemplateOptions options) {
        return Single.create(handler -> {
            elasticSearchAdminService.putTemplate(name, source, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<Void> deleteTemplate(String name, TemplateOptions options) {
        return Single.create(handler -> {
            elasticSearchAdminService.deleteTemplate(name, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }
}

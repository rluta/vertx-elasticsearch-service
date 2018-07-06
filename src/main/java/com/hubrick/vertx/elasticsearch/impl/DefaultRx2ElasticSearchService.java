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

import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.Rx2ElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.BulkDeleteOptions;
import com.hubrick.vertx.elasticsearch.model.BulkIndexOptions;
import com.hubrick.vertx.elasticsearch.model.BulkOptions;
import com.hubrick.vertx.elasticsearch.model.BulkResponse;
import com.hubrick.vertx.elasticsearch.model.BulkUpdateOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse;
import com.hubrick.vertx.elasticsearch.model.DeleteOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteResponse;
import com.hubrick.vertx.elasticsearch.model.GetOptions;
import com.hubrick.vertx.elasticsearch.model.GetResponse;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.IndexResponse;
import com.hubrick.vertx.elasticsearch.model.MultiGetOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetResponse;
import com.hubrick.vertx.elasticsearch.model.MultiSearchOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateResponse;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public class DefaultRx2ElasticSearchService implements Rx2ElasticSearchService {

    private final ElasticSearchService elasticSearchService;

    public DefaultRx2ElasticSearchService(ElasticSearchService elasticSearchService) {
        checkNotNull(elasticSearchService, "elasticSearchService must not be null");

        this.elasticSearchService = elasticSearchService;
    }

    @Override
    public Single<IndexResponse> index(String index, String type, JsonObject source, IndexOptions options) {
        return Single.create(handler -> {
            elasticSearchService.index(index, type, source, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<UpdateResponse> update(String index, String type, String id, UpdateOptions options) {
        return Single.create(handler -> {
            elasticSearchService.update(index, type, id, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<GetResponse> get(String index, String type, String id, GetOptions options) {
        return Single.create(handler -> {
            elasticSearchService.get(index, type, id, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<SearchResponse> search(List<String> indices, SearchOptions options) {
        return Single.create(handler -> {
            elasticSearchService.search(indices, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<SearchResponse> searchScroll(String scrollId, SearchScrollOptions options) {
        return Single.create(handler -> {
            elasticSearchService.searchScroll(scrollId, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<DeleteResponse> delete(String index, String type, String id, DeleteOptions options) {
        return Single.create(handler -> {
            elasticSearchService.delete(index, type, id, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<BulkResponse> bulk(List<BulkIndexOptions> bulkIndexOptions,
                                     List<BulkUpdateOptions> bulkUpdateOptions,
                                     List<BulkDeleteOptions> bulkDeleteOptions,
                                     BulkOptions bulkOptions) {
        return Single.create(handler -> {
            elasticSearchService.bulk(bulkIndexOptions, bulkUpdateOptions, bulkDeleteOptions, bulkOptions, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<MultiSearchResponse> multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions, MultiSearchOptions options) {
        return Single.create(handler -> {
            elasticSearchService.multiSearch(multiSearchQueryOptions, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<MultiGetResponse> multiGet(List<MultiGetQueryOptions> multiGetQueryOptions, MultiGetOptions options) {
        return Single.create(handler -> {
            elasticSearchService.multiGet(multiGetQueryOptions, options, response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }

    @Override
    public Single<DeleteByQueryResponse> deleteByQuery(List<String> indices, DeleteByQueryOptions options) {
        return Single.create(handler -> {
            elasticSearchService.deleteByQuery(indices, options,  response -> {
                if (response.succeeded()) {
                    handler.onSuccess(response.result());
                } else {
                    handler.onError(response.cause());
                }
            });
        });
    }
}

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

import com.hubrick.vertx.elasticsearch.impl.DefaultRx2ElasticSearchService;
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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

/**
 * ElasticSearch RX2 service
 *
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public interface Rx2ElasticSearchService {

    static Rx2ElasticSearchService createEventBusProxy(Vertx vertx, String address) {
        return new DefaultRx2ElasticSearchService(ElasticSearchService.createEventBusProxy(vertx, address));
    }

    default Single<IndexResponse> index(String index, String type, JsonObject source) {
        return index(index, type, source, new IndexOptions());
    }

    Single<IndexResponse> index(String index, String type, JsonObject source, IndexOptions options);

    Single<UpdateResponse> update(String index, String type, String id, UpdateOptions options);

    default Single<GetResponse> get(String index, String type, String id) {
        return get(index, type, id, new GetOptions());
    }

    Single<GetResponse> get(String index, String type, String id, GetOptions options);

    default Single<SearchResponse> search(String index) {
        return search(index, new SearchOptions());
    }

    default Single<SearchResponse> search(String index, SearchOptions options) {
        return search(Collections.singletonList(index), options);
    }

    default Single<SearchResponse> search(List<String> indices) {
        return search(indices, new SearchOptions());
    }

    Single<SearchResponse> search(List<String> indices, SearchOptions options);

    default Single<SearchResponse> searchScroll(String scrollId) {
        return searchScroll(scrollId, new SearchScrollOptions());
    }

    Single<SearchResponse> searchScroll(String scrollId, SearchScrollOptions options);

    default Single<DeleteResponse> delete(String index, String type, String id) {
        return delete(index, type, id, new DeleteOptions());
    }

    Single<DeleteResponse> delete(String index, String type, String id, DeleteOptions options);

    default Single<BulkResponse> bulkIndex(final List<BulkIndexOptions> bulkIndexOptions, BulkOptions options) {
        return bulk(bulkIndexOptions, Collections.emptyList(), Collections.emptyList(), options);
    }

    default Single<BulkResponse> bulkUpdate(final List<BulkUpdateOptions> bulkUpdateOptions, BulkOptions options) {
        return bulk(Collections.emptyList(), bulkUpdateOptions, Collections.emptyList(), options);
    }

    default Single<BulkResponse> bulkDelete(final List<BulkDeleteOptions> bulkDeleteOptions, BulkOptions options) {
        return bulk(Collections.emptyList(), Collections.emptyList(), bulkDeleteOptions, options);
    }

    Single<BulkResponse> bulk(final List<BulkIndexOptions> indexOptions,
                              final List<BulkUpdateOptions> updateOptions,
                              final List<BulkDeleteOptions> deleteOptions,
                              final BulkOptions bulkOptions);

    default Single<MultiSearchResponse> multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions) {
        return multiSearch(multiSearchQueryOptions, new MultiSearchOptions());
    }

    Single<MultiSearchResponse> multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions,
                                            final MultiSearchOptions options);

    default Single<MultiGetResponse> multiGet(final List<MultiGetQueryOptions> multiGetQueryOptions) {
        return multiGet(multiGetQueryOptions, new MultiGetOptions());
    }

    Single<MultiGetResponse> multiGet(final List<MultiGetQueryOptions> multiGetQueryOptions,
                                      final MultiGetOptions options);

    default Single<DeleteByQueryResponse> deleteByQuery(String index, DeleteByQueryOptions options) {
        return deleteByQuery(Collections.singletonList(index), options);
    }

    Single<DeleteByQueryResponse> deleteByQuery(List<String> indices, DeleteByQueryOptions options);
}

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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Collections;
import java.util.List;

/**
 * ElasticSearch service
 */
@VertxGen
@ProxyGen
public interface ElasticSearchService {

    static ElasticSearchService createEventBusProxy(Vertx vertx, String address) {
        return ProxyHelper.createProxy(ElasticSearchService.class, vertx, address);
    }

    @ProxyIgnore
    void start();

    @ProxyIgnore
    void stop();

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param source        the source to be indexed
     * @param resultHandler result handler callback
     */
    @GenIgnore
    @ProxyIgnore
    default void index(String index, String type, JsonObject source, Handler<AsyncResult<JsonObject>> resultHandler) {
        index(index, type, source, new IndexOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param source        the source to be indexed
     * @param options       optional index options (id, timeout, ttl, etc.)
     * @param resultHandler result handler callback
     */
    void index(String index, String type, JsonObject source, IndexOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/java-update-api.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to update
     * @param options       the update options (doc, script, etc.)
     * @param resultHandler result handler callback
     */
    void update(String index, String type, String id, UpdateOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to update
     * @param resultHandler result handler callback
     */
    @GenIgnore
    @ProxyIgnore
    default void get(String index, String type, String id, Handler<AsyncResult<JsonObject>> resultHandler) {
        get(index, type, id, new GetOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to update
     * @param options       the update options
     * @param resultHandler result handler callback
     */
    void get(String index, String type, String id, GetOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void search(String index, Handler<AsyncResult<JsonObject>> resultHandler) {
        search(index, new SearchOptions(), resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void search(String index, SearchOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        search(Collections.singletonList(index), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void search(List<String> indices, Handler<AsyncResult<JsonObject>> resultHandler) {
        search(indices, new SearchOptions(), resultHandler);
    }

    void search(List<String> indices, SearchOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void searchScroll(String scrollId, Handler<AsyncResult<JsonObject>> resultHandler) {
        searchScroll(scrollId, new SearchScrollOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/reference/1.4/search-request-scroll.html
     *
     * @param scrollId
     * @param options
     * @param resultHandler
     */
    void searchScroll(String scrollId, SearchScrollOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to delete
     * @param resultHandler result handler callback
     */
    @GenIgnore
    @ProxyIgnore
    default void delete(String index, String type, String id, Handler<AsyncResult<JsonObject>> resultHandler) {
        delete(index, type, id, new DeleteOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to delete
     * @param options       optional delete options (timeout, etc.)
     * @param resultHandler result handler callback
     */
    void delete(String index, String type, String id, DeleteOptions options, Handler<AsyncResult<JsonObject>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void suggest(String index, SuggestOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        suggest(Collections.singletonList(index), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void suggest(String index, Handler<AsyncResult<JsonObject>> resultHandler) {
        suggest(Collections.singletonList(index), new SuggestOptions(), resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void suggest(List<String> indices, Handler<AsyncResult<JsonObject>> resultHandler) {
        suggest(indices, new SuggestOptions(), resultHandler);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters.html
     *
     * @param indices       the index names
     * @param options       optional suggest options
     * @param resultHandler result handler callback
     */
    void suggest(List<String> indices, SuggestOptions options, Handler<AsyncResult<JsonObject>> resultHandler);


    @GenIgnore
    @ProxyIgnore
    default void deleteByQuery(String index, JsonObject query, DeleteByQueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        deleteByQuery(Collections.singletonList(index), query, options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void deleteByQuery(String index, JsonObject query, Handler<AsyncResult<JsonObject>> resultHandler) {
        deleteByQuery(Collections.singletonList(index), query, new DeleteByQueryOptions(), resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void deleteByQuery(List<String> indices, JsonObject query, Handler<AsyncResult<JsonObject>> resultHandler) {
        deleteByQuery(indices, query, new DeleteByQueryOptions(), resultHandler);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
     *
     * @param indices       the index names
     * @param query         the query that will be used for deletion
     * @param options       delete by query options (timeout, etc.)
     * @param resultHandler result handler callback
     */
    void deleteByQuery(List<String> indices, JsonObject query, DeleteByQueryOptions options, Handler<AsyncResult<JsonObject>> resultHandler);
}

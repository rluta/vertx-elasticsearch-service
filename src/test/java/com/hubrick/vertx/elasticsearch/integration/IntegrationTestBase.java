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
package com.hubrick.vertx.elasticsearch.integration;

import com.google.common.collect.ImmutableList;
import com.hubrick.vertx.elasticsearch.AbstractVertxIntegrationTest;
import com.hubrick.vertx.elasticsearch.ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.ElasticSearchServiceVerticle;
import com.hubrick.vertx.elasticsearch.Rx2ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.Rx2ElasticSearchService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.impl.DefaultRx2ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.impl.DefaultRx2ElasticSearchService;
import com.hubrick.vertx.elasticsearch.impl.DefaultRxElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.impl.DefaultRxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.BulkIndexOptions;
import com.hubrick.vertx.elasticsearch.model.BulkOptions;
import com.hubrick.vertx.elasticsearch.model.BulkResponse;
import com.hubrick.vertx.elasticsearch.model.CompletionSuggestOption;
import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse;
import com.hubrick.vertx.elasticsearch.model.DeleteOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteResponse;
import com.hubrick.vertx.elasticsearch.model.GetResponse;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.IndexResponse;
import com.hubrick.vertx.elasticsearch.model.MultiGetQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetResponse;
import com.hubrick.vertx.elasticsearch.model.MultiSearchQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchResponse;
import com.hubrick.vertx.elasticsearch.model.OpType;
import com.hubrick.vertx.elasticsearch.model.RefreshPolicy;
import com.hubrick.vertx.elasticsearch.model.ScriptSortOption;
import com.hubrick.vertx.elasticsearch.model.ScriptType;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.model.SearchType;
import com.hubrick.vertx.elasticsearch.model.SortOrder;
import com.hubrick.vertx.elasticsearch.model.UpdateOptions;
import io.reactivex.Single;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hubrick.vertx.elasticsearch.VertxMatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

/**
 * {@link ElasticSearchServiceVerticle} integration test
 */
public abstract class IntegrationTestBase extends AbstractVertxIntegrationTest {

    private ElasticSearchService service;
    private ElasticSearchAdminService adminService;
    private RxElasticSearchService rxService;
    private RxElasticSearchAdminService rxAdminService;
    private Rx2ElasticSearchService rx2Service;
    private Rx2ElasticSearchAdminService rx2AdminService;

    private String id = "integration-test-1";
    private String index = "test_index";
    private String type = "test_type";
    private String source_user = "hubrick";
    private String source_message = "vertx elastic search";

    protected JsonObject config;

    protected abstract String getVerticleName();

    protected abstract void configure(JsonObject config);

    @Before
    public void setUp(TestContext testContext) throws Exception {
        config = readConfig();
        configure(config);

        deployVerticle(testContext, getVerticleName(), new DeploymentOptions().setConfig(config));

        service = ElasticSearchService.createEventBusProxy(vertx, "et.elasticsearch");
        adminService = ElasticSearchAdminService.createEventBusProxy(vertx, "et.elasticsearch.admin");
        rxService = new DefaultRxElasticSearchService(service);
        rxAdminService = new DefaultRxElasticSearchAdminService(adminService);
        rx2Service = new DefaultRx2ElasticSearchService(service);
        rx2AdminService = new DefaultRx2ElasticSearchAdminService(adminService);

        final CountDownLatch latch = new CountDownLatch(1);
        adminService.deleteIndex(index, deleteIndexResult -> {
            adminService.createIndex(index, new JsonObject(), new CreateIndexOptions(), createMappingResult -> {
                adminService.putMapping(index, type, readJson("mapping.json"), putMappingResult -> {
                    if (putMappingResult.failed()) {
                        putMappingResult.cause().printStackTrace();
                        testContext.fail();
                    }
                    latch.countDown();
                });
            });
        });

        latch.await(15, TimeUnit.SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        service.stop();
        destroyVerticle();
    }

    @Test
    public void testIndexRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rxService.index(this.index, type, source, options)
                .subscribe(
                        indexResponse -> {
                            assertIndex(testContext, indexResponse);

                            // Give elasticsearch time to index the document
                            vertx.setTimer(1000, id -> async.complete());
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testIndexRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rx2Service.index(this.index, type, source, options)
                .subscribe(
                        indexResponse -> {
                            assertIndex(testContext, indexResponse);

                            // Give elasticsearch time to index the document
                            vertx.setTimer(1000, id -> async.complete());
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertIndex(TestContext testContext, IndexResponse indexResponse) {
        assertThat(testContext, indexResponse.getIndex(), is(this.index));
        assertThat(testContext, indexResponse.getType(), is(type));
        assertThat(testContext, indexResponse.getId(), is(id));
        assertThat(testContext, indexResponse.getCreated(), is(true));
        assertThat(testContext, indexResponse.getVersion(), greaterThan(0l));
    }

    @Test
    public void testGetRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rxService.index(index, type, source, options)
                .flatMap(indexResponse -> rxService.get(index, type, id))
                .subscribe(
                        getResponse -> {
                            assertGet(testContext, getResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testGetRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rx2Service.index(index, type, source, options)
                .flatMap(indexResponse -> rx2Service.get(index, type, id))
                .subscribe(
                        getResponse -> {
                            assertGet(testContext, getResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertGet(TestContext testContext, GetResponse getResponse) {
        assertThat(testContext, getResponse.getResult().getIndex(), is(index));
        assertThat(testContext, getResponse.getResult().getType(), is(type));
        assertThat(testContext, getResponse.getResult().getId(), is(id));
        assertThat(testContext, getResponse.getResult().getExists(), is(true));
        assertThat(testContext, getResponse.getResult().getVersion(), greaterThan(0l));

        assertThat(testContext, getResponse.getResult().getSource(), notNullValue());
        assertThat(testContext, getResponse.getResult().getSource().getString("user"), is(source_user));
        assertThat(testContext, getResponse.getResult().getSource().getString("message"), is(source_message));
    }

    @Test
    public void testUpdateRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rxService.index(index, type, source, options)
                .flatMap(indexResponse -> {
                    final UpdateOptions updateOptions = new UpdateOptions().setScript("ctx._source.message = 'updated message'", ScriptType.INLINE);
                    return rxService.update(index, type, id, updateOptions);
                })
                .flatMap(updateResponse -> rxService.get(index, type, id))
                .subscribe(
                        getResponse -> {
                            assertUpdate(testContext, getResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testUpdateRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rx2Service.index(index, type, source, options)
                .flatMap(indexResponse -> {
                    final UpdateOptions updateOptions = new UpdateOptions().setScript("ctx._source.message = 'updated message'", ScriptType.INLINE);
                    return rx2Service.update(index, type, id, updateOptions);
                })
                .flatMap(updateResponse -> rx2Service.get(index, type, id))
                .subscribe(
                        getResponse -> {
                            assertUpdate(testContext, getResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertUpdate(TestContext testContext, GetResponse getResponse) {
        assertThat(testContext, getResponse.getResult().getIndex(), is(index));
        assertThat(testContext, getResponse.getResult().getType(), is(type));
        assertThat(testContext, getResponse.getResult().getId(), is(id));
        assertThat(testContext, getResponse.getResult().getExists(), is(true));
        assertThat(testContext, getResponse.getResult().getVersion(), greaterThan(0l));

        assertThat(testContext, getResponse.getResult().getSource(), notNullValue());
        assertThat(testContext, getResponse.getResult().getSource().getString("user"), is(source_user));
        assertThat(testContext, getResponse.getResult().getSource().getString("message"), is("updated message"));
    }

    @Test
    public void testSearchRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final SearchOptions searchOptions = new SearchOptions()
                .setTimeoutInMillis(1000l)
                .setSize(10)
                .setSourceIncludes(Arrays.asList("user", "message"))
                .addFieldSort("user", SortOrder.DESC)
                .addScripSort("doc['message'].value", "painless", ScriptSortOption.Type.STRING, new JsonObject().put("param1", ImmutableList.of("1", "2", "3")), SortOrder.ASC)
                .addScriptField("script_field", "doc['message'].value", "painless", new JsonObject().put("param1", ImmutableList.of("1", "2", "3")))
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        final IndexOptions indexOptions = new IndexOptions().setId(id);
        rxService.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    final ObservableFuture<IndexResponse> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(2000l, event -> observableFuture.toHandler().handle(Future.succeededFuture(indexResponse)));
                    return observableFuture;
                })
                .flatMap(indexResponse -> rxService.search(index, searchOptions))
                .subscribe(
                        searchResponse -> {
                            assertSearch(testContext, searchResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testSearchRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final SearchOptions searchOptions = new SearchOptions()
                .setTimeoutInMillis(1000l)
                .setSize(10)
                .setSourceIncludes(Arrays.asList("user", "message"))
                .addFieldSort("user", SortOrder.DESC)
                .addScripSort("doc['message'].value", "painless", ScriptSortOption.Type.STRING, new JsonObject().put("param1", ImmutableList.of("1", "2", "3")), SortOrder.ASC)
                .addScriptField("script_field", "doc['message'].value", "painless", new JsonObject().put("param1", ImmutableList.of("1", "2", "3")))
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        final IndexOptions indexOptions = new IndexOptions().setId(id);
        rx2Service.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    return Single.create(emitter -> {
                        vertx.setTimer(2000l, event -> {
                            emitter.onSuccess(indexResponse);
                        });
                    });
                })
                .flatMap(indexResponse -> rx2Service.search(index, searchOptions))
                .subscribe(
                        searchResponse -> {
                            assertSearch(testContext, searchResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertSearch(TestContext testContext, SearchResponse searchResponse) {
        assertThat(testContext, searchResponse, notNullValue());
        assertThat(testContext, searchResponse.getHits().getHits(), hasSize(1));

        assertThat(testContext, searchResponse.getHits().getHits().get(0).getIndex(), is(index));
        assertThat(testContext, searchResponse.getHits().getHits().get(0).getType(), is(type));
        assertThat(testContext, searchResponse.getHits().getHits().get(0).getId(), is(id));

        assertThat(testContext, searchResponse.getHits().getHits().get(0).getSource(), notNullValue());
        assertThat(testContext, searchResponse.getHits().getHits().get(0).getSource().getString("user"), is(source_user));
        assertThat(testContext, searchResponse.getHits().getHits().get(0).getSource().getString("message"), is(source_message));
    }

    @Test
    public void testScrollRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final SearchOptions options = new SearchOptions()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setScroll("5m")
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        final IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rxService.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(2000l, event -> observableFuture.toHandler().handle(Future.succeededFuture()));
                    return observableFuture;
                })
                .flatMap(aVoid -> rxService.search(index, options))
                .flatMap(searchResponse -> {
                    String scrollId = searchResponse.getScrollId();
                    assertScroll(testContext, searchResponse);
                    SearchScrollOptions scrollOptions = new SearchScrollOptions().setScroll("5m");
                    return rxService.searchScroll(scrollId, scrollOptions);
                })
                .subscribe(
                        searchResponse -> {
                            assertThat(testContext, searchResponse.getHits().getTotal(), greaterThan(0l));
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testScrollRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final SearchOptions options = new SearchOptions()
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setScroll("5m")
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        final IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rx2Service.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    return Single.create(emitter -> {
                        vertx.setTimer(2000l, event -> {
                            emitter.onSuccess(indexResponse);
                        });
                    });
                })
                .flatMap(aVoid -> rx2Service.search(index, options))
                .flatMap(searchResponse -> {
                    String scrollId = searchResponse.getScrollId();
                    assertScroll(testContext, searchResponse);
                    SearchScrollOptions scrollOptions = new SearchScrollOptions().setScroll("5m");
                    return rx2Service.searchScroll(scrollId, scrollOptions);
                })
                .subscribe(
                        searchResponse -> {
                            assertThat(testContext, searchResponse.getHits().getTotal(), greaterThan(0l));
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertScroll(TestContext testContext, SearchResponse searchResponse) {
        String scrollId = searchResponse.getScrollId();
        assertThat(testContext, scrollId, notNullValue());
        assertThat(testContext, searchResponse.getHits().getHits().size(), greaterThan(0));
    }

    @Test
    public void testSearch_SuggestRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("message_suggest", source_message);

        rxService.index(index, type, source)
                .flatMap(result -> {
                    final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(1000, id -> observableFuture.toHandler().handle(Future.succeededFuture()));
                    return observableFuture;
                })
                .flatMap(aVoid -> {
                    final SearchOptions options = new SearchOptions();
                    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
                            .setText("v")
                            .setField("message_suggest");
                    options.addSuggestion("test-suggest", completionSuggestOption);

                    return rxService.search(index, options);
                })
                .subscribe(
                        searchResponse -> {
                            assertSuggest(testContext, searchResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testSearch_SuggestRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("message_suggest", source_message);

        rx2Service.index(index, type, source)
                .flatMap(result -> {
                    return Single.create(emitter -> {
                        vertx.setTimer(2000l, event -> {
                            emitter.onSuccess(result);
                        });
                    });
                })
                .flatMap(aVoid -> {
                    final SearchOptions options = new SearchOptions();
                    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
                            .setText("v")
                            .setField("message_suggest");
                    options.addSuggestion("test-suggest", completionSuggestOption);

                    return rx2Service.search(index, options);
                })
                .subscribe(
                        searchResponse -> {
                            assertSuggest(testContext, searchResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertSuggest(TestContext testContext, SearchResponse searchResponse) {
        assertThat(testContext, searchResponse.getSuggestions().get("test-suggest"), notNullValue());
        assertThat(testContext, searchResponse.getSuggestions().get("test-suggest").getSize(), is(1));
        assertThat(testContext, searchResponse.getSuggestions().get("test-suggest").getEntries().get(0), notNullValue());
        assertThat(testContext, searchResponse.getSuggestions().get("test-suggest").getEntries().get(0).getLength(), is(1));
        assertThat(testContext, searchResponse.getSuggestions().get("test-suggest").getEntries().get(0).getOptions().get(0).getText(), is(source_message));
    }

    @Test
    public void testDeleteByQuerySimpleRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        final IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rxService.index(index, type, source, indexOptions)
                .flatMap(result -> {
                    final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(2000, id -> observableFuture.toHandler().handle(Future.succeededFuture()));
                    return observableFuture;
                })
                .flatMap(aVoid -> {
                    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions().setTimeoutInMillis(1000l);
                    return rxService.deleteByQuery(index, deleteByQueryOptions.setQuery(new JsonObject().put("ids", new JsonObject().put("values", new JsonArray().add(documentId.toString())))));
                })
                .subscribe(
                        deleteByQueryResponse -> {
                            assertDeleteByQuerySimple(testContext, deleteByQueryResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testDeleteByQuerySimpleRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        final IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rx2Service.index(index, type, source, indexOptions)
                .flatMap(result -> {
                    return Single.create(emitter -> {
                        vertx.setTimer(2000l, event -> {
                            emitter.onSuccess(result);
                        });
                    });
                })
                .flatMap(aVoid -> {
                    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions().setTimeoutInMillis(1000l);
                    return rx2Service.deleteByQuery(index, deleteByQueryOptions.setQuery(new JsonObject().put("ids", new JsonObject().put("values", new JsonArray().add(documentId.toString())))));
                })
                .subscribe(
                        deleteByQueryResponse -> {
                            assertDeleteByQuerySimple(testContext, deleteByQueryResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertDeleteByQuerySimple(TestContext testContext, DeleteByQueryResponse deleteByQueryResponse) {
        assertThat(testContext, deleteByQueryResponse.getTimedOut(), is(false));
        assertThat(testContext, deleteByQueryResponse.getDeleted(), is(1L));
        assertThat(testContext, deleteByQueryResponse.getFailures().size(), is(0));
    }

    @Test
    public void testBulkIndexRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source1 = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final JsonObject source2 = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final BulkOptions options = new BulkOptions();
        rxService.bulkIndex(
                ImmutableList.of(
                        new BulkIndexOptions().setIndex(index).setType(type).setSource(source1).setIndexOptions(new IndexOptions().setId("1")),
                        new BulkIndexOptions().setIndex(index).setType(type).setSource(source2).setIndexOptions(new IndexOptions().setId("2"))
                ),
                options
        ).subscribe(
                bulkIndexResponse -> {
                    assertBulkIndex(testContext, bulkIndexResponse);
                    // Give elasticsearch time to index the document
                    vertx.setTimer(1000, id -> async.complete());
                },
                error -> testContext.fail(error)
        );
    }

    @Test
    public void testBulkIndexRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source1 = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final JsonObject source2 = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final BulkOptions options = new BulkOptions();
        rx2Service.bulkIndex(
                ImmutableList.of(
                        new BulkIndexOptions().setIndex(index).setType(type).setSource(source1).setIndexOptions(new IndexOptions().setId("1")),
                        new BulkIndexOptions().setIndex(index).setType(type).setSource(source2).setIndexOptions(new IndexOptions().setId("2"))
                ),
                options
        ).subscribe(
                bulkIndexResponse -> {
                    assertBulkIndex(testContext, bulkIndexResponse);
                    // Give elasticsearch time to index the document
                    vertx.setTimer(1000, id -> async.complete());
                },
                error -> testContext.fail(error)
        );
    }

    private void assertBulkIndex(TestContext testContext, BulkResponse bulkIndexResponse) {
        assertThat(testContext, bulkIndexResponse, notNullValue());
        assertThat(testContext, bulkIndexResponse.getRawResponse(), notNullValue());

        assertThat(testContext, bulkIndexResponse.getResponses().size(), is(2));
        assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getId()).collect(Collectors.toList()), containsInAnyOrder("1", "2"));
        assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getShards()).collect(Collectors.toList()), hasSize(2));
        assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getIndex()).collect(Collectors.toList()), containsInAnyOrder(index, index));
        assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getType()).collect(Collectors.toList()), containsInAnyOrder(type, type));
        assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getOpType()).collect(Collectors.toList()), containsInAnyOrder(OpType.INDEX, OpType.INDEX));
        assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getFailure()).collect(Collectors.toList()), containsInAnyOrder(new JsonObject(), new JsonObject()));

        assertThat(testContext, bulkIndexResponse.getTookInMillis(), greaterThanOrEqualTo(0l));
    }

    @Test
    public void testMultiSearchRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final SearchOptions searchOptions = new SearchOptions()
                .setTimeoutInMillis(1000l)
                .setSize(10)
                .setSourceIncludes(Arrays.asList("user", "message"))
                .addFieldSort("user", SortOrder.DESC)
                .addScripSort("doc['message'].value", "painless", ScriptSortOption.Type.STRING, new JsonObject().put("param1", ImmutableList.of("1", "2", "3")), SortOrder.ASC)
                .addScriptField("script_field", "doc['message'].value", "painless", new JsonObject().put("param1", ImmutableList.of("1", "2", "3")))
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        final IndexOptions indexOptions = new IndexOptions().setId(id);
        rxService.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    final ObservableFuture<IndexResponse> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(2000l, event -> observableFuture.toHandler().handle(Future.succeededFuture(indexResponse)));
                    return observableFuture;
                })
                .flatMap(indexResponse -> rxService.multiSearch(ImmutableList.of(new MultiSearchQueryOptions().addIndex(index).setSearchOptions(searchOptions))))
                .subscribe(
                        multiSearchResponse -> {
                            assertMultiSearch(testContext, multiSearchResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testMultiSearchRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final SearchOptions searchOptions = new SearchOptions()
                .setTimeoutInMillis(1000l)
                .setSize(10)
                .setSourceIncludes(Arrays.asList("user", "message"))
                .addFieldSort("user", SortOrder.DESC)
                .addScripSort("doc['message'].value", "painless", ScriptSortOption.Type.STRING, new JsonObject().put("param1", ImmutableList.of("1", "2", "3")), SortOrder.ASC)
                .addScriptField("script_field", "doc['message'].value", "painless", new JsonObject().put("param1", ImmutableList.of("1", "2", "3")))
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        final IndexOptions indexOptions = new IndexOptions().setId(id);
        rx2Service.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    return Single.create(emitter -> {
                        vertx.setTimer(2000l, event -> {
                            emitter.onSuccess(indexResponse);
                        });
                    });
                })
                .flatMap(indexResponse -> rx2Service.multiSearch(ImmutableList.of(new MultiSearchQueryOptions().addIndex(index).setSearchOptions(searchOptions))))
                .subscribe(
                        multiSearchResponse -> {
                            assertMultiSearch(testContext, multiSearchResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertMultiSearch(TestContext testContext, MultiSearchResponse multiSearchResponse) {
        assertThat(testContext, multiSearchResponse, notNullValue());
        assertThat(testContext, multiSearchResponse.getResponses(), hasSize(1));
        assertThat(testContext, multiSearchResponse.getRawResponse(), notNullValue());

        assertThat(testContext, multiSearchResponse.getResponses().get(0).getFailureMessage(), nullValue());

        assertThat(testContext, multiSearchResponse.getResponses().get(0).getSearchResponse().getHits().getHits().get(0).getIndex(), is(index));
        assertThat(testContext, multiSearchResponse.getResponses().get(0).getSearchResponse().getHits().getHits().get(0).getType(), is(type));
        assertThat(testContext, multiSearchResponse.getResponses().get(0).getSearchResponse().getHits().getHits().get(0).getId(), is(id));

        assertThat(testContext, multiSearchResponse.getResponses().get(0).getSearchResponse().getHits().getHits().get(0).getSource(), notNullValue());
        assertThat(testContext, multiSearchResponse.getResponses().get(0).getSearchResponse().getHits().getHits().get(0).getSource().getString("user"), is(source_user));
        assertThat(testContext, multiSearchResponse.getResponses().get(0).getSearchResponse().getHits().getHits().get(0).getSource().getString("message"), is(source_message));
    }

    @Test
    public void testMultiGetRx(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rxService.index(index, type, source, options)
                .flatMap(indexResponse -> rxService.multiGet(ImmutableList.of(new MultiGetQueryOptions().setId(id).setIndex(index).setType(type).setFetchSource(true))))
                .subscribe(
                        multiGetResponse -> {
                            assertMultiGet(testContext, multiGetResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testMultiGetRx2(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final IndexOptions options = new IndexOptions().setId(id);
        rx2Service.index(index, type, source, options)
                .flatMap(indexResponse -> rx2Service.multiGet(ImmutableList.of(new MultiGetQueryOptions().setId(id).setIndex(index).setType(type).setFetchSource(true))))
                .subscribe(
                        multiGetResponse -> {
                            assertMultiGet(testContext, multiGetResponse);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertMultiGet(TestContext testContext, MultiGetResponse multiGetResponse) {
        assertThat(testContext, multiGetResponse, notNullValue());
        assertThat(testContext, multiGetResponse.getResponses(), hasSize(1));
        assertThat(testContext, multiGetResponse.getRawResponse(), notNullValue());

        assertThat(testContext, multiGetResponse.getResponses().get(0).getFailureMessage(), nullValue());

        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getIndex(), is(index));
        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getType(), is(type));
        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getId(), is(id));
        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getExists(), is(true));
        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getVersion(), greaterThan(0l));

        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getSource(), notNullValue());
        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getSource().getString("user"), is(source_user));
        assertThat(testContext, multiGetResponse.getResponses().get(0).getGetResult().getSource().getString("message"), is(source_message));
    }

    @Test
    public void testDeleteRx(TestContext testContext) throws Exception {
        final Async async = testContext.async();
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        final IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rxService.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(2000l, event -> observableFuture.toHandler().handle(Future.succeededFuture()));
                    return observableFuture;
                })
                .flatMap(indexResponse -> rxService.delete(index, type, documentId.toString(), new DeleteOptions().setRefresh(RefreshPolicy.IMMEDIATE)))
                .subscribe(
                        deleteResponse -> {
                            assertDelete(testContext, deleteResponse, documentId);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testDeleteRx2(TestContext testContext) throws Exception {
        final Async async = testContext.async(2);
        final JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        final IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rx2Service.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    return Single.create(emitter -> {
                        vertx.setTimer(2000l, event -> {
                            emitter.onSuccess(indexResponse);
                        });
                    });
                })
                .flatMap(indexResponse -> rx2Service.delete(index, type, documentId.toString(), new DeleteOptions().setRefresh(RefreshPolicy.IMMEDIATE)))
                .subscribe(
                        deleteResponse -> {
                            assertDelete(testContext, deleteResponse, documentId);
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    private void assertDelete(TestContext testContext, DeleteResponse deleteResponse, UUID documentId) {
        assertThat(testContext, deleteResponse.getIndex(), is(index));
        assertThat(testContext, deleteResponse.getType(), is(type));
        assertThat(testContext, deleteResponse.getId(), is(documentId.toString()));
        assertThat(testContext, deleteResponse.getDeleted(), is(true));
        assertThat(testContext, deleteResponse.getVersion(), greaterThan(0L));
    }

    private JsonObject readConfig() {
        return readJson("config.json");
    }

    private JsonObject readJson(String path) {

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        try (Scanner scanner = new Scanner(cl.getResourceAsStream(path)).useDelimiter("\\A")) {
            String s = scanner.next();
            return new JsonObject(s);
        }

    }

}

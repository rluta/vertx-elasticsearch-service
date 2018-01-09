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
import com.hubrick.vertx.elasticsearch.RxElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.impl.DefaultRxElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.impl.DefaultRxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.CompletionSuggestOption;
import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteOptions;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.RefreshPolicy;
import com.hubrick.vertx.elasticsearch.model.ScriptSortOption;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.model.SuggestOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

/**
 * {@link ElasticSearchServiceVerticle} integration test
 */
public abstract class IntegrationTestBase extends AbstractVertxIntegrationTest {

    private ElasticSearchService service;
    private ElasticSearchAdminService adminService;
    private RxElasticSearchService rxService;
    private RxElasticSearchAdminService rxAdminService;

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
    public void testIndex(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        IndexOptions options = new IndexOptions().setId(id);
        rxService.index(index, type, source, options)
                .subscribe(
                        indexResponse -> {
                            assertThat(testContext, indexResponse.getIndex(), is(index));
                            assertThat(testContext, indexResponse.getType(), is(type));
                            assertThat(testContext, indexResponse.getId(), is(id));
                            assertThat(testContext, indexResponse.getCreated(), is(true));
                            assertThat(testContext, indexResponse.getVersion(), greaterThan(0l));

                            // Give elasticsearch time to index the document
                            vertx.setTimer(1000, id -> async.complete());
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testGet(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        IndexOptions options = new IndexOptions().setId(id);
        rxService.index(index, type, source, options)
                .flatMap(indexResponse -> rxService.get(index, type, id))
                .subscribe(
                        getResponse -> {
                            assertThat(testContext, getResponse.getResult().getIndex(), is(index));
                            assertThat(testContext, getResponse.getResult().getType(), is(type));
                            assertThat(testContext, getResponse.getResult().getId(), is(id));
                            assertThat(testContext, getResponse.getResult().getExists(), is(true));
                            assertThat(testContext, getResponse.getResult().getVersion(), greaterThan(0l));

                            assertThat(testContext, getResponse.getResult().getSource(), notNullValue());
                            assertThat(testContext, getResponse.getResult().getSource().getString("user"), is(source_user));
                            assertThat(testContext, getResponse.getResult().getSource().getString("message"), is(source_message));

                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testSearchSimple(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        SearchOptions options = new SearchOptions()
                .setTimeoutInMillis(1000l)
                .setSize(10)
                .setFrom(10)
                .setSourceIncludes(Arrays.asList("user", "message"))
                .addFieldSort("user", SortOrder.DESC)
                .addScripSort("doc['message']", "groovy", ScriptSortOption.Type.STRING, new JsonObject().put("param1", ImmutableList.of("1", "2", "3")), SortOrder.ASC)
                .addScriptField("script_field", "doc['message']", "groovy", new JsonObject().put("param1", ImmutableList.of("1", "2", "3")))
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        rxService.search(index, options)
                .subscribe(
                        json -> {
                            assertThat(testContext, json, notNullValue());
                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testSearch_EventBusInvalidEnum(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject json = new JsonObject()
                .put("indices", new JsonArray().add(index))
                .put("options", new JsonObject().put("searchType", "invalid_type"));

        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "search");

        vertx.eventBus().<JsonObject>send("et.elasticsearch", json, options, res -> {
            assertThat(testContext, res.failed(), is(true));
            Throwable t = res.cause();
            assertThat(testContext, t, instanceOf(ReplyException.class));
            async.complete();
        });
    }

    @Test
    public void testScroll(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        SearchOptions options = new SearchOptions()
                .setSearchType(SearchType.DEFAULT)
                .setScroll("5m")
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

        rxService.index(index, type, source, indexOptions)
                .flatMap(indexResponse -> {
                    final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
                    vertx.setTimer(2000l, event -> observableFuture.toHandler().handle(Future.succeededFuture()));
                    return observableFuture;
                })
                .flatMap(aVoid -> rxService.search(index, options))
                .flatMap(searchResponse -> {
                    String scrollId = searchResponse.getScrollId();
                    assertThat(testContext, scrollId, notNullValue());
                    assertThat(testContext, searchResponse.getHits().getHits().size(), greaterThan(0));

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
    public void testSuggest(TestContext testContext) throws Exception {

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
                    final SuggestOptions options = new SuggestOptions();
                    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
                            .setText("v")
                            .setField("message_suggest");
                    options.addSuggestion("test-suggest", completionSuggestOption);

                    return rxService.suggest(index, options);
                })
                .subscribe(
                        suggestResponse -> {
                            assertThat(testContext, suggestResponse.getSuggestions().get("test-suggest"), notNullValue());
                            assertThat(testContext, suggestResponse.getSuggestions().get("test-suggest").getSize(), is(1));
                            assertThat(testContext, suggestResponse.getSuggestions().get("test-suggest").getEntries().get(0), notNullValue());
                            assertThat(testContext, suggestResponse.getSuggestions().get("test-suggest").getEntries().get(0).getLength(), is(1));
                            assertThat(testContext, suggestResponse.getSuggestions().get("test-suggest").getEntries().get(0).getOptions().get(0).getText(), is(source_message));

                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void testDeleteByQuery_Simple(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        final UUID documentId = UUID.randomUUID();
        IndexOptions indexOptions = new IndexOptions().setId(documentId.toString());

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
                            assertThat(testContext, deleteByQueryResponse.getTimedOut(), is(false));
                            assertThat(testContext, deleteByQueryResponse.getDeleted(), is(1L));
                            assertThat(testContext, deleteByQueryResponse.getFailures().size(), is(0));

                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
    }

    @Test
    public void test8BulkIndex(TestContext testContext) throws Exception {

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

        final IndexOptions options = new IndexOptions().setId(id);
        rxService.bulkIndex(index, type, ImmutableList.of(source1, source2), options)
            .subscribe(
                bulkIndexResponse -> {
                    assertThat(testContext, bulkIndexResponse.getResponses().size(), is(2));
                    assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getId()).filter(e -> e != null).collect(Collectors.toList()), hasSize(2));
                    assertThat(testContext, bulkIndexResponse.getResponses().stream().map(e -> e.getShards()).filter(e -> e != null).collect(Collectors.toList()), hasSize(2));

                    assertThat(testContext, bulkIndexResponse.getTookInMillis(), greaterThanOrEqualTo(0l));

                    // Give elasticsearch time to index the document
                    vertx.setTimer(1000, id -> async.complete());
                },
                error -> testContext.fail(error)
            );
    }

    @Test
    public void testDelete(TestContext testContext) throws Exception {
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
                            assertThat(testContext, deleteResponse.getIndex(), is(index));
                            assertThat(testContext, deleteResponse.getType(), is(type));
                            assertThat(testContext, deleteResponse.getId(), is(documentId.toString()));
                            assertThat(testContext, deleteResponse.getDeleted(), is(true));
                            assertThat(testContext, deleteResponse.getVersion(), greaterThan(0L));

                            async.complete();
                        },
                        error -> testContext.fail(error)
                );
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

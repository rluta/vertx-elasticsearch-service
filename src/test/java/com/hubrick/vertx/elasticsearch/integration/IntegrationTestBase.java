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

import com.hubrick.vertx.elasticsearch.AbstractVertxIntegrationTest;
import com.hubrick.vertx.elasticsearch.CompletionSuggestOption;
import com.hubrick.vertx.elasticsearch.DeleteByQueryOptions;
import com.hubrick.vertx.elasticsearch.ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.ElasticSearchServiceVerticle;
import com.hubrick.vertx.elasticsearch.IndexOptions;
import com.hubrick.vertx.elasticsearch.ScriptSortOption;
import com.hubrick.vertx.elasticsearch.SearchOptions;
import com.hubrick.vertx.elasticsearch.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.SuggestOptions;
import com.hubrick.vertx.elasticsearch.VertxMatcherAssert;
import com.hubrick.vertx.elasticsearch.impl.DefaultElasticSearchService;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collections;
import java.util.Scanner;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * {@link ElasticSearchServiceVerticle} integration test
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class IntegrationTestBase extends AbstractVertxIntegrationTest {

    private ElasticSearchService service;
    private ElasticSearchAdminService adminService;
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
    }

    @After
    public void tearDown() throws Exception {
        service.stop();
        destroyVerticle();
    }

    @Test
    public void test1Index(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject source = new JsonObject()
                .put("user", source_user)
                .put("message", source_message)
                .put("obj", new JsonObject()
                        .put("array", new JsonArray()
                                .add("1")
                                .add("2")));

        IndexOptions options = new IndexOptions().setId(id);
        service.index(index, type, source, options, result -> {

            VertxMatcherAssert.assertThat(testContext, result.succeeded(), is(true));
            JsonObject json = result.result();

            VertxMatcherAssert.assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_INDEX), is(index));
            VertxMatcherAssert.assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_TYPE), is(type));
            VertxMatcherAssert.assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_ID), is(id));
            VertxMatcherAssert.assertThat(testContext, json.getInteger(DefaultElasticSearchService.CONST_VERSION, 0), greaterThan(0));

            // Give elasticsearch time to index the document
            vertx.setTimer(1000, id -> async.complete());

        });
    }

    @Test
    public void test2Get(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        service.get(index, type, id, result -> {

            VertxMatcherAssert.assertThat(testContext, result.succeeded(), is(true));
            JsonObject body = result.result();

            VertxMatcherAssert.assertThat(testContext, body.getString(DefaultElasticSearchService.CONST_INDEX), is(index));
            VertxMatcherAssert.assertThat(testContext, body.getString(DefaultElasticSearchService.CONST_TYPE), is(type));
            VertxMatcherAssert.assertThat(testContext, body.getString(DefaultElasticSearchService.CONST_ID), is(id));
            VertxMatcherAssert.assertThat(testContext, body.getInteger(DefaultElasticSearchService.CONST_VERSION, 0), greaterThan(0));

            JsonObject source = body.getJsonObject(DefaultElasticSearchService.CONST_SOURCE);
            VertxMatcherAssert.assertThat(testContext, source, notNullValue());
            VertxMatcherAssert.assertThat(testContext, source.getString("user"), is(source_user));
            VertxMatcherAssert.assertThat(testContext, source.getString("message"), is(source_message));

            async.complete();

        });
    }

    @Test
    public void test3Search_Simple(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        SearchOptions options = new SearchOptions()
                .setTimeout("1000")
                .setSize(10)
                .setFrom(10)
                .addField("user")
                .addField("message")
                .addFieldSort("user", SortOrder.DESC)
                .addScripSort("doc['message']", ScriptSortOption.Type.STRING, Collections.emptyMap(), SortOrder.ASC)
                .addScriptField("script_field", "doc['message']", Collections.emptyMap())
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        service.search(index, options, result -> {

            VertxMatcherAssert.assertThat(testContext, result.succeeded(), is(true));
            JsonObject json = result.result();
            VertxMatcherAssert.assertThat(testContext, json, notNullValue());
            async.complete();

        });
    }

    @Test
    public void test4Search_EventBus_Invalid_Enum(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject json = new JsonObject()
                .put("indices", new JsonArray().add(index))
                .put("options", new JsonObject().put("templateType", "invalid_type"));

        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "search");

        vertx.eventBus().<JsonObject>send("et.elasticsearch", json, options, res -> {
            VertxMatcherAssert.assertThat(testContext, res.failed(), is(true));
            Throwable t = res.cause();
            VertxMatcherAssert.assertThat(testContext, t, instanceOf(ReplyException.class));
            async.complete();
        });
    }

    @Test
    public void test5Scroll(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        SearchOptions options = new SearchOptions()
                .setSearchType(SearchType.SCAN)
                .setScroll("5m")
                .setQuery(new JsonObject().put("match_all", new JsonObject()));

        service.search(index, options, result1 -> {

            VertxMatcherAssert.assertThat(testContext, result1.succeeded(), is(true));
            JsonObject json = result1.result();

            String scrollId = json.getString("_scroll_id");
            VertxMatcherAssert.assertThat(testContext, scrollId, notNullValue());

            SearchScrollOptions scrollOptions = new SearchScrollOptions().setScroll("5m");

            service.searchScroll(scrollId, scrollOptions, result2 -> {

                VertxMatcherAssert.assertThat(testContext, result2.succeeded(), is(true));
                JsonObject json2 = result2.result();

                JsonObject hits = json2.getJsonObject("hits");
                VertxMatcherAssert.assertThat(testContext, hits, notNullValue());
                JsonArray hitsArray = hits.getJsonArray("hits");
                VertxMatcherAssert.assertThat(testContext, hitsArray, notNullValue());
                VertxMatcherAssert.assertThat(testContext, hitsArray.size(), greaterThan(0));

                async.complete();
            });

        });
    }

    @Test
    public void test6Suggest(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        JsonObject mapping = readJson("mapping.json");

        adminService.putMapping(index, type, mapping, result1 -> {

            VertxMatcherAssert.assertThat(testContext, result1.succeeded(), is(true));

            JsonObject source = new JsonObject()
                    .put("user", source_user)
                    .put("message", source_message)
                    .put("message_suggest", source_message);


            service.index(index, type, source, result2 -> {

                VertxMatcherAssert.assertThat(testContext, result2.succeeded(), is(true));

                // Delay 1s to give time for indexing
                vertx.setTimer(1000, id -> {
                    final SuggestOptions options = new SuggestOptions();
                    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
                            .setText("v")
                            .setField("message_suggest");
                    options.addSuggestion("test-suggest", completionSuggestOption);

                    service.suggest(index, options, result3 -> {

                        VertxMatcherAssert.assertThat(testContext, result3.succeeded(), is(true));
                        JsonObject json = result3.result();

                        VertxMatcherAssert.assertThat(testContext, json.getJsonArray("test-suggest"), notNullValue());
                        VertxMatcherAssert.assertThat(testContext, json.getJsonArray("test-suggest").getJsonObject(0), notNullValue());
                        VertxMatcherAssert.assertThat(testContext, json.getJsonArray("test-suggest").getJsonObject(0).getInteger("length"), is(1));
                        VertxMatcherAssert.assertThat(testContext, json.getJsonArray("test-suggest").getJsonObject(0).getJsonArray("options").getJsonObject(0).getString("text"), is(source_message));

                        async.complete();
                    });
                });

            });
        });
    }

    @Test
    public void test7DeleteByQuery_Simple(TestContext testContext) throws Exception {

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

        service.index(index, type, source, indexOptions, indexResult -> {

            VertxMatcherAssert.assertThat(testContext, indexResult.succeeded(), is(true));

            // Give elasticsearch time to index the document
            vertx.setTimer(2000, id -> {
                DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
                        .setTimeout("1000");

                service.deleteByQuery(index, new JsonObject().put("ids", new JsonObject().put("values", new JsonArray().add(documentId.toString()))), deleteByQueryOptions, deleteByQueryResult -> {

                    VertxMatcherAssert.assertThat(testContext, deleteByQueryResult.succeeded(), is(true));
                    JsonObject json = deleteByQueryResult.result();
                    System.out.println(json);
                    VertxMatcherAssert.assertThat(testContext, json, notNullValue());

                    VertxMatcherAssert.assertThat(testContext, json.getJsonObject("_indices"), notNullValue());
                    VertxMatcherAssert.assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index), notNullValue());
                    VertxMatcherAssert.assertThat(testContext, json.getJsonObject("_indices").getJsonObject("_all"), notNullValue());
                    VertxMatcherAssert.assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index).getInteger("found"), is(1));
                    VertxMatcherAssert.assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index).getInteger("deleted"), is(1));
                    VertxMatcherAssert.assertThat(testContext, json.getJsonObject("_indices").getJsonObject(index).getInteger("failed"), is(0));

                    async.complete();
                });
            });
        });
    }

    @Test
    public void test99Delete(TestContext testContext) throws Exception {

        final Async async = testContext.async();
        service.delete(index, type, id, result -> {

            VertxMatcherAssert.assertThat(testContext, result.succeeded(), is(true));
            JsonObject json = result.result();

            VertxMatcherAssert.assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_INDEX), is(index));
            VertxMatcherAssert.assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_TYPE), is(type));
            VertxMatcherAssert.assertThat(testContext, json.getString(DefaultElasticSearchService.CONST_ID), is(id));
            VertxMatcherAssert.assertThat(testContext, json.getInteger(DefaultElasticSearchService.CONST_VERSION, 0), greaterThan(0));

            async.complete();

        });
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

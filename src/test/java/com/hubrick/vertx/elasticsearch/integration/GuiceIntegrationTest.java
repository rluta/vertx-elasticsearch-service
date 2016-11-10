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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Guice integration tests
 */
public class GuiceIntegrationTest extends IntegrationTestBase {
    @Override
    protected String getVerticleName() {
        return "java-guice:com.hubrick.vertx.elasticsearch.ElasticSearchServiceVerticle";
    }

    @Override
    protected void configure(JsonObject config) {
        config.put("address", "et.elasticsearch")
                .put("guice_binder", new JsonArray()
                        .add("com.hubrick.vertx.elasticsearch.guice.ElasticSearchBinder"));
    }
}

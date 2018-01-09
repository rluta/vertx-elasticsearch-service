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
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.internal.InternalElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteIndexOptions;
import com.hubrick.vertx.elasticsearch.model.MappingOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.common.xcontent.XContentType;

import javax.inject.Inject;
import java.util.List;

/**
 * Default implementation of {@link ElasticSearchAdminService}
 */
public class DefaultElasticSearchAdminService implements InternalElasticSearchAdminService {

    private final InternalElasticSearchService service;

    @Inject
    public DefaultElasticSearchAdminService(InternalElasticSearchService service) {
        this.service = service;
    }

    @Override
    public void putMapping(List<String> indices, String type, JsonObject source, MappingOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        final PutMappingRequestBuilder builder = PutMappingAction.INSTANCE.newRequestBuilder(service.getClient())
                .setIndices(indices.toArray(new String[indices.size()]))
                .setType(type)
                .setSource(source.encode(), XContentType.JSON);

        builder.execute(new ActionListener<PutMappingResponse>() {
            @Override
            public void onResponse(PutMappingResponse putMappingResponse) {
                JsonObject json = new JsonObject()
                        .put("acknowledged", putMappingResponse.isAcknowledged());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Exception e) {
                resultHandler.handle(Future.failedFuture(e));
            }
        });
    }

    @Override
    public void createIndex(String index, JsonObject source, CreateIndexOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {

        final CreateIndexRequestBuilder builder = CreateIndexAction.INSTANCE.newRequestBuilder(service.getClient())
                .setIndex(index)
                .setSource(source.encode(), XContentType.JSON);

        builder.execute(new ActionListener<CreateIndexResponse>() {
            @Override
            public void onResponse(CreateIndexResponse createIndexResponse) {
                final JsonObject json = new JsonObject().put("acknowledged", createIndexResponse.isAcknowledged());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Exception e) {
                resultHandler.handle(Future.failedFuture(e));
            }
        });
    }

    @Override
    public void deleteIndex(List<String> indices, DeleteIndexOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
        final DeleteIndexRequestBuilder builder = new DeleteIndexRequestBuilder(service.getClient(), DeleteIndexAction.INSTANCE, indices.toArray(new String[0]));

        builder.execute(new ActionListener<DeleteIndexResponse>() {
            @Override
            public void onResponse(DeleteIndexResponse deleteIndexResponse) {
                final JsonObject json = new JsonObject().put("acknowledged", deleteIndexResponse.isAcknowledged());
                resultHandler.handle(Future.succeededFuture(json));
            }

            @Override
            public void onFailure(Exception e) {
                resultHandler.handle(Future.failedFuture(e));
            }
        });
    }

    /**
     * Returns the inner admin client
     *
     * @return
     */
    @Override
    public AdminClient getAdmin() {
        return service.getClient().admin();
    }

}

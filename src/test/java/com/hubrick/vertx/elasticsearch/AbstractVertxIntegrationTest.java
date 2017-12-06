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

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Emir Dizdarevic
 */
@RunWith(VertxUnitRunner.class)
public abstract class AbstractVertxIntegrationTest {

    protected Vertx vertx;

    protected final void deployVerticle(TestContext testContext, String verticleName, DeploymentOptions deploymentOptions) throws InterruptedException {
        vertx = Vertx.vertx();

        CountDownLatch latch = new CountDownLatch(1);
        vertx.deployVerticle(verticleName, deploymentOptions, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
                testContext.fail();
            }
            latch.countDown();
        });

        latch.await(10, TimeUnit.SECONDS);
    }

    protected final void destroyVerticle() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        vertx.close(event ->  latch.countDown());

        latch.await(30, TimeUnit.SECONDS);
    }

}

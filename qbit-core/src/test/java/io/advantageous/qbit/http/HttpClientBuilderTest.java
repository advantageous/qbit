/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.http;

import io.advantageous.qbit.Factory;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.spi.FactorySPI;
import io.advantageous.qbit.spi.HttpClientFactory;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.boon.Exceptions.die;

public class HttpClientBuilderTest {

    HttpClientBuilder objectUnderTest;
    boolean ok;

    @Before
    public void setUp() throws Exception {

        objectUnderTest = new HttpClientBuilder();

        FactorySPI.setFactory(new Factory() {

            @Override
            public HttpClient createHttpClient(String host, int port, int requestBatchSize,
                                               int timeOutInMilliseconds, int poolSize, boolean autoFlush,
                                               int flushRate,
                                               boolean keepAlive, boolean pipeline) {
                return FactorySPI.getHttpClientFactory().create(host, port, requestBatchSize,
                        timeOutInMilliseconds, poolSize, autoFlush, flushRate, keepAlive, pipeline);
            }
        });

        FactorySPI.setHttpClientFactory(new HttpClientFactory() {

            @Override
            public HttpClient create(String host, int port, int requestBatchSize, int timeOutInMilliseconds, int poolSize, boolean autoFlush, int flushRate, boolean keepAlive, boolean pipeLine) {
                return null;
            }
        });

        Sys.sleep(100);

    }


    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetHost() throws Exception {

        ok = objectUnderTest.setHost("host").getHost().equals("host") || die();
        ok = objectUnderTest.setHost("localhost").getHost().equals("localhost") || die();
        ok = objectUnderTest.setAutoFlush(true).isAutoFlush() || die();
        ok = !objectUnderTest.setAutoFlush(false).isAutoFlush() || die();
        ok = objectUnderTest.setPollTime(7).getPollTime() == 7 || die();
        ok = objectUnderTest.setPoolSize(11).getPoolSize() == 11 || die();
        ok = objectUnderTest.setPort(9090).getPort() == 9090 || die();
        ok = objectUnderTest.setPort(8080).getPort() == 8080 || die();
        ok = objectUnderTest.setRequestBatchSize(13).getRequestBatchSize() == 13
                || die();
        ok = objectUnderTest.setTimeOutInMilliseconds(113).getTimeOutInMilliseconds() == 113
                || die();

        objectUnderTest.build();

    }
}
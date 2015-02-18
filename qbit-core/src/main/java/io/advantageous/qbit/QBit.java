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

package io.advantageous.qbit;

import io.advantageous.qbit.spi.FactorySPI;
import org.boon.core.reflection.ClassMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.boon.Boon.puts;

/**
 * Main interface to QBit.
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public class QBit {
    private Logger logger = LoggerFactory.getLogger(QBit.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();

    public static Factory factory() {
        return new QBit().doGetFactory();
    }

    public Factory doGetFactory() {
        Factory factory = FactorySPI.getFactory();

        if (factory == null) {

            if (debug) {
                puts("Factory was null");
            }

            registerReflectionAndJsonParser();
            registerNetworkStack();
            return FactorySPI.getFactory();
        }

        return factory;
    }

    private void registerReflectionAndJsonParser() {
        try {
            final Class<?> boonFactory = Class.forName("io.advantageous.qbit.spi.RegisterBoonWithQBit");
            ClassMeta.classMeta(boonFactory).invokeStatic("registerBoonWithQBit");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not find reflection lib or JSON parser");
        }
    }

    private void registerNetworkStack() {

        try {

            try {
                final Class<?> vertxFactory = Class.forName("io.advantageous.qbit.vertx.RegisterVertxWithQBit");
                ClassMeta.classMeta(vertxFactory).invokeStatic("registerVertxWithQBit");

            } catch (Exception ex) {


                if (debug) {
                    puts("Unable to load vertx network stack, trying Jetty", ex);
                }

                final Class<?> vertxFactory = Class.forName("io.advantageous.qbit.http.jetty.RegisterJettyWithQBit");
                ClassMeta.classMeta(vertxFactory).invokeStatic("registerJettyWithQBit");
            }
        } catch (Exception ex) {
            FactorySPI.setHttpServerFactory((options, requestQueueBuilder, responseQueueBuilder,
                                             webSocketMessageQueueBuilder, systemManager) -> {

                throw new IllegalStateException("Unable to load Vertx or Jetty network libs");
            });


            FactorySPI.setHttpClientFactory((host, port, requestBatchSize, timeOutInMilliseconds, poolSize,
                                             autoFlush, flushRate, keepAlive, pipeLine) -> {
                throw new IllegalStateException("Unable to load Vertx or Jetty network libs");
            });
        }
    }

}

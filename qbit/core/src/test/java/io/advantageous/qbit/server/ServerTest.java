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

package io.advantageous.qbit.server;

import io.advantageous.boon.core.Sets;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.boon.spi.BoonJsonMapper;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceBundleBuilder;
import io.advantageous.qbit.service.bundle.example.todo.Todo;
import io.advantageous.qbit.service.bundle.example.todo.TodoService;
import io.advantageous.qbit.spi.ProtocolEncoder;
import io.advantageous.qbit.spi.ProtocolParser;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.*;

import static io.advantageous.boon.core.Exceptions.die;
import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.boon.json.JsonFactory.fromJsonArray;

public class ServerTest extends TimedTesting {

    @Test
    public void testServer() {

        super.setupLatch();

        ProtocolEncoder encoder = QBit.factory().createEncoder();

        ProtocolParser parser = QBit.factory().createProtocolParser();
        MockHttpServer httpServer = new MockHttpServer();
        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").build();

        JsonMapper mapper = new BoonJsonMapper();

        ServiceEndpointServerImpl server = new ServiceEndpointServerImpl(httpServer, encoder,
                parser, serviceBundle, mapper, 30, 100, 30, 10, null, null, null, null, 8080, 0, null, null, 50, 2, 2);

        server.initServices(Sets.set(new TodoService()));

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        server.start();

        httpServer.postRequestObject("/services/todo-manager/todo", new Todo("Call Dad", "Call Dad", new Date()), (code, mimeType, body) -> {

            puts("CALL CALLED", body, "\n\n");
            if (body != null && code == 202 && body.equals("\"success\"")) {
                resultsWorked.set(true);
            }
        });

        waitForTrigger(20, o -> resultsWorked.get());

        if (!resultsWorked.get()) {
            die("Add operation did not work");
        }

        resultsWorked.set(false);

        httpServer.sendHttpGet("/services/todo-manager/todo/list/", null, (code, mimeType, body) -> {

            puts("ADD CALL RESPONSE code ", code, " mimeType ", mimeType, " body ", body);

            List<Todo> todos = fromJsonArray(body, Todo.class);
            if (todos.size() > 0) {
                Todo todo = todos.get(0);
                if (todo.getDescription().equals("Call Dad")) {
                    resultsWorked.set(true);
                }
            }
        });

        server.serviceBundle().flushSends();
        Sys.sleep(100);
        server.flush();
        Sys.sleep(100);

        waitForTrigger(20, o -> resultsWorked.get());

        if (!resultsWorked.get()) {
            die("List operation did not work");
        }

    }

    @Test
    public void testServerTimeout() {

        ProtocolEncoder encoder = QBit.factory().createEncoder();
        MockHttpServer httpServer = new MockHttpServer();

        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/services").build();
        JsonMapper mapper = new BoonJsonMapper();

        ServiceEndpointServerImpl server = new ServiceEndpointServerImpl(httpServer, encoder,
                QBit.factory().createProtocolParser(), serviceBundle, mapper, 1, 100, 30,
                10, null,
                null, null, null, 8080, 0, null, null, 50, 2, 2);

        server.initServices(Sets.set(new TodoService()));

        server.start();

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        for (int index = 0; index < 100; index++) {

            httpServer.sendHttpGet("/services/todo-manager/timeout", null, (code, mimeType, body) -> {

                if (code == 408 && body != null && body.equals("\"timed out\"")) {
                    resultsWorked.set(true);
                }
            });
        }

        server.serviceBundle().flushSends();
        Sys.sleep(100);
        Sys.sleep(100);
        server.flush();
        Sys.sleep(100);

        waitForTrigger(8, o -> resultsWorked.get());

        if (!resultsWorked.get()) {
            die(" we did not get timeout");
        }

        resultsWorked.set(false);

    }

    @Test
    public void testNoMethodCallFound() {

        ProtocolEncoder encoder = QBit.factory().createEncoder();
        MockHttpServer httpServer = new MockHttpServer();

        final ServiceBundle serviceBundle = new ServiceBundleBuilder().setAddress("/root").build();

        JsonMapper mapper = new BoonJsonMapper();

        ServiceEndpointServerImpl server = new ServiceEndpointServerImpl(
                httpServer, encoder, QBit.factory().createProtocolParser(), serviceBundle, mapper, 1, 100, 30, 10,
                null, null, null, null, 8080, 0, null, null, 50, 2, 2);

        server.initServices(new TodoService());

        server.start();

        final AtomicBoolean resultsWorked = new AtomicBoolean();

        resultsWorked.set(false);

        httpServer.sendHttpGet("/services/todo-manager/testNoMethodCallFound", null, (code, mimeType, body) -> {

            if (code == 404) {
                resultsWorked.set(true);
            }
        });

        server.serviceBundle().flushSends();
        Sys.sleep(100);
        Sys.sleep(100);
        server.flush();
        Sys.sleep(100);

        waitForTrigger(20, o -> resultsWorked.get());

        if (!resultsWorked.get()) {
            die(" we did not get timeout");
        }

        resultsWorked.set(false);

    }

}
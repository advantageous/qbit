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

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceQueue;

import java.util.function.Consumer;

/**
 * Represents a server that marshals method calls to a service.
 *
 * @author richardhightower@gmail.com (Rick Hightower)
 * @author gcc@rd.io (Geoff Chandler)
 */
public interface ServiceEndpointServer extends Server {

    default ServiceEndpointServer initServices(Object... services) {
        throw new IllegalStateException("Not implemented");
    }

    default ServiceEndpointServer initServices(Iterable<Object> services) {
        throw new IllegalStateException("Not implemented");
    }

    default ServiceBundle serviceBundle() {
        throw new IllegalStateException("Not implemented");

    }

    default ServiceEndpointServer addServiceQueue(String address, ServiceQueue serviceQueue) {
        throw new IllegalStateException("Not implemented");
    }

    default ServiceEndpointServer addServiceConsumer(String address, Consumer<MethodCall<Object>> service) {
        serviceBundle().addServiceObject(address, service);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    default ServiceEndpointServer addServiceObject(String address, Object serviceObject) {
        serviceBundle().addServiceObject(address, serviceObject);
        return this;
    }

    default ServiceEndpointServer flush() {
        throw new IllegalStateException("Not implemented");
    }

    default ServiceEndpointServer startServer() {
        start();
        return this;
    }

    default ServiceEndpointServer startServerNotifyStart(Runnable runnable) {
        startWithNotify(runnable);
        return this;
    }


    default ServiceEndpointServer startServerAndWait() {
        start();
        return this;
    }

}

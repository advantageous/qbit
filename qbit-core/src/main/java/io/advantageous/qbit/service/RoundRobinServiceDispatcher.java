/*******************************************************************************
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
*  QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
*   http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
*   http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
*   http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
*   http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
*   http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
*/

package io.advantageous.qbit.service;


import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.queue.SendQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author  rhightower
 * on 2/18/15.
 */
public class RoundRobinServiceDispatcher implements Consumer<MethodCall<Object>>, ServiceFlushable, Stoppable{

    private List<Service> services = new ArrayList<>();


    private List<SendQueue<MethodCall<Object>>> sendQueues = new ArrayList<>();

    private AtomicInteger index = new AtomicInteger();

    private final boolean startServices;

    public RoundRobinServiceDispatcher(boolean startServices) {
        this.startServices = startServices;
    }


    public RoundRobinServiceDispatcher() {
        this.startServices = true;
    }


    public RoundRobinServiceDispatcher addService(Service service) {
        services.add(service);
        return this;
    }

    public RoundRobinServiceDispatcher start() {

        services = Collections.unmodifiableList(services);



        if (startServices) {
            for (Service service : services) {
                service.start();
            }
        }

        for (Service service : services) {
            sendQueues.add(service.requests());
        }

        return this;
    }

    @Override
    public void accept(MethodCall<Object> methodCall) {


        int localIndex = index.getAndIncrement() % services.size();

        final SendQueue<MethodCall<Object>> methodCallSendQueue = sendQueues.get(localIndex);
        methodCallSendQueue.sendAndFlush(methodCall);

    }

    @Override
    public void flush() {
        for (Service service : services) {
            service.flush();
        }
    }

    @Override
    public void stop() {

        for (Service service : services) {
            service.stop();
        }
    }
}

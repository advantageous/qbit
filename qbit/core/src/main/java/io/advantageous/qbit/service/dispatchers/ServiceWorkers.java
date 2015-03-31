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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 ******************************************************************************/

package io.advantageous.qbit.service.dispatchers;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.boon.core.reflection.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author  rhightower
 * on 2/18/15.
 */
public class ServiceWorkers implements ServiceMethodDispatcher {


    private final int flushInterval;
    private final TimeUnit timeUnit;

    public static RoundRobinServiceDispatcher workers(int flushInterval, TimeUnit timeUnit) {
        return new RoundRobinServiceDispatcher(flushInterval, timeUnit);
    }

    public static RoundRobinServiceDispatcher workers() {
        return new RoundRobinServiceDispatcher();
    }

    public static ShardedMethodDispatcher shardedWorkers(final ShardRule shardRule) {
        return new ShardedMethodDispatcher(shardRule);
    }

    public static ShardedMethodDispatcher shardedWorkers(int flushInterval, TimeUnit timeUnit, final ShardRule shardRule) {
        return new ShardedMethodDispatcher(flushInterval, timeUnit, shardRule);
    }


    public static ShardedMethodDispatcher shardOnFirstArgumentWorkers() {
        return new ShardedMethodDispatcher((methodName, methodArgs, numWorkers) -> {
            int shardKey =  methodArgs[0].hashCode() % numWorkers;
            return shardKey;
        });
    }


    public static ShardedMethodDispatcher shardOnSecondArgumentWorkers() {
        return new ShardedMethodDispatcher((methodName, methodArgs, numWorkers) -> {
            int shardKey =  methodArgs[1].hashCode() % numWorkers;
            return shardKey;
        });
    }


    public static ShardedMethodDispatcher shardOnThirdArgumentWorkers() {
        return new ShardedMethodDispatcher((methodName, methodArgs, numWorkers) -> {
            int shardKey =  methodArgs[2].hashCode() % numWorkers;
            return shardKey;
        });
    }


    public static ShardedMethodDispatcher shardOnFourthArgumentWorkers() {
        return new ShardedMethodDispatcher((methodName, methodArgs, numWorkers) -> {
            int shardKey =  methodArgs[3].hashCode() % numWorkers;
            return shardKey;
        });
    }


    public static ShardedMethodDispatcher shardOnFifthArgumentWorkers() {
        return new ShardedMethodDispatcher((methodName, methodArgs, numWorkers) -> {
            int shardKey =  methodArgs[4].hashCode() % numWorkers;
            return shardKey;
        });
    }


    public static ShardedMethodDispatcher shardOnBeanPath(final String beanPath) {
        return new ShardedMethodDispatcher((methodName, methodArgs, numWorkers) -> {
            int shardKey =  BeanUtils.idx(methodArgs, beanPath).hashCode() % numWorkers;
            return shardKey;
        });
    }





    protected final boolean startServices;
    protected List<ServiceQueue> serviceQueues = new ArrayList<>();
    protected List<SendQueue<MethodCall<Object>>> sendQueues = new ArrayList<>();
    protected AtomicInteger index = new AtomicInteger();


    public ServiceWorkers(boolean startServices) {

        this.startServices = startServices;
        this.flushInterval = 50;
        this.timeUnit = TimeUnit.MILLISECONDS;
    }



    public ServiceWorkers(int flushInterval, TimeUnit timeUnit) {

        this.startServices = true;
        this.flushInterval = flushInterval;
        this.timeUnit = timeUnit;

    }

    public ServiceWorkers() {
        this.startServices = true;
        this.flushInterval = 50;
        this.timeUnit = TimeUnit.MILLISECONDS;
    }


    public ServiceWorkers addService(ServiceQueue serviceQueue) {
        serviceQueues.add(serviceQueue);
        return this;
    }

    public ServiceWorkers addServices(ServiceQueue... servicesArray) {

        for (ServiceQueue serviceQueue : servicesArray) {
            addService(serviceQueue);
        }
        return this;
    }

    public ServiceWorkers start() {

        serviceQueues = Collections.unmodifiableList(serviceQueues);



        if (startServices) {
            for (ServiceQueue serviceQueue : serviceQueues) {
                serviceQueue.start();
            }
        }

        for (ServiceQueue serviceQueue : serviceQueues) {
            if (flushInterval > 0) {
                SendQueue<MethodCall<Object>> methodCallSendQueue = serviceQueue.requestsWithAutoFlush(flushInterval, timeUnit);
                methodCallSendQueue.start();
                sendQueues.add(methodCallSendQueue);
            }else {
                sendQueues.add(serviceQueue.requests());
            }
        }

        return this;
    }

    public void accept(MethodCall<Object> methodCall) {


        int localIndex = index.getAndIncrement() % serviceQueues.size();

        final SendQueue<MethodCall<Object>> methodCallSendQueue = sendQueues.get(localIndex);
        methodCallSendQueue.send(methodCall);

    }

    public void flush() {

        for (SendQueue sendQueue : sendQueues) {
            sendQueue.flushSends();
        }

        for (ServiceQueue serviceQueue : serviceQueues) {
            serviceQueue.flush();
        }
    }

    public void stop() {


        for (SendQueue sendQueue : sendQueues) {
            sendQueue.stop();
        }

        for (ServiceQueue serviceQueue : serviceQueues) {
            serviceQueue.stop();
        }
    }
}

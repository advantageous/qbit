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

package io.advantageous.qbit.system;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.server.Server;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.ServiceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static io.advantageous.boon.Boon.puts;

/**
 * Created by rhightower on 2/11/15.
 */
public class QBitSystemManager {

    private final List<ServiceQueue> serviceQueueList = new CopyOnWriteArrayList<>();
    private final List<ServiceBundle> serviceBundleList = new CopyOnWriteArrayList<>();
    private final List<Server> serverList = new CopyOnWriteArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(QBitSystemManager.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private boolean coreSystemShutdown;
    private volatile int countTracked;

    private CountDownLatch countDownLatch;

    public QBitSystemManager() {
        this(true, true);
    }

    public QBitSystemManager(final boolean coreSystemShutdown, final boolean handleShutDownHook) {
        this.coreSystemShutdown = coreSystemShutdown;

        if (handleShutDownHook) {
            /* Shutdown gracefully for CTRl-C etc. */
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {

                    logger.info("QBit shutting down gracefully... " + new Date());
                    shutDown();
                    waitForShutdown();
                    if (logger.isInfoEnabled()) {
                        logger.info("QBit shutdown gracefully " + new Date());
                    }
                }
            });
        }
    }

    public void registerService(final ServiceQueue serviceQueue) {
        if (debug) puts("registerService", serviceQueue);
        countTracked++;
        serviceQueueList.add(serviceQueue);
    }


    public void registerServer(final Server server) {
        countTracked++;
        serverList.add(server);
    }


    public void registerServiceBundle(final ServiceBundle bundle) {
        countTracked++;
        serviceBundleList.add(bundle);
    }

    public void shutDown() {

        for (ServiceQueue serviceQueue : serviceQueueList) {
            try {
                serviceQueue.stop();
            } catch (Exception ex) {

                if (debug) {
                    logger.debug("Unable to shutdown service", ex);
                }
            }
        }

        for (Server server : serverList) {
            try {
                server.stop();
            } catch (Exception ex) {

                if (debug) {
                    logger.debug("Unable to shutdown server", ex);
                }
            }
        }


        for (ServiceBundle bundle : serviceBundleList) {
            try {
                bundle.stop();
            } catch (Exception ex) {

                if (debug) {
                    logger.debug("Unable to shutdown bundle", ex);
                }
            }
        }

        countTracked = 0;


        if (coreSystemShutdown) {
            QBit.factory().shutdownSystemEventBus();
        }



    }

    public void serviceShutDown() {


        if (countDownLatch!=null) {
            countDownLatch.countDown();
        }

        if (debug) puts("serviceShutDown", countDownLatch.getCount());
    }

    public void waitForShutdown() {


        if (countDownLatch == null) {
            countDownLatch = new CountDownLatch(countTracked);

        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            if (debug) logger.debug("done", e);
        }

        if (coreSystemShutdown) {
            QBit.factory().shutdownSystemEventBus();
        }

        if (debug) puts("Shutdown complete!");
    }

    public void startAll() {
        serviceQueueList.forEach(ServiceQueue::start);
        serverList.forEach(Server::start);
        serviceBundleList.forEach(ServiceBundle::start);
        countDownLatch = new CountDownLatch(countTracked);


        if (debug) puts("startAll", countDownLatch.getCount());
        if (debug) puts("startAll", countTracked);
    }
}

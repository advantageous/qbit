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

package io.advantageous.qbit.system;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.server.Server;
import io.advantageous.qbit.service.Service;
import io.advantageous.qbit.service.ServiceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static org.boon.Boon.puts;

/**
 * Created by rhightower on 2/11/15.
 */
public class QBitSystemManager {

    private final List<Service> serviceList = new CopyOnWriteArrayList<>();
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

    public void registerService(final Service service) {
        if (debug) puts("registerService", service);
        countTracked++;
        serviceList.add(service);
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

        for (Service service : serviceList) {
            try {
                service.stop();
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


    }

    public void serviceShutDown() {

        countDownLatch.countDown();

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
        serviceList.forEach(Service::start);
        serverList.forEach(Server::start);
        serviceBundleList.forEach(ServiceBundle::start);
        countDownLatch = new CountDownLatch(countTracked);


        if (debug) puts("startAll", countDownLatch.getCount());
        if (debug) puts("startAll", countTracked);
    }
}

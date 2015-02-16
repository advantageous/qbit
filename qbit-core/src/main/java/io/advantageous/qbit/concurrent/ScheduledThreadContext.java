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

package io.advantageous.qbit.concurrent;

import io.advantageous.qbit.GlobalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.boon.Boon.sputs;

/**
 * Created by rhightower on 2/13/15.
 */
public class ScheduledThreadContext implements ExecutorContext {
    private final Logger logger = LoggerFactory.getLogger(ScheduledThreadContext.class);
    private final boolean debug = false || GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final int priority;
    private final boolean daemon;
    private final Runnable runnable;
    private final int initialDelay;
    private final int period;
    private final TimeUnit unit;
    private final String threadName;
    private final String description;
    private ScheduledExecutorService monitor;
    private ScheduledFuture<?> future;


    public ScheduledThreadContext(final Runnable runnable,
                                  final int initialDelay,
                                  final int period,
                                  final TimeUnit unit,
                                  final String threadName,
                                  final String description, int priority, boolean daemon) {
        this.initialDelay = initialDelay;
        this.period = period;
        this.unit = unit;
        this.runnable = runnable;
        this.threadName = threadName;
        this.description = description;
        this.priority = priority;
        this.daemon = daemon;
    }

    @Override
    public void start() {

        if (debug) {
            logger.debug(sputs("Started:", description));
        }

        if (monitor != null) {
            throw new IllegalStateException(description + " Must be stopped before it can be started");
        }
        monitor = Executors.newScheduledThreadPool(1,
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName(threadName);
                    thread.setPriority(priority);
                    if (daemon) thread.setDaemon(daemon);
                    return thread;
                }
        );
        /** This wants to be configurable. */
        future = monitor.scheduleAtFixedRate(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error("Problem running: " + description, ex);
            }
        }, initialDelay, period, unit);
    }

    @Override
    public void stop() {

        if (debug) {
            logger.debug(sputs("Stopped:", description));
        }


        try {
            if (future != null) {
                future.cancel(true);
                future = null;
            }

        } catch (Exception ex) {

            logger.error("Problem stopping: " + description + "- Can't cancel timer", ex);
        }

        try {

            if (monitor != null) {
                monitor.shutdown();
                monitor = null;
            }
        } catch (Exception ex) {


            logger.error("Problem stopping: " + description + "- Can't shutdown monitor", ex);
        }

        future = null;
        monitor = null;

    }
}

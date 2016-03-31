/**
 * ****************************************************************************
 * <p>
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 * http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
 * http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
 * http://rick-hightower.blogspot.com/2015/01/quick-startClient-qbit-programming.html
 * http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
 * http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 * <p>
 * ****************************************************************************
 */
package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.client.ClientProxy;

/**
 * Collects stats
 * This collects key performance indicators: timings, counts and levels/gauges.
 * Created by rick on 6/6/15.
 */
public interface StatsCollector extends ClientProxy {


    /**
     * Increment a counter by 1.
     * This is a short cut for recordCount(name, 1);
     *
     * @param name name name of metric, KPI, metric.
     */
    default void increment(final String name) {
        recordCount(name, 1);
    }

    /**
     * Record a a count.
     * Used to record things like how many users used the site.
     *
     * @param name  name of the metric, KPI, stat
     * @param count count to record.
     */
    default void recordCount(String name, long count) {
    }

    /**
     * This is used to record things like the count of current threads or
     * free system memory or free disk, etc.
     * Record Level. Some systems call this a gauge.
     *
     * @param name  name of the gauge or level
     * @param level level
     */
    default void recordLevel(String name, long level) {
    }

    /**
     * This is used to record timings.
     * This would be things like how long did it take this service to call this remote service.
     *
     * @param name     name of the timing
     * @param duration duration
     */
    default void recordTiming(String name, long duration) {
    }

}

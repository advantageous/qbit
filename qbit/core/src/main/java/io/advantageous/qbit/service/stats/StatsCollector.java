package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.client.ClientProxy;

/**
 * Collects stats
 * Created by rick on 6/6/15.
 */
public interface StatsCollector extends ClientProxy {

    default void increment(String name) {
    }

    default void recordCount(String name, long count) {
    }

    default void recordLevel(String name, long level) {
    }

    default void recordTiming(String name, long duration) {
    }

}

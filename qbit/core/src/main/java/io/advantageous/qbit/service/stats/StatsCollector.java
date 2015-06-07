package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.client.ClientProxy;

/**
 * Collects stats
 * Created by rick on 6/6/15.
 */
public interface StatsCollector extends ClientProxy {

    default void recordCount(String name, int count) {
    }

    default void recordLevel(String name, int level) {
    }

    default void recordTiming(String name, int duration) {
    }

}

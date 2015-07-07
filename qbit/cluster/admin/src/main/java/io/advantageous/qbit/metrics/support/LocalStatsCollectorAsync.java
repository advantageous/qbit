package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.stats.StatCollection;

import java.util.Map;

public interface LocalStatsCollectorAsync extends StatReplicator, StatCollection {

    Map<String, Map<String, ?>> collect(Callback<Map<String, Map<String, ?>>> callback);
}

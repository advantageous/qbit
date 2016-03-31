package io.advantageous.qbit.service.stats;

import io.advantageous.qbit.reactive.Callback;

import java.util.Map;

public interface StatCollection {

    Map<String, Map<String, ?>> collect(Callback<Map<String, Map<String, ?>>> callback);

}

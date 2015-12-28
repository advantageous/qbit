package io.advantageous.qbit.example.perf.websocket;

import io.advantageous.qbit.reactive.Callback;

public interface TradeServiceAsync {


    void trade(Callback<Boolean> callback, final Trade trade);
    void count(Callback<Long> callback);
}

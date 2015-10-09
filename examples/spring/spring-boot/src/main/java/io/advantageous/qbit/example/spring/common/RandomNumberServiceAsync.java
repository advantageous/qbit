package io.advantageous.qbit.example.spring.common;

import io.advantageous.qbit.reactive.Callback;

public interface RandomNumberServiceAsync {
    void getRandom(Callback<Integer> callback, int min, int max);
}

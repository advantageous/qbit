package io.advantageous.qbit.spring.rest;

import io.advantageous.qbit.reactive.Callback;

public interface HelloWorld {
    public void hello(Callback<String> callback);
}

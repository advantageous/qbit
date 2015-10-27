package io.advantageous.qbit.example.websocket.service;

import io.advantageous.qbit.reactive.Callback;

public interface EchoAsync {
    void echo(final Callback<String> echoCallback, String echo);
    void echo1(final Callback<String> echoCallback, String echo);
    void echo2(final Callback<Echo> echoCallback, String echo);

}

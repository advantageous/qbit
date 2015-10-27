package io.advantageous.qbit.example.websocket.service;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.reactive.Callback;

@RequestMapping("/echo")
public class EchoService {


    @RequestMapping("/echo0")
    public String echo(String echo) {
        System.out.println(echo);
        return echo;
    }


    @RequestMapping("/echo1")
    public void echo1(final Callback<String> echoCallback, String echo) {
        System.out.println(echo);
        echoCallback.returnThis(echo);
    }


    @RequestMapping("/echo2")
    public void echo2(final Callback<Echo> echoCallback, String echo) {
        System.out.println(echo);
        echoCallback.returnThis(new Echo(echo));
    }

}

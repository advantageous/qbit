package io.advantageous.qbit.example.hello;

/**
* Created by rhightower on 2/10/15.
*/


public class HelloObject {
    private final String hello;
    private final long time = System.currentTimeMillis();

    public HelloObject(String hello) {
        this.hello = hello;
    }
}



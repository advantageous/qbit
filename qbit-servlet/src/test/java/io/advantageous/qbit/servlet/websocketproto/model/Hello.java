package io.advantageous.qbit.servlet.websocketproto.model;


public class Hello {
    private final String hello;

    public Hello(final String message) {
        this.hello = message;
    }

    public String getHello() {
        return hello;
    }

    @Override
    public String toString() {
        return "Hello{" +
                "hello='" + hello + '\'' +
                '}';
    }
}
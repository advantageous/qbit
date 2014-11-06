package io.advantageous.qbit.spi;

import io.advantageous.qbit.Factory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public class FactorySPI {

    private static final AtomicReference<Factory> ref = new AtomicReference<>();

    private static final AtomicReference<HttpServerFactory> httpServerFactoryRef = new AtomicReference<>();


    public static Factory getFactory() {
        return ref.get();
    }


    public static void setFactory(Factory factory) {
        ref.set(factory);
    }


    public static HttpServerFactory getHttpServerFactory() {
        return httpServerFactoryRef.get();
    }


    public static void setHttpServerFactory(HttpServerFactory factory) {
        httpServerFactoryRef.set(factory);
    }

}

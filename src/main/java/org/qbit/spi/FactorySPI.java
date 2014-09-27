package org.qbit.spi;

import org.qbit.Factory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Richard on 9/26/14.
 */
public class FactorySPI {

    private static final AtomicReference<Factory> ref = new AtomicReference<>();

    static {
        ref.set(new FactoryImpl());
    }

    public static Factory getFactory() {
        return ref.get();
    }


    public static void setFactory(Factory factory) {
        ref.set(factory);
    }
}

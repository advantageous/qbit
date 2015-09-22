package io.advantageous.qbit.vertx.http.util;

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VertxFactory;

import java.util.ServiceLoader;

public class VertxCreate {


    private static VertxFactory vertxFactory;

    static {
        vertxFactory = loadFactory();
    }

    private static VertxFactory loadFactory() {
        ServiceLoader<VertxFactory> factories = ServiceLoader.load(VertxFactory.class);
        return factories.iterator().next();
    }



    /** Spring boot when creating a giant jar
     * has a problem running ServiceLoader multiple times or
     * so it appears. This is a workaround.
     * @return new vertx instance from cache vertxFactory
     */
    public static Vertx newVertx() {
        final ClassMeta<VertxFactory> vertxFactoryClassMeta = ClassMeta.classMeta(VertxFactory.class);

        final Iterable<MethodAccess> createVertxMethods = vertxFactoryClassMeta.methods("vertx");


        for (MethodAccess methodAccess : createVertxMethods) {
            if (methodAccess.method().getParameterCount() == 0) {
                return (Vertx) methodAccess.invoke(vertxFactory);
            }

        }
        throw new IllegalStateException("Unable to load vertx factory");
    }
}

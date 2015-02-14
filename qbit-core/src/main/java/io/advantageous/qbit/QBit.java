package io.advantageous.qbit;

import io.advantageous.qbit.spi.FactorySPI;
import org.boon.core.reflection.ClassMeta;

/**
 * Main interface to QBit.
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public class QBit {

    public static Factory factory() {
        Factory factory =  FactorySPI.getFactory();

        if (factory == null) {
            try {
                final Class<?> boonFactory = Class.forName("io.advantageous.qbit.spi.RegisterBoonWithQBit");
                ClassMeta.classMeta(boonFactory).invokeStatic("registerBoonWithQBit");

                try {
                    final Class<?> vertxFactory = Class.forName("io.advantageous.qbit.vertx.RegisterVertxWithQBit");
                    ClassMeta.classMeta(vertxFactory).invokeStatic("registerVertxWithQBit");

                } catch (Exception ex) {

                    final Class<?> vertxFactory = Class.forName("io.advantageous.qbit.http.jetty.RegisterJettyWithQBit");
                    ClassMeta.classMeta(vertxFactory).invokeStatic("registerJettyWithQBit");
                }
                return FactorySPI.getFactory();

            } catch (ClassNotFoundException e) {
               return null;
            }
        }
        return factory;
    }

}

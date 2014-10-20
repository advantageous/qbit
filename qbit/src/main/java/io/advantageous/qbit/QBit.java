package io.advantageous.qbit;

import io.advantageous.qbit.spi.FactorySPI;

/**
 * Main interface to QBit.
 * Created by Richard on 9/26/14.
 * @author rhightower
 */
public class QBit {

    public static Factory factory() {
        Factory factory =  FactorySPI.getFactory();
        return factory;
    }

}

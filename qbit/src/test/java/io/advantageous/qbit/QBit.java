package io.advantageous.qbit;

import io.advantageous.qbit.spi.FactorySPI;

/**
 * Created by Richard on 9/26/14.
 */
public class QBit {

    public static Factory factory() {
        return FactorySPI.getFactory();
    }

}

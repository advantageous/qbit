package io.advantageous.qbit.spi;

import io.advantageous.qbit.boon.BoonQBitFactory;
import io.advantageous.qbit.client.BoonClientFactory;
import io.advantageous.qbit.events.impl.BoonEventManager;
import io.advantageous.qbit.events.impl.BoonEventManagerFactory;

/**
 * Created by rhightower on 10/19/14.
 * @author rhightower
 * Helper class to marry Boon with QBit while keeping QBit seperate from Boon.
 */
public class RegisterBoonWithQBit {

    public static void registerBoonWithQBit() {
        FactorySPI.setFactory(new BoonQBitFactory());


        FactorySPI.setClientFactory(new BoonClientFactory());
        FactorySPI.setEventManagerFactory(new BoonEventManagerFactory());


    }
}

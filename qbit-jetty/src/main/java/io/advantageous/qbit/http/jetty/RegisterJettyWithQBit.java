package io.advantageous.qbit.http.jetty;

import io.advantageous.qbit.spi.FactorySPI;

/**
 * Created by rhightower on 2/13/15.
 */
public class RegisterJettyWithQBit {
    public static void registerJettyWithQBit() {
            FactorySPI.setHttpServerFactory(new JettyHttpServerFactory());
            FactorySPI.setHttpClientFactory(new JettyHttpClientFactory());
    }
}

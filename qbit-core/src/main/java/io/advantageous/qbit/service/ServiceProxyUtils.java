package io.advantageous.qbit.service;

import io.advantageous.qbit.client.ClientProxy;

/**
 * Created by rhightower on 2/4/15.
 */
public class ServiceProxyUtils {

    public static void flushServiceProxy(Object object) {
        if (object instanceof ClientProxy) {
            ((ClientProxy) object).clientProxyFlush();
        }
    }
}

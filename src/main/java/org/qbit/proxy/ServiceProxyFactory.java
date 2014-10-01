package org.qbit.proxy;

import org.qbit.service.ServiceBundle;

/**
 * Created by Richard on 10/1/14.
 */
public interface ServiceProxyFactory {
    <T> T createProxyWithReturnAddress(Class<T> serviceInterface,
                                       String serviceName,
                                       String returnAddressArg,
                                       ServiceBundle serviceBundle);

    <T> T createProxy(Class<T> serviceInterface,
                      String serviceName,
                      ServiceBundle serviceBundle);
}

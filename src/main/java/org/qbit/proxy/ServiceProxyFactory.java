package org.qbit.proxy;

import org.qbit.service.EndPoint;
import org.qbit.service.ServiceBundle;

/**
 * Created by Richard on 10/1/14.
 */
public interface ServiceProxyFactory {
    <T> T createProxyWithReturnAddress(Class<T> serviceInterface,
                                       String serviceName,
                                       String returnAddressArg,
                                       EndPoint serviceBundle);

    <T> T createProxy(Class<T> serviceInterface,
                      String serviceName,
                      EndPoint serviceBundle);
}

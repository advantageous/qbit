package io.advantageous.qbit.client;


import io.advantageous.qbit.service.EndPoint;

/**
 * Created by Richard on 10/1/14.
 *  @author Rick Hightower
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

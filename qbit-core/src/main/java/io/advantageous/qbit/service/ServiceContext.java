package io.advantageous.qbit.service;

import io.advantageous.qbit.service.impl.ServiceImpl;

/**
 * Created by rhightower on 2/4/15.
 */
public class ServiceContext {


    public static Service currentService() {
        return ServiceImpl.currentService();
    }
}

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.service.ServiceBuilder;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServiceHealthManagerDefaultTest {


    @Test
    public void testIsFailing() throws Exception {

        BaseServiceQueueImpl.serviceThreadLocal.set(ServiceBuilder.serviceBuilder().setServiceObject(new Object()).build());

        ServiceHealthManagerDefault serviceHealthManagerDefault = new ServiceHealthManagerDefault(null, null);

        serviceHealthManagerDefault.setFailing();
        assertTrue(serviceHealthManagerDefault.isFailing());
        serviceHealthManagerDefault.recover();
        assertFalse(serviceHealthManagerDefault.isFailing());


        serviceHealthManagerDefault = new ServiceHealthManagerDefault(null, null);
        BaseServiceQueueImpl.serviceThreadLocal.set(null);
        serviceHealthManagerDefault.setFailing();
        assertFalse(serviceHealthManagerDefault.isFailing());

    }

    @Test
    public void testIsOk() throws Exception {
        BaseServiceQueueImpl.serviceThreadLocal.set(ServiceBuilder.serviceBuilder().setServiceObject(new Object()).build());

        ServiceHealthManagerDefault serviceHealthManagerDefault = new ServiceHealthManagerDefault(null, null);

        serviceHealthManagerDefault.recover();
        assertTrue(serviceHealthManagerDefault.isOk());
        serviceHealthManagerDefault.setFailing();
        assertFalse(serviceHealthManagerDefault.isOk());


        serviceHealthManagerDefault = new ServiceHealthManagerDefault(null, null);
        BaseServiceQueueImpl.serviceThreadLocal.set(null);
        serviceHealthManagerDefault.recover();
        assertTrue(serviceHealthManagerDefault.isOk());

    }

}
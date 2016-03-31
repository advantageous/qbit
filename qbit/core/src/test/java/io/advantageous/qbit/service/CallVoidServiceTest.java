package io.advantageous.qbit.service;

import io.advantageous.qbit.queue.QueueBuilder;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class CallVoidServiceTest {

    @Test
    public void test() {
        final ServiceBuilder serviceBuilder = ServiceBuilder.serviceBuilder();

        final QueueBuilder responseQueueBuilder = serviceBuilder.getResponseQueueBuilder();
        final QueueBuilder requestQueueBuilder = serviceBuilder.getRequestQueueBuilder()
                .setEnqueueTimeout(1).setEnqueueTimeoutTimeUnit(TimeUnit.SECONDS);

        responseQueueBuilder.setBatchSize(5);
        responseQueueBuilder.setSize(5);
        requestQueueBuilder.setBatchSize(5);
        requestQueueBuilder.setSize(5);


        serviceBuilder.setServiceObject(new ServiceImpl());

        final ServiceQueue serviceQueue = serviceBuilder.buildAndStartAll();

        final ServiceI proxy = serviceQueue.createProxyWithAutoFlush(ServiceI.class,
                100, TimeUnit.MILLISECONDS);


        for (int index = 0; index < 1_000; index++) {
            proxy.callme();
        }
    }


    public interface ServiceI {
        void callme();
    }

    public class ServiceImpl {
        public void callme() {

            throw new RuntimeException();
        }
    }
}

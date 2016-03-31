package io.advantageous.qbit.reactive.async;

import io.advantageous.qbit.reactive.AsyncFutureCallback;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.service.ServiceBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;
import io.advantageous.qbit.service.ServiceQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AsyncFutureBuilderTest {


    private Foo foo;
    private ServiceQueue serviceQueue;

    @Before
    public void setUp() throws Exception {

        serviceQueue = ServiceBuilder.serviceBuilder().setServiceObject(new FooService()).buildAndStartAll();
        foo = serviceQueue.createProxy(Foo.class);
    }

    @After
    public void tearDown() throws Exception {
        serviceQueue.stop();
    }

    @Test
    public void test() {
        final AsyncFutureCallback<Boolean> callback = AsyncFutureBuilder
                .asyncFutureBuilder().setSupportLatch(true).build(Boolean.class);

        foo.getValue(callback);

        ServiceProxyUtils.flushServiceProxy(foo);

        final Boolean result = callback.get();

        assertTrue(result);
    }

    interface Foo {
        void getValue(Callback<Boolean> callback);
    }

    static class FooService implements Foo {

        @Override
        public void getValue(Callback<Boolean> callback) {
            callback.accept(true);
        }
    }


}
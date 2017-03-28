package io.advantageous.qbit.vertx.bugs;

import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.*;
import io.advantageous.qbit.util.PortUtils;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.Promises;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class Bug746 {


    @Test //Works
    public void testWithServiceQueue() {
        final ServiceQueue serviceQueue = ServiceBuilder.serviceBuilder()
                .setServiceObject(new FeedServiceImpl()).buildAndStartAll();
        final FeedService feedService = serviceQueue.createProxyWithAutoFlush(FeedService.class, Duration.ofMillis(100));

        final List<FeedMeta> feedMetas = feedService.listFeeds()
                .blockingGet(Duration.ofSeconds(30));

        assertNotNull(feedMetas);
        assertEquals(1, feedMetas.size());

        assertEquals("Hello", feedMetas.get(0).name);

        serviceQueue.stop();
    }

    @Test //Works
    public void testWithServiceBundle() {

        final ServiceBundleBuilder serviceBundleBuilder = ServiceBundleBuilder.serviceBundleBuilder();

        final ServiceBundle serviceBundle = serviceBundleBuilder.build().startServiceBundle();

        serviceBundle.addServiceObject("foo", new FeedServiceImpl());

        final FeedService feedService = serviceBundle.createLocalProxy(FeedService.class, "foo");

        final Promise<List<FeedMeta>> listPromise = feedService.listFeeds().asHandler()
                .invokeAsBlockingPromise(Duration.ofSeconds(30));

        ServiceProxyUtils.flushServiceProxy(feedService);


        final List<FeedMeta> feedMetas = listPromise.asHandler().get();


        assertNotNull(feedMetas);
        assertEquals(1, feedMetas.size());

        assertEquals("Hello", feedMetas.get(0).name);

        serviceBundle.stop();
    }

    @Test //Works
    public void testWithClient() {


        int port = PortUtils.findOpenPortStartAt(9999);
        final ServiceEndpointServer serviceEndpointServer =
                EndpointServerBuilder.endpointServerBuilder().setPort(port)
                        .addService("foo", new FeedServiceImpl()).build().startServerAndWait();


        final Client client = ClientBuilder.clientBuilder().setPort(port).build().startClient();

        final FeedService feedService = client.createProxy(FeedService.class, "foo");


        final List<FeedMeta> feedMetas = feedService.listFeeds().blockingGet(Duration.ofSeconds(30));


        assertNotNull(feedMetas);
        assertEquals(1, feedMetas.size());

        assertEquals("Hello", feedMetas.get(0).name);

        serviceEndpointServer.stop();
        client.stop();
    }

    public interface FeedService {
        Promise<List<FeedMeta>> listFeeds();

    }

    public static class FeedMeta {
        private final String name;

        public FeedMeta(String name) {
            this.name = name;
        }
    }

    public static class FeedServiceImpl implements FeedService {

        public Promise<List<FeedMeta>> listFeeds() {
            return Promises.invokablePromise(promise -> {
                promise.resolve(Collections.singletonList(new FeedMeta("Hello")));
            });
        }

    }

}

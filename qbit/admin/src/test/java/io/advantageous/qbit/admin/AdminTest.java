package io.advantageous.qbit.admin;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthStatus;
import io.advantageous.qbit.util.PortUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * created by rick on 6/3/15.
 */
public class AdminTest {

    ServiceEndpointServer serviceEndpointServer;
    AdminBuilder adminBuilder;
    HttpClient httpClient;

    @Before
    public void setUp() throws Exception {
        adminBuilder = AdminBuilder.adminBuilder();

        int port = adminBuilder.getPort();

        port = PortUtils.findOpenPortStartAt(port);

        adminBuilder.setPort(port);

        adminBuilder.getHealthService().register("foo", 1, TimeUnit.SECONDS);
        adminBuilder.getHealthService().register("bar", 1, TimeUnit.SECONDS);
        adminBuilder.getHealthService().register("baz", 1, TimeUnit.SECONDS);


        adminBuilder.getHealthService().checkInOk("foo");
        adminBuilder.getHealthService().checkInOk("bar");
        adminBuilder.getHealthService().checkInOk("baz");

        serviceEndpointServer = adminBuilder.build().startServer();

        httpClient = HttpClientBuilder.httpClientBuilder().setPort(adminBuilder.getPort()).build().startClient();
    }

    @After
    public void tearDown() throws Exception {
        serviceEndpointServer.stop();
        httpClient.stop();
        Sys.sleep(1000);
    }

    @Test
    public void testOk() throws Exception {

        HttpTextResponse httpResponse = httpClient.get("/__admin/ok");

        assertTrue(httpResponse.code() == 200);

        assertEquals("true", httpResponse.body());

    }

    @Test
    public void testFindAllNodes() throws Exception {

        HttpTextResponse httpResponse = httpClient.get("/__admin/all-nodes/");

        assertTrue(httpResponse.code() == 200);

        assertEquals("[\"bar\",\"foo\",\"baz\"]", httpResponse.body());
    }

    @Test
    public void testFindAllHealthyNodes() throws Exception {


        adminBuilder.getHealthService().checkIn("foo", HealthStatus.FAIL);

        adminBuilder.getHealthService().clientProxyFlush();
        Sys.sleep(1000);

        HttpTextResponse httpResponse = httpClient.get("/__admin/all-nodes/");

        assertTrue(httpResponse.code() == 200);

        assertEquals("[\"bar\",\"foo\",\"baz\"]", httpResponse.body());
    }
}
package io.advantageous.qbit.test;

import io.advantageous.qbit.http.HttpContentTypes;
import io.advantageous.qbit.http.request.HttpRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class MockHttpClientTest {

    MockHttpClient mockHttpClient;

    @Before
    public void setUp() throws Exception {

        mockHttpClient = new MockHttpClient();

    }

    @After
    public void tearDown() throws Exception {


    }

    @Test
    public void post2FormParams() throws Exception {

        mockHttpClient.postFormAsyncWith2Params("/test", "foo0", "bar0",
                "foo1", "bar1", (code, contentType, body) -> {

                });

        final HttpRequest request = mockHttpClient.getRequests().get(0);

        assertTrue(request.getContentType().equals(HttpContentTypes.FORM));


        assertEquals("foo0=bar0&foo1=bar1", request.getBodyAsString());

    }


    @Test
    public void post3FormParams() throws Exception {

        mockHttpClient.postFormAsyncWith3Params("/test", "foo0", "bar0",
                "foo1", "bar1", "foo2", "bar2", (code, contentType, body) -> {

                });

        final HttpRequest request = mockHttpClient.getRequests().get(0);

        assertTrue(request.getContentType().equals(HttpContentTypes.FORM));


        assertEquals("foo0=bar0&foo1=bar1&foo2=bar2", request.getBodyAsString());

    }
}
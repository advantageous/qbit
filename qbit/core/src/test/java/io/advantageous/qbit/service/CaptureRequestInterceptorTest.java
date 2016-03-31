package io.advantageous.qbit.service;

import io.advantageous.qbit.http.HttpContext;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.message.MethodCallBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class CaptureRequestInterceptorTest {

    private CaptureRequestInterceptor captureRequestInterceptor;
    private RequestContext requestContext;
    private HttpContext httpContext;

    @Before
    public void setup() throws Exception {
        captureRequestInterceptor = new CaptureRequestInterceptor();
        requestContext = new RequestContext();
        httpContext = new HttpContext();
    }

    @Test
    public void testNoOriginatingRequest() throws Exception {
        MethodCallBuilder methodCallBuilder = new MethodCallBuilder();
        methodCallBuilder.setName("Method 1");
        captureRequestInterceptor.before(methodCallBuilder.build());

        assertEquals("Method 1", requestContext.getMethodCall().get().name());

        captureRequestInterceptor.after(methodCallBuilder.build(), null);


        assertFalse(requestContext.getMethodCall().isPresent());


    }

    @Test
    public void testOriginatingMethod() throws Exception {

        HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setUri("/foo");
        MethodCallBuilder methodCallBuilder = MethodCallBuilder.methodCallBuilder();
        methodCallBuilder.setName("Method 1");
        methodCallBuilder.setOriginatingRequest(httpRequestBuilder.build());
        methodCallBuilder.setOriginatingRequest(methodCallBuilder.build());
        methodCallBuilder.setName("Method 2");


        captureRequestInterceptor.before(methodCallBuilder.build());

        assertEquals("Method 2", requestContext.getMethodCall().get().name());
        assertTrue(httpContext.getHttpRequest().isPresent());

        captureRequestInterceptor.after(methodCallBuilder.build(), null);


        assertFalse(requestContext.getMethodCall().isPresent());


    }


}
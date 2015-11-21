package io.advantageous.qbit.logging;

import io.advantageous.boon.core.Sets;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class SetupMdcForHttpRequestInterceptorTest {

    private SetupMdcForHttpRequestInterceptor mdcForHttpRequestInterceptor;

    private MethodCallBuilder methodCallBuilder;


    private HttpRequest httpRequest;


    //TODO #552

    @Before
    public void setup() throws Exception {
        mdcForHttpRequestInterceptor = new SetupMdcForHttpRequestInterceptor(Sets.set("foo"));
        methodCallBuilder = MethodCallBuilder.methodCallBuilder();
        httpRequest = HttpRequestBuilder.httpRequestBuilder().setUri("/foo").setRemoteAddress("localhost")
                .setMethodGet()
                .addHeader("foo", "bar").build();
    }

    @Test
    public void test() throws Exception{

        mdcForHttpRequestInterceptor.before(methodCallBuilder
                .setOriginatingRequest(httpRequest).setName("m1").build());

        final Map<String, String> mdc = MDC.getCopyOfContextMap();

        validate(mdc);


        mdcForHttpRequestInterceptor.after(null, null);


        final Map<String, String> mdc2 = MDC.getCopyOfContextMap();

        assertNull(mdc2);

    }


    @Test
    public void testNoRequest() throws Exception{

        mdcForHttpRequestInterceptor.before(methodCallBuilder
                .setName("m1").build());
        final Map<String, String> mdc = MDC.getCopyOfContextMap();
        assertNull(mdc);
        mdcForHttpRequestInterceptor.after(null, null);
        final Map<String, String> mdc2 = MDC.getCopyOfContextMap();
        assertNull(mdc2);

    }

    @Test
    public void testNoHeaders() throws Exception{

        mdcForHttpRequestInterceptor = new SetupMdcForHttpRequestInterceptor(Collections.emptySet());

        mdcForHttpRequestInterceptor.before(methodCallBuilder
                .setOriginatingRequest(httpRequest).setName("m1").build());

        final Map<String, String> mdc = MDC.getCopyOfContextMap();

        validate(mdc, false);


        mdcForHttpRequestInterceptor.after(null, null);


        final Map<String, String> mdc2 = MDC.getCopyOfContextMap();

        assertNull(mdc2);

    }


    private void validate(Map<String, String> mdc) {
        validate(mdc, true);
    }

    private void validate(Map<String, String> mdc, boolean header) {
        final String httpMethod = mdc.get(SetupMdcForHttpRequestInterceptor.REQUEST_HTTP_METHOD);
        final String uri = mdc.get(SetupMdcForHttpRequestInterceptor.REQUEST_URI);
        final String remoteAddress = mdc.get(SetupMdcForHttpRequestInterceptor.REQUEST_REMOTE_ADDRESS);
        final String headerFoo = mdc.get(SetupMdcForHttpRequestInterceptor.REQUEST_HEADER_PREFIX + "foo");


        assertEquals("/foo", uri);
        assertEquals("GET", httpMethod);
        assertEquals("localhost", remoteAddress);

        if (header) {
            assertEquals("bar", headerFoo);
        } else {
            assertNull(headerFoo);
        }

    }

    @Test
    public void testNested() throws Exception{

        final MethodCall<Object> m1 = methodCallBuilder
                .setOriginatingRequest(httpRequest).setName("m1").build();


        final MethodCall<Object> m2 = methodCallBuilder
                .setOriginatingRequest(m1).setName("m2").build();

        mdcForHttpRequestInterceptor.before(m2);

        final Map<String, String> mdc = MDC.getCopyOfContextMap();

        validate(mdc);


        mdcForHttpRequestInterceptor.after(null, null);


        final Map<String, String> mdc2 = MDC.getCopyOfContextMap();

        assertNull(mdc2);

    }








}
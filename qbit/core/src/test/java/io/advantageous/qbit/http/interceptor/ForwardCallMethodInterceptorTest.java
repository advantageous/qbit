package io.advantageous.qbit.http.interceptor;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.service.CaptureRequestInterceptor;
import io.advantageous.qbit.service.RequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ForwardCallMethodInterceptorTest {
    private ForwardCallMethodInterceptor forwardCallMethodInterceptor;

    private RequestContext requestContext;
    private CaptureRequestInterceptor captureRequestInterceptor;
    private MethodCallBuilder methodCallBuilder;


    @Before
    public void setup() {
        captureRequestInterceptor = new CaptureRequestInterceptor();
        requestContext = new RequestContext();
        forwardCallMethodInterceptor = new ForwardCallMethodInterceptor(requestContext);


        HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();
        httpRequestBuilder.setUri("/foo");
        MethodCallBuilder methodCallBuilder = MethodCallBuilder.methodCallBuilder();
        methodCallBuilder.setName("Method 1");
        methodCallBuilder.setOriginatingRequest(httpRequestBuilder.build());
        methodCallBuilder.setOriginatingRequest(methodCallBuilder.build());
        methodCallBuilder.setName("Method 2");

        captureRequestInterceptor.before(methodCallBuilder.build());


        this.methodCallBuilder = MethodCallBuilder.methodCallBuilder();
    }

    @Test
    public void test() {
        forwardCallMethodInterceptor.beforeMethodSent(this.methodCallBuilder);

        assertNotNull(methodCallBuilder.getOriginatingRequest());

        final Request<Object> originatingRequest = methodCallBuilder.getOriginatingRequest();

        assertTrue(originatingRequest instanceof MethodCall);

        assertEquals("Method 2", ((MethodCall) originatingRequest).name());


        assertTrue(originatingRequest.originatingRequest() instanceof MethodCall);


        assertEquals("Method 1", ((MethodCall) originatingRequest.originatingRequest()).name());


        assertTrue(originatingRequest.originatingRequest().originatingRequest() instanceof HttpRequest);
        assertEquals("/foo", ((HttpRequest) originatingRequest.originatingRequest().originatingRequest()).getUri());

    }

    @After
    public void tearDown() {
        captureRequestInterceptor.after(null, null);
    }


}
package io.advantageous.qbit.vertx.bugs;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.util.PortUtils;
import org.junit.Test;

public class Bug724 {

    @Test
    public void test() {
        final int port = PortUtils.findOpenPortStartAt(8080);

        final ServiceEndpointServer serviceEndpointServer =
                EndpointServerBuilder.endpointServerBuilder().setUri("/")
                        .addService(new MyService()).setPort(port).build();

        serviceEndpointServer.startServerAndWait();

        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder().setPort(port).buildAndStart();

        final HttpRequest httpRequest = HttpRequestBuilder.httpRequestBuilder().setUri("/reverse")
                .addParam("foo", "bar").addParam("reverseParam", "Rick Loves Java")
                .setFormPostAndCreateFormBody().build();

        final HttpTextResponse httpTextResponse = httpClient.sendRequestAndWait(httpRequest);

        //assertEquals("was=Rick Loves Java", httpTextResponse.body());
    }

    @RequestMapping("/")
    public static class MyService {
        @RequestMapping(value = "/reverse", method = RequestMethod.POST, code = 200)
        public void doPostReverse(final Callback<String> callback, final @RequestParam("reverseParam") String reverseParam) {
            String retval = reverseParam != null ? new StringBuilder(reverseParam).reverse().toString() : "reverseParam was NULL";
            System.out.println("In: " + reverseParam + ", Out: " + retval);
            callback.resolve("was=" + reverseParam);
        }
    }

}

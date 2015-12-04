package io.advantageous.qbit.example.nocache;

import static io.advantageous.qbit.admin.ManagedServiceBuilder.managedServiceBuilder;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.http.NoCacheHeaders;
import io.advantageous.qbit.annotation.http.ResponseHeader;
import static io.advantageous.qbit.http.request.HttpResponseBuilder.httpResponseBuilder;

import io.advantageous.qbit.http.request.HttpBinaryResponse;
import io.advantageous.qbit.http.request.HttpResponseBuilder;
import io.advantageous.qbit.http.request.HttpResponseDecorator;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.util.MultiMap;

@RequestMapping("/") @NoCacheHeaders
public class NoCacheService {

    @RequestMapping @ResponseHeader(name = "X-Attempt-Id", value="UUID")
    public String hello() {
        return "hello";
    }



//    public static void main(final String... args) {
//        managedServiceBuilder()
//                .setRootURI("/").addEndpointService(new NoCacheService()).startApplication();
//    }


    public static void main(final String... args) {
        final ManagedServiceBuilder managedServiceBuilder = managedServiceBuilder();

        managedServiceBuilder.getHttpServerBuilder().addResponseDecorator(new HttpResponseDecorator() {
            @Override
            public boolean decorateTextResponse(HttpTextResponse[] responseHolder, String requestPath,
                                                int code, String contentType, String payload,
                                                MultiMap<String, String> responseHeaders,
                                                MultiMap<String, String> requestHeaders,
                                                MultiMap<String, String> requestParams) {


                final HttpResponseBuilder responseBuilder = HttpResponseBuilder.httpResponseBuilder()
                        .setCode(code)
                        .setContentType(contentType)
                        .setBody(payload);
                if (responseHeaders != null && !responseHeaders.isEmpty()) {
                    responseBuilder.setHeaders(responseHeaders);
                }

                responseBuilder
                        .addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                        .addHeader("Pragma", "no-cache")
                        .addHeader("Expires", "0")
                        .addHeader("X-MyHeader-Hostname", "foo");

                responseHolder[0] = (HttpTextResponse)responseBuilder
                        .build();


                return true;
            }

            @Override
            public boolean decorateBinaryResponse(HttpBinaryResponse[] responseHolder, String requestPath,
                                                  int code, String contentType, byte[] payload,
                                                  MultiMap<String, String> responseHeaders,
                                                  MultiMap<String, String> requestHeaders,
                                                  MultiMap<String, String> requestParams) {


                final HttpResponseBuilder responseBuilder = HttpResponseBuilder.httpResponseBuilder()
                        .setCode(code)
                        .setContentType(contentType)
                        .setBody(payload);

                if (responseHeaders != null && !responseHeaders.isEmpty()) {
                    responseBuilder.setHeaders(responseHeaders);
                }

                responseBuilder
                        .addHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                        .addHeader("Pragma", "no-cache")
                        .addHeader("Expires", "0")
                        .addHeader("X-Calypso-Hostname", "foo");

                responseHolder[0] = (HttpBinaryResponse)responseBuilder
                        .build();


                return true;
            }
        });

        managedServiceBuilder.setRootURI("/").addEndpointService(new NoCacheService())
                .startApplication();
    }


}

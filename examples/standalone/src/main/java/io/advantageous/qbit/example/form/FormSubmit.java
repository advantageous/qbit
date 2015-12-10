package io.advantageous.qbit.example.form;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.http.NoCacheHeaders;
import io.advantageous.qbit.http.HttpContext;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpResponseBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.reactive.Callback;


import java.util.Optional;


/**
 * The form data is like Servlets.
 *
 * curl -X "POST" "http://localhost:8080/v1/submit?urlparam=12" \
 * -H "Content-Type: application/x-www-form-urlencoded" \
 * --data-urlencode "data=body data"
 */
@RequestMapping("/") @NoCacheHeaders
public class FormSubmit {


    @RequestMapping
    public String hello() {
        return "hello";
    }

    @RequestMapping(method = RequestMethod.POST)
    public void submit(final Callback<HttpResponse<String>> callback) {
        final HttpContext httpContext = new HttpContext();
        final Optional<HttpRequest> httpRequest = httpContext.getHttpRequest();


        final StringBuilder sb = new StringBuilder(128);

        if (httpRequest.isPresent()) {
            sb.append("Content Type :" ).append(httpRequest.get().getContentType()).append("\n");
            sb.append(" Method :").append(httpRequest.get().getMethod()).append("\n");
            sb.append(" Form Body Length ").append(httpRequest.get().getBody().length).append("\n");
            sb.append(" Params ").append(httpRequest.get().getParams().toString()).append("\n");

            HttpTextResponse textResponse = HttpResponseBuilder.httpResponseBuilder()

                    .setJsonBodyCodeOk(sb.toString())
                    .buildTextResponse();

            callback.accept(textResponse);
        }
    }

    public static void main(final String... args) {
        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();
        managedServiceBuilder.enableRequestChain();
        managedServiceBuilder.addEndpointService(new FormSubmit())
                .setRootURI("/v1").startApplication();

    }
}


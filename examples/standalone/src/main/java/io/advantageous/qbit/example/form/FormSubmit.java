package io.advantageous.qbit.example.form;

import io.advantageous.qbit.admin.ManagedServiceBuilder;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.http.NoCacheHeaders;
import io.advantageous.qbit.http.HttpContext;
import io.advantageous.qbit.http.config.HttpServerConfig;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpResponse;
import io.advantageous.qbit.http.request.HttpResponseBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.vertx.http.VertxHttpServerBuilder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

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
            sb.append(" Form Params ").append(httpRequest.get().getFormParams().toString()).append("\n");

            HttpTextResponse textResponse = HttpResponseBuilder.httpResponseBuilder()

                    .setJsonBodyCodeOk(sb.toString())
                    .buildTextResponse();

            callback.accept(textResponse);
        }
    }

    public static void mainNoVertx(final String... args) {
        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();

        managedServiceBuilder.getHttpServerBuilder().addShouldContinueHttpRequestPredicate(request -> {
            System.out.println("BODY " + request.getBodyAsString());

            System.out.println("FORM PARAMS " + request.getFormParams());
            return true;
        });

        managedServiceBuilder.enableRequestChain();
        managedServiceBuilder.addEndpointService(new FormSubmit())
                .setRootURI("/v1").startApplication();

    }


    public static class MyVerticle extends AbstractVerticle {


        private ManagedServiceBuilder managedServiceBuilder;
        public MyVerticle(ManagedServiceBuilder managedServiceBuilder) {
            this.managedServiceBuilder = managedServiceBuilder;
        }
        @Override
        public void start() throws Exception {

            managedServiceBuilder.setRootURI("/v1");
            managedServiceBuilder.enableRequestChain();

            final Vertx vertx = getVertx();
			/* Vertx HTTP Server. */
            final io.vertx.core.http.HttpServer vertxHttpServer =
                    vertx.createHttpServer();

			/* Route one call to a vertx handler. */
            final Router router = Router.router(vertx); //Vertx router

			/* Route everything under /v1 to QBit http server. */
            final Route qbitRoute = router.route().path("/v1/*");

			/*
			 * Use the VertxHttpServerBuilder which is a special builder for Vertx/Qbit integration.
			 */
            VertxHttpServerBuilder vertxHttpServerBuilder =  VertxHttpServerBuilder.vertxHttpServerBuilder();


            final HttpServer httpServer=  vertxHttpServerBuilder
                    .setRoute(qbitRoute)
                    .setHttpServer(vertxHttpServer)
                    .setVertx(vertx)
                    .setConfig(new HttpServerConfig())
                    .build();

            vertxHttpServerBuilder.addShouldContinueHttpRequestPredicate(request -> {
                System.out.println("BODY " + request.getBodyAsString());

                System.out.println("FORM PARAMS " + request.getFormParams());
                return true;
            } );
            managedServiceBuilder.addEndpointService(new FormSubmit());


			/*
			 * Create and start new service endpointServer.
			 */
            managedServiceBuilder.getEndpointServerBuilder()
                    .setHttpServer(httpServer)
                    .build()
                    .startServer();

			/*
			 * Associate the router as a request handler for the vertxHttpServer.
			 */
            vertxHttpServer.requestHandler(router::accept).listen(
                    managedServiceBuilder.getPort());

        }
    }


    public static void main(String[] args) {

        final ManagedServiceBuilder managedServiceBuilder = ManagedServiceBuilder.managedServiceBuilder();
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MyVerticle(managedServiceBuilder), result -> {
            if (result.succeeded()) {
                System.out.println("Deployment id is:  {} on port : {}  " + result.result() + " " + managedServiceBuilder.getPort());
            } else {
                System.out.println("Deployment failed!" + result.cause());
            }
        });
    }

}


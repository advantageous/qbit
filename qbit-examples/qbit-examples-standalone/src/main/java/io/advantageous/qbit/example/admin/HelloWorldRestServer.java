package io.advantageous.qbit.example.admin;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.server.ServiceServer;
import org.boon.Boon;
import org.boon.core.Sys;

import static io.advantageous.qbit.http.HttpServerBuilder.httpServerBuilder;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;
import static org.boon.Boon.resource;

/**
 * Created by rhightower on 2/9/15.
 */
public class HelloWorldRestServer {


    public static final String HTML_HELLO_PAGE = "/ui/helloWorld.html";


    public static void startHelloWorld(int port) {

        HttpServer httpServer = httpServerBuilder().setPort(port).build();

        httpServer.setShouldContinueHttpRequest(httpRequest -> {
            /* If not the page we want to return then just continue. */
            if ( ! httpRequest.getUri().equals(HTML_HELLO_PAGE) ) {
                return true;
            }
            final String helloWorldWebPage = resource(HTML_HELLO_PAGE);
            httpRequest.getResponse().response(200, "text/html", helloWorldWebPage);
            return false;
        });

        httpServer.setHttpRequestConsumer(httpRequest ->
                httpRequest.getResponse().response(404, "text/html", "<h>NOT FOUND</h>"));


        final ServiceServer serviceServer = serviceServerBuilder()
                .setHttpServer(httpServer).build();

        serviceServer.initServices(new HelloService());
        serviceServer.startServer();

    }

    public static void main(String... args) {
        startHelloWorld(9999);
        while(true) Sys.sleep(1000);
    }

}

package io.advantageous.qbit.example.admin;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.server.ServiceServer;
import org.boon.Boon;
import org.boon.core.Sys;

import java.util.function.Consumer;

import static io.advantageous.qbit.http.HttpServerBuilder.httpServerBuilder;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;

/**
 * Created by rhightower on 2/9/15.
 */
public class HelloWorldRestServer {


    public static final String HTML_HELLO_PAGE = "/ui/helloWorld.html";

    public static class HelloObject {
        final String hello;
        final long time = System.currentTimeMillis();

        public HelloObject(String hello) {
            this.hello = hello;
        }
    }


    @RequestMapping("/helloservice")
    public static class HelloService {


        @RequestMapping("/hello")
        public HelloObject hello() {
            return new HelloObject("Hi Fadi");
        }

    }

    public static void startAdminUI(int port) {

        HttpServer httpServer = httpServerBuilder().setPort(port).build();

        httpServer.setShouldContinueHttpRequest(httpRequest -> {

            /* If not the page we want to return then just continue. */
            if ( ! httpRequest.getUri().equals(HTML_HELLO_PAGE) ) {
                return true;
            }

            final String helloWorldWebPage = Boon.resource(HTML_HELLO_PAGE);

            httpRequest.getResponse().response(200, "text/html", helloWorldWebPage);

            return false;
        });

        httpServer.setHttpRequestConsumer(new Consumer<HttpRequest>() {
            @Override
            public void accept(HttpRequest httpRequest) {

                httpRequest.getResponse().response(404, "text/html", "<h>NOT FOUND</h>");
            }
        });

        final ServiceServer serviceServer = serviceServerBuilder().setHttpServer(httpServer).build();

        serviceServer.initServices(new HelloService());
        serviceServer.startServer();
    }

    public static void main(String... args) {
        startAdminUI(9999);
        while(true) Sys.sleep(1000);
    }

}

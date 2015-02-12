package io.advantageous.qbit.example.hello;

import io.advantageous.qbit.http.HttpServer;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.system.QBitSystemManager;

import static io.advantageous.qbit.http.HttpServerBuilder.httpServerBuilder;
import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;
import static org.boon.Boon.resource;

/**
 * Created by rhightower on 2/9/15.
 */
public class HelloWorldRestServer {


    public static final String HTML_HELLO_PAGE = "/ui/helloWorld.html";


    public static void main(String... args) {

        /* Create the system manager to manage the shutdown. */
        QBitSystemManager systemManager = new QBitSystemManager();

        HttpServer httpServer = httpServerBuilder()
                .setPort(9999).build();

        /* Register the Predicate using a Java 8 lambda expression. */
        httpServer.setShouldContinueHttpRequest(httpRequest -> {
            /* If not the page uri we want to then just continue by returning true. */
            if (!httpRequest.getUri().equals(HTML_HELLO_PAGE)) {
                return true;
            }
            /* read the page from the file system or classpath. */
            final String helloWorldWebPage = resource(HTML_HELLO_PAGE);
            /* Send the HTML file out to the browser. */
            httpRequest.getResponse().response(200, "text/html", helloWorldWebPage);
            return false;
        });


        /* Start the service. */
        final ServiceServer serviceServer = serviceServerBuilder().setSystemManager(systemManager)
                .setHttpServer(httpServer).build().initServices(new HelloService()).startServer();

        /* Wait for the service to shutdown. */
        systemManager.waitForShutdown();

    }


}

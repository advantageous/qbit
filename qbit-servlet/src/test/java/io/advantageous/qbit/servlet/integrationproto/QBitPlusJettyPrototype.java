package io.advantageous.qbit.servlet.integrationproto;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.servlet.QBitHttpServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import javax.servlet.ServletConfig;

/**
 * Created by rhightower on 2/12/15.
 */
public class QBitPlusJettyPrototype {

    public static class MyQBitServlet extends QBitHttpServlet {
        public static final String QBIT_SERVICE_SERVER_SERVER = "QBit.ServiceServer.server";
        private ServletConfig config;

        @Override
        protected void wireHttpServer(final HttpServer httpServer, final ServletConfig config) {
            final ServiceServer server = MyServiceModule.configureApp(httpServer);
            config.getServletContext().setAttribute(QBIT_SERVICE_SERVER_SERVER, server);
            this.config = config;
        }

        @Override
        protected void stop() {
            final ServiceServer server =
                    (ServiceServer)
                            config.getServletContext().getAttribute(QBIT_SERVICE_SERVER_SERVER);
            server.stop();
        }
    }

    public static void main(String... args) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler servletContextHandler = new ServletContextHandler(server,
                                                                       "*", true, false);
        servletContextHandler.addServlet(MyQBitServlet.class, "/services/*");
        server.start();
        server.join();
    }
}

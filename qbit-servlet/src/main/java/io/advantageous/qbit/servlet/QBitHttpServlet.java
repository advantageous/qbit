package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.advantageous.qbit.servlet.QBitServletUtil.convertRequest;

/**
 * Created by rhightower on 2/12/15.
 */
@WebServlet(asyncSupported = true)
public abstract class QBitHttpServlet extends HttpServlet {

    private final ServletHttpServer httpServer = new ServletHttpServer();

    @Override
    public void destroy() {
        httpServer.stop();
        stop();
    }

    protected abstract void stop();

    @Override
    public void init(ServletConfig config) throws ServletException {
        httpServer.start();
        wireHttpServer(httpServer, config);
    }

    protected abstract void wireHttpServer(final HttpServer httpServer, ServletConfig config);

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
                                                             throws ServletException, IOException {
        final HttpRequest httpRequest = convertRequest(request.startAsync());
        httpServer.handleRequest(httpRequest);
    }
}

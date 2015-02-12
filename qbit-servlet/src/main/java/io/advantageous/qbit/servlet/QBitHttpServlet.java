package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpServer;

import javax.servlet.AsyncContext;
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
@WebServlet(asyncSupported = true, value = "/AsyncServlet")
public abstract class QBitHttpServlet extends HttpServlet {

    private final ServletHttpServer httpServer = new ServletHttpServer();

    @Override
    public void destroy() {
        httpServer.stop();
    }

    @Override
    public void init() throws ServletException {
        httpServer.start();
        wireHttpServer(httpServer);
    }

    protected abstract void wireHttpServer(final HttpServer httpServer);

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        handleRequest(request.startAsync());
    }

    protected void handleRequest(final AsyncContext asyncContext) {
        final HttpRequest httpRequest = convertRequest(asyncContext);
        httpServer.handleRequest(httpRequest);
    }




}

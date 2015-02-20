/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpTransport;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.server.HttpServer;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.advantageous.qbit.servlet.QBitServletUtil.convertRequest;

/**
 * @author rhightower on 2/12/15.
 */
@WebServlet(asyncSupported = true)
public abstract class QBitHttpServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(QBitHttpServlet.class);

    private final SimpleHttpServer httpServer = new SimpleHttpServer();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            wireHttpServer(httpServer, config);
        } catch (Exception ex) {
            logger.error("Unable to start QBitHttpServlet servlet", ex);
        }
    }

    @Override
    public void destroy() {
        httpServer.stop();
        stop();
    }

    protected abstract void stop();

    protected abstract void wireHttpServer(final HttpTransport httpTransport,
                                           final ServletConfig config);

    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final HttpRequest httpRequest = convertRequest(request.startAsync());
        httpServer.handleRequest(httpRequest);
    }
}

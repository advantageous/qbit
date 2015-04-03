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

package io.advantageous.qbit.http.jetty.impl.server;

import io.advantageous.boon.core.IO;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.config.HttpServerOptions;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.server.impl.SimpleHttpServer;
import io.advantageous.qbit.servlet.HttpServletHeaderMultiMap;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.boon.core.reflection.BeanUtils;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.websocket.api.WebSocketBehavior;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;
import static io.advantageous.qbit.servlet.QBitServletUtil.setRequestBodyIfNeeded;

/**
 * @author rhightower on 2/13/15.
 */
public class JettyQBitHttpServer extends SimpleHttpServer {

    private final Logger logger = LoggerFactory.getLogger(SimpleHttpServer.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final Server server;
    private final WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);

    private final WebSocketServletFactory webSocketServletFactory;
    private final HttpServerOptions options;

    public JettyQBitHttpServer(HttpServerOptions options, QBitSystemManager systemManager) {

        super(systemManager, options.getFlushInterval());

        policy.setMaxBinaryMessageSize(options.getMaxWebSocketFrameSize());
        policy.setMaxTextMessageSize(options.getMaxWebSocketFrameSize());
        policy.setMaxTextMessageBufferSize(options.getMaxWebSocketFrameSize());
        policy.setMaxBinaryMessageBufferSize(options.getMaxWebSocketFrameSize());
        policy.setAsyncWriteTimeout(10_000);

        this.options = BeanUtils.copy(options);
        if (debug) {
            puts(options);
        }
        this.server = new Server();
        configureServer();
        webSocketServletFactory = webSocketServletFactory();
    }

    private void configureServer() {
        configureThreadPool(options);
        configureConnector(options);
        configureHandler();
    }

    private void configureHandler() {
        server.setHandler(new AbstractHandler() {
            @Override
            public void handle(final String target,
                               final Request baseRequest,
                               final HttpServletRequest request,
                               final HttpServletResponse response)
                    throws IOException, ServletException {

                if (webSocketServletFactory.isUpgradeRequest(request, response)) {
                    /* We have an upgrade request. */
                    if (webSocketServletFactory.acceptWebSocket(request, response)) {

                        baseRequest.setHandled(true);
                        /* websocket created */
                        return;
                    }
                    if (response.isCommitted()) {
                        return;
                    }
                } else {
                    baseRequest.setAsyncSupported(true);

                    handleRequestInternal(request, response);
                }
            }
        });
    }
    private void handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) {

        final AsyncContext asyncContext = request.startAsync(request, response);
        final HttpRequestBuilder httpRequestBuilder = convertRequest(asyncContext);
        setupRequestHandler(response, httpRequestBuilder);
        handleRequest(httpRequestBuilder.build());

    }


    public static HttpRequestBuilder convertRequest(final AsyncContext asyncContext) {

        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        final MultiMap<String, String> headers = new HttpServletHeaderMultiMap(request);
        final MultiMap<String, String> params = new MultiMapImpl<>(request.getParameterMap());
        final HttpRequestBuilder httpRequestBuilder = httpRequestBuilder().setParams(params)
                .setHeaders(headers).setUri(request.getRequestURI())
                .setMethod(request.getMethod());
        setRequestBodyIfNeeded(request, httpRequestBuilder);
        return httpRequestBuilder;
    }




    private static void setupRequestHandler(final HttpServletResponse response,
                                            final HttpRequestBuilder httpRequestBuilder) {

        httpRequestBuilder.setTextReceiver((code, contentType, body) -> {


            response.setHeader("Content-Type", contentType);
            response.setStatus(code);
            final byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            response.setHeader("Content-Length", String.valueOf(bodyBytes.length));

            try {
                final ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(bodyBytes);
                outputStream.close();
                //baseRequest.setHandled(true);
                //asyncContext.dispatch();
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }


    private void configureConnector(HttpServerOptions options) {
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(options.getPort());

        connector.setAcceptQueueSize(options.getAcceptBackLog());
        connector.setReuseAddress(options.isReuseAddress());
        connector.setSoLingerTime(options.getSoLinger());
        connector.setIdleTimeout(options.getIdleTimeout());


        if (options.getHost() != null) {
            connector.setHost(options.getHost());
        }
        server.addConnector(connector);
    }

    private void configureThreadPool(HttpServerOptions options) {
        final ThreadPool threadPool = this.server.getThreadPool();

        if (threadPool instanceof QueuedThreadPool) {
            if (options.getWorkers() > 4) {
                ((QueuedThreadPool) threadPool).setMaxThreads(options.getWorkers());
                ((QueuedThreadPool) threadPool).setMinThreads(4);
            }
        }
    }


    private WebSocketServletFactory webSocketServletFactory() {

        try {
            WebSocketServletFactory webSocketServletFactory = WebSocketServletFactory.Loader.create(policy);
            webSocketServletFactory.init();
            webSocketServletFactory.setCreator((request, response) -> new JettyNativeWebSocketHandler(request, JettyQBitHttpServer.this));
            return webSocketServletFactory;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }


    @Override
    public void start() {
        super.start();
        try {
            server.start();
        } catch (Exception ex) {
            logger.error("Unable to start up Jetty", ex);
        }
    }


    public void stop() {
        super.stop();
        try {
            server.stop();
        } catch (Exception ex) {
            logger.error("Unable to shut down Jetty", ex);
        }
    }


}

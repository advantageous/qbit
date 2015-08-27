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

import io.advantageous.boon.core.IO;
import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.util.MultiMap;
import io.advantageous.qbit.util.MultiMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;

/**
 * @author rhightower on 2/12/15.
 */
public class QBitServletUtil {


    private final Logger logger = LoggerFactory.getLogger(QBitServletUtil.class);
    private final boolean debug = GlobalConstants.DEBUG || logger.isDebugEnabled();
    private final boolean trace = logger.isTraceEnabled();


    private QBitServletUtil() {

    }

    public static HttpRequest convertRequest(final AsyncContext asyncContext, final Consumer<Exception> onError) {

        return new QBitServletUtil().doConvertRequest(asyncContext, onError);

    }

    public static HttpRequest convertRequest(final AsyncContext asyncContext) {

        return new QBitServletUtil().doConvertRequest(asyncContext, null);

    }

    public static void setRequestBodyIfNeeded(final HttpServletRequest request,
                                              final HttpRequestBuilder httpRequestBuilder) {

        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {
            final String body = readBody(request);
            if (body != null) {
                httpRequestBuilder.setBody(body);
            }
        }
    }

    public static String readBody(final HttpServletRequest request) {
        try {
            final ServletInputStream inputStream = request.getInputStream();
            final String read = IO.read(inputStream, StandardCharsets.UTF_8);
            inputStream.close();
            return read;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private HttpRequest doConvertRequest(final AsyncContext asyncContext, Consumer<Exception> onError) {

        if (onError == null) {
            onError = e -> {

            };
        }

        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        final MultiMap<String, String> headers = new HttpServletHeaderMultiMap(request);
        final MultiMap<String, String> params = new MultiMapImpl<>(request.getParameterMap());


        final HttpRequestBuilder httpRequestBuilder = httpRequestBuilder().setParams(params)
                .setHeaders(headers).setUri(request.getRequestURI())
                .setMethod(request.getMethod());

        setRequestBodyIfNeeded(request, httpRequestBuilder);
        setupRequestHandler(asyncContext, response, httpRequestBuilder, onError);
        return httpRequestBuilder.build();

    }

    private void setupRequestHandler(final AsyncContext asyncContext,
                                     final HttpServletResponse response,
                                     final HttpRequestBuilder httpRequestBuilder,
                                     final Consumer<Exception> onError) {

        httpRequestBuilder.setTextReceiver((code, contentType, body) -> {
                    try {
                        if (!response.isCommitted()) {

                            response.setHeader("Content-Type", contentType);
                            response.setStatus(code);
                            final byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

                            response.setHeader("Content-Length", String.valueOf(bodyBytes.length));

                            final ServletOutputStream outputStream = response.getOutputStream();
                            outputStream.write(bodyBytes);
                            outputStream.close();
                            asyncContext.complete();
                        }
                    } catch (Exception ex) {
                        onError.accept(ex);
                        if (debug) logger.debug("unable to write", ex);
                    }
                }
        );
    }
}

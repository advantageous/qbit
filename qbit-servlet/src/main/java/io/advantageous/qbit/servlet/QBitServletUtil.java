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

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.util.MultiMap;
import org.boon.IO;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.advantageous.qbit.http.request.HttpRequestBuilder.httpRequestBuilder;

/**
 * @author rhightower on 2/12/15.
 */
public class QBitServletUtil {

    public static HttpRequest convertRequest(final AsyncContext asyncContext) {

        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        final MultiMap<String, String> headers = new HttpServletHeaderMultiMap(request);
        final MultiMap<String, String> params = new HttpServletParamMultiMap(request);
        final HttpRequestBuilder httpRequestBuilder = httpRequestBuilder().setParams(params)
                .setHeaders(headers).setUri(request.getPathInfo())
                .setMethod(request.getMethod());

        setRequestBodyIfNeeded(request, httpRequestBuilder);
        setupRequestHandler(asyncContext, response, httpRequestBuilder);
        return httpRequestBuilder.build();
    }

    private static void setupRequestHandler(final AsyncContext asyncContext,
                                            final HttpServletResponse response,
                                            final HttpRequestBuilder httpRequestBuilder) {

        httpRequestBuilder.setTextReceiver((code, contentType, body) -> {
            response.setHeader("Content-Type", contentType);
            try {
                final ServletOutputStream outputStream = response.getOutputStream();
                outputStream.write(body.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
                asyncContext.complete();
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private static void setRequestBodyIfNeeded(final HttpServletRequest request,
                                               final HttpRequestBuilder httpRequestBuilder) {

        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {
            final String body = readBody(request);
            if (body != null) {
                httpRequestBuilder.setBody(body);
            }
        }
    }

    private static String readBody(final HttpServletRequest request) {
        try {
            final ServletInputStream inputStream = request.getInputStream();
            return IO.read(inputStream, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

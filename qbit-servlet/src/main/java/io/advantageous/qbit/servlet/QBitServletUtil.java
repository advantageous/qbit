package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpRequestBuilder;
import io.advantageous.qbit.util.MultiMap;
import org.boon.IO;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.advantageous.qbit.http.HttpRequestBuilder.httpRequestBuilder;

/**
 * Created by rhightower on 2/12/15.
 */
public class QBitServletUtil {

    public static HttpRequest convertRequest(final AsyncContext asyncContext) {

        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        final HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
        final MultiMap<String, String> headers = new HttpServletHeaderMultiMap(request);
        final MultiMap<String, String> params = new HttpServletParamMultiMap(request);
        final HttpRequestBuilder httpRequestBuilder =
                                    httpRequestBuilder().setParams(params)
                                        .setHeaders(headers).setUri(request.getRequestURI())
                                        .setMethod(request.getMethod());

        setRequestBodyIfNeeded(request, httpRequestBuilder);
        setupRequestHandler(asyncContext, response, httpRequestBuilder);
        return httpRequestBuilder.build();
    }

    private static void setupRequestHandler(AsyncContext asyncContext, HttpServletResponse response, HttpRequestBuilder httpRequestBuilder) {
        httpRequestBuilder.setTextResponse((code, contentType, body) -> {
            response.setHeader("Content-Type", contentType);
            try {
                final ServletOutputStream outputStream = response.getOutputStream();
                IO.write(outputStream, body);
                outputStream.close();
                asyncContext.complete();
            } catch (IOException e) {
               throw new IllegalStateException(e);
            }
        });
    }

    private static void setRequestBodyIfNeeded(HttpServletRequest request, HttpRequestBuilder httpRequestBuilder) {
        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {
            final String body = readBody(request);
            if (body!=null) {
                httpRequestBuilder.setBody(body);
            }
        }
    }

    private static String readBody(HttpServletRequest request) {
        try {
            final ServletInputStream inputStream = request.getInputStream();
            final String body = IO.read(inputStream, StandardCharsets.UTF_8);
            return body;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

package io.advantageous.qbit.servlet;

import io.advantageous.qbit.http.HttpRequest;
import io.advantageous.qbit.http.HttpRequestBuilder;
import io.advantageous.qbit.util.MultiMap;
import org.boon.IO;

import javax.servlet.AsyncContext;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.advantageous.qbit.http.HttpRequestBuilder.httpRequestBuilder;

/**
 * Created by rhightower on 2/12/15.
 */
public class QBitServletUtil {

    public static HttpRequest convertRequest(final AsyncContext asyncContext) {

        final HttpServletRequest request = (HttpServletRequest) asyncContext.getRequest();
        MultiMap<String, String> headers = new HttpServletHeaderMultiMap(request);
        MultiMap<String, String> params = new HttpServletParamMultiMap(request);

        final HttpRequestBuilder httpRequestBuilder = httpRequestBuilder().setParams(params).setHeaders(headers)
                .setMethod(request.getMethod());

        if (request.getMethod().equals("POST") || request.getMethod().equals("PUT")) {

            final String body = readBody(request);
            if (body!=null) {
                httpRequestBuilder.setBody(body);
            }
        }
        return httpRequestBuilder.build();
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

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

package io.advantageous.qbit.http;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import org.junit.Test;

import static io.advantageous.boon.Boon.puts;
import static io.advantageous.boon.Exceptions.die;

public class HttpRequestBuilderTest {

    boolean ok;

    @Test
    public void test() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.addParam("user", "rick");
        requestBuilder.addParam("password", "duck soup");


        final HttpRequest request = requestBuilder.buildClientRequest();


        ok = "foo/bar/baz?password=duck+soup&user=rick".equals(request.getUri())
                || die();


        ok = "GET".equals(request.getMethod())
                || die();


        ok = request.getBody().length == 0 || die();

        requestBuilder.setId(9);
        requestBuilder.setTimestamp(10);
        requestBuilder.setRemoteAddress("remote");

        final HttpRequest request1 = requestBuilder.build();


        final HttpRequest request2 = requestBuilder.build();


        ok = request1.hashCode() == request2.hashCode() || die();
        ok = request1.equals(request2) || die();
        ok = request.getBody().equals(request1.getBody()) || die();
        ok = request.getBodyAsString().equals(request1.getBodyAsString()) || die();
        ok = request.getContentType().equals(request1.getContentType()) || die(request.getContentType());
        ok = request.getMethod().equals(request1.getMethod()) || die();
        ok = request2.getMessageId() == request1.getMessageId() || die();
        ok = request2.getRemoteAddress().equals(request1.getRemoteAddress()) || die();

        puts(request);

        request.isJson();

    }


    @Test
    public void testFormPost() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.addParam("user", "rick");
        requestBuilder.addParam("password", "duck soup");
        requestBuilder.setMethod("POST");


        final HttpRequest request = requestBuilder.buildClientRequest();


        ok = "password=duck+soup&user=rick".equals(request.getBodyAsString())
                || die(request.getBodyAsString());


        ok = "POST".equals(request.getMethod())
                || die();


        ok = "foo/bar/baz".equals(request.getUri())
                || die();


    }


    @Test
    public void testFormJsonPost() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.setJsonBodyForPost("\"hi\"");

        final HttpRequest request = requestBuilder.build();


        ok = "\"hi\"".equals(request.getBodyAsString())
                || die(request.getBodyAsString());


        ok = "POST".equals(request.getMethod())
                || die();


        ok = "foo/bar/baz".equals(request.getUri())
                || die();


    }
}
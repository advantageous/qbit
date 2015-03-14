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
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
package io.advantageous.consul.endpoints;

import io.advantageous.boon.Boon;
import io.advantageous.boon.Str;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.option.Consistency;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpResponse;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;

import static io.advantageous.consul.domain.ConsulException.die;

/**
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class RequestUtils {

    public static <T> ConsulResponse<T> consulResponse(final Class<T> responseType, final HttpResponse response) {

        T responseObject = null;

        if (response.code() == 200) {

            if (!Str.isEmpty( response.body() )) {
                responseObject = Boon.fromJson(response.body(), responseType);
            }

        } else {
            die("Unable to read response", response.code(), response.body());
        }

        int index = Integer.valueOf(response.headers().getFirst("X-Consul-Index"));
        long lastContact = Long.valueOf(response.headers().getFirst("X-Consul-Lastcontact"));
        boolean knownLeader = Boolean.valueOf(response.headers().getFirst("X-Consul-Knownleader"));
        ConsulResponse<T> consulResponse = new ConsulResponse<T>(responseObject, lastContact, knownLeader, index);

        return consulResponse;
    }


    public static <T> ConsulResponse<List<T>> consulResponseList(final Class<T> responseType, final HttpResponse response) {

        List<T> responseObject = null;

        if (response.code() == 200) {

            if (!Str.isEmpty( response.body() )) {
                responseObject = Boon.fromJsonArray(response.body(), responseType);
            }

        } else {
            die("Unable to read response", response.code(), response.body());
        }

        int index = Integer.valueOf(response.headers().getFirst("X-Consul-Index"));
        long lastContact = Long.valueOf(response.headers().getFirst("X-Consul-Lastcontact"));
        boolean knownLeader = Boolean.valueOf(response.headers().getFirst("X-Consul-Knownleader"));
        ConsulResponse<List<T>> consulResponse = new ConsulResponse<>(responseObject, lastContact, knownLeader, index);

        return consulResponse;
    }

    public static String decodeBase64(String value) {
        return new String(Base64.getDecoder().decode(value));
    }



    public static  HttpRequestBuilder getHttpRequestBuilder(
            final String datacenter,
            final String tag,
            final RequestOptions requestOptions,
            final String path) {

        final HttpRequestBuilder httpRequestBuilder = HttpRequestBuilder.httpRequestBuilder();

        httpRequestBuilder.setUri(cleanURI(path));

        if (!Str.isEmpty(datacenter)) {
            httpRequestBuilder.addParam("dc", datacenter);
        }

        if (!Str.isEmpty(tag)) {
            httpRequestBuilder.addParam("tag", tag);
        }

        if(requestOptions.isBlocking()) {
            httpRequestBuilder.addParam("wait", requestOptions.getWait());
            httpRequestBuilder.addParam("index", String.valueOf(requestOptions.getIndex()));
        }

        if(requestOptions.getConsistency() == Consistency.CONSISTENT) {
            httpRequestBuilder.addParam("consistent", "true");

        }
        if(requestOptions.getConsistency() == Consistency.STALE) {
            httpRequestBuilder.addParam("stale", "true");
        }
        return httpRequestBuilder;
    }

    public static String cleanURI(final String path) {
        String requestPath;
        try {
            URI uri = new URI("http", "fakedomain", path, null);
            requestPath = uri.getRawPath();
        } catch (URISyntaxException e) {
            requestPath = path;
        }

        return requestPath;

    }


}

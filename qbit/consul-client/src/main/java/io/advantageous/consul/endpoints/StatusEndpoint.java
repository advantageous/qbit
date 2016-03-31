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

import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.http.request.HttpRequestBuilder;

import java.net.URI;
import java.util.List;

import static io.advantageous.boon.json.JsonFactory.fromJson;
import static io.advantageous.boon.json.JsonFactory.fromJsonArray;
import static io.advantageous.consul.domain.ConsulException.die;

public class StatusEndpoint extends Endpoint {


    public StatusEndpoint(String scheme, String host, String port, String rootPath) {
        super(scheme, host, port, rootPath);
    }

    public StatusEndpoint(URI rootURI, String rootPath) {
        super(rootURI, rootPath);
    }

    /**
     * Retrieves the host/port of the Consul leader.
     * <p>
     * GET /v1/status/leader
     *
     * @return The host/port of the leader.
     */
    public String getLeader() {


        final URI uri = createURI("/leader");

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, RequestOptions.BLANK, "");

        final HTTP.Response httpResponse = HTTP.getResponse(uri + "?" + httpRequestBuilder.paramString());

        if (httpResponse.code() != 200) {
            die("Unable to retrieve the leader", uri, httpResponse.code(), httpResponse.body());
        }

        return fromJson(httpResponse.body(), String.class).replace("\"", "").trim();
    }

    /**
     * Retrieves a list of host/ports for raft peers.
     * <p>
     * GET /v1/status/peers
     *
     * @return List of host/ports for raft peers.
     */
    public List<String> getPeers() {


        final URI uri = createURI("/peers");

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(null, null, RequestOptions.BLANK, "");

        final HTTP.Response httpResponse = HTTP.getResponse(uri + "?" + httpRequestBuilder.paramString());

        if (httpResponse.code() != 200) {
            die("Unable to get the peers", uri, httpResponse.code(), httpResponse.body());
        }

        return fromJsonArray(httpResponse.body(), String.class);
    }

}

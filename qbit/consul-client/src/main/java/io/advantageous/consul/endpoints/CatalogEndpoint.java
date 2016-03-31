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

import io.advantageous.consul.domain.CatalogNode;
import io.advantageous.consul.domain.CatalogService;
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.Node;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.http.request.HttpRequestBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.json.JsonFactory.fromJsonArray;
import static io.advantageous.consul.domain.ConsulException.die;


/**
 * HTTP Client for /v1/catalog/ endpoints.
 * <p>
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class CatalogEndpoint extends Endpoint {

    public CatalogEndpoint(String scheme, String host, String port, String rootPath) {
        super(scheme, host, port, rootPath);
    }

    public CatalogEndpoint(URI rootURI, String rootPath) {
        super(rootURI, rootPath);
    }

    /**
     * Retrieves all datacenters.
     * <p>
     * GET /v1/catalog/datacenters
     *
     * @return A list of datacenter names.
     */
    public List<String> getDatacenters() {

        URI uri = createURI("/datacenters");

        HTTP.Response httpResponse = HTTP.getResponse(uri.toString());

        if (httpResponse.code() == 200) {
            return fromJsonArray(httpResponse.body(), String.class);
        }
        die("Unable to retrieve the datacenters", uri, httpResponse.code(), httpResponse.body());
        return Collections.emptyList();
    }


    /**
     * Retrieves all services for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(RequestOptions requestOptions) {
        return getServices(null, null, requestOptions);
    }

    public ConsulResponse<Map<String, List<String>>> getServices() {
        return getServices(null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves all services for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(
            @SuppressWarnings("SameParameterValue") final String datacenter, @SuppressWarnings("SameParameterValue") final String tag,
            final RequestOptions requestOptions) {


        final URI uri = createURI("/services");


        final HttpRequestBuilder httpRequestBuilder = RequestUtils.getHttpRequestBuilder(datacenter, tag, requestOptions, "/");


        HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse.code() != 200) {
            die("Unable to retrieve the datacenters", uri, httpResponse.code(), httpResponse.body());
        }

        //noinspection unchecked
        return (ConsulResponse<Map<String, List<String>>>) (Object) RequestUtils.consulResponse(Map.class, httpResponse);

    }

    /**
     * Retrieves a single service.
     * <p>
     * GET /v1/catalog/service/{service}
     *
     * @param serviceName service name
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(@SuppressWarnings("SameParameterValue") final String serviceName) {
        return getService(serviceName, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single service for a given datacenter.
     * <p>
     * GET /v1/catalog/service/{service}?dc={datacenter}
     *
     * @param serviceName service name
     * @param datacenter  datacenter
     * @param tag         tag
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(final String serviceName,
                                                           final String datacenter, final String tag
    ) {
        return getService(serviceName, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single service with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/catalog/service/{service}
     *
     * @param serviceName    service name
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String serviceName, RequestOptions requestOptions) {
        return getService(serviceName, null, null, requestOptions);
    }

    /**
     * Retrieves a single service for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/catalog/service/{service}?dc={datacenter}
     *
     * @param serviceName    service name
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(final String serviceName, final String datacenter, final String tag,
                                                           RequestOptions requestOptions) {


        final URI uri = createURI("/service/" + serviceName);


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "/");


        HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse.code() != 200) {
            die("Unable to retrieve the service", uri, httpResponse.code(), httpResponse.body());
        }

        return RequestUtils.consulResponseList(CatalogService.class, httpResponse);

    }

    /**
     * Retrieves a single node.
     * <p>
     * GET /v1/catalog/node/{node}
     *
     * @param node node
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node) {
        return getNode(node, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single node for a given datacenter.
     * <p>
     * GET /v1/catalog/node/{node}?dc={datacenter}
     *
     * @param node       node
     * @param datacenter dc
     * @param tag        tag
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node, final String datacenter, final String tag
    ) {
        return getNode(node, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single node with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/catalog/node/{node}
     *
     * @param node           node
     * @param requestOptions The Query Options to use.
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node,
                                               final RequestOptions requestOptions) {
        return getNode(node, null, null, requestOptions);
    }

    /**
     * Retrieves a single node for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/catalog/node/{node}?dc={datacenter}
     *
     * @param node           node
     * @param datacenter     dc
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node,
                                               final String datacenter,
                                               final String tag,
                                               final RequestOptions requestOptions) {

        final URI uri = createURI("/node/" + node);

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");

        final HTTP.Response httpResponse = HTTP.getResponse(uri + "?" + httpRequestBuilder.paramString());
        if (httpResponse.code() != 200) {
            die("Unable to retrieve the node", uri, httpResponse.code(), httpResponse.body());
        }
        return RequestUtils.consulResponse(CatalogNode.class, httpResponse);
    }

    public ConsulResponse<List<Node>> getNodes(final String datacenter,
                                               final String tag,
                                               final RequestOptions requestOptions) {


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");


        final URI uri = createURI("/nodes");


        final HTTP.Response httpResponse = HTTP.getResponse(uri + "?" + httpRequestBuilder.paramString());

        if (httpResponse.code() != 200) {
            die("Unable to retrieve the nodes", uri, httpResponse.code(), httpResponse.body());
        }
        return RequestUtils.consulResponseList(Node.class, httpResponse);

    }


    public ConsulResponse<List<Node>> getNodes(final String datacenter) {
        return getNodes(datacenter, null, RequestOptions.BLANK);
    }

    public ConsulResponse<List<Node>> getNodes(@SuppressWarnings("SameParameterValue") final String datacenter, @SuppressWarnings("SameParameterValue") final String tag) {
        return getNodes(datacenter, tag, RequestOptions.BLANK);
    }

    public ConsulResponse<List<Node>> getNodes(RequestOptions requestOptions) {
        return getNodes(null, null, RequestOptions.BLANK);
    }


    public ConsulResponse<List<Node>> getNodes() {
        return getNodes(null, null, RequestOptions.BLANK);
    }
}

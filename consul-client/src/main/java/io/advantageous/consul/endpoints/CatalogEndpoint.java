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
import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.CatalogNode;
import io.advantageous.consul.domain.CatalogService;
import io.advantageous.consul.domain.Node;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.http.request.HttpResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.advantageous.consul.domain.ConsulException.die;


/**
 * HTTP Client for /v1/catalog/ endpoints.
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class CatalogEndpoint {

    private final HttpClient httpClient;
    private final String rootPath;


    /**
     *
     * @param httpClient http client
     * @param rootPath root path
     */
    public CatalogEndpoint(final HttpClient httpClient, final String rootPath) {
        this.httpClient = httpClient;
        this.rootPath = rootPath;
    }


    /**
     * Retrieves all datacenters.
     *
     * GET /v1/catalog/datacenters
     *
     * @return A list of datacenter names.
     */
    public List<String> getDatacenters() {

        final String path = rootPath + "/datacenters";
        final HttpResponse httpResponse = httpClient.get(path);

        if (httpResponse.code() == 200) {
           return Boon.fromJsonArray(httpResponse.body(), String.class);
        }
        die("Unable to retrieve the datacenters", path, httpResponse.code(), httpResponse.body());
        return Collections.emptyList();
    }


    /**
     * Retrieves all services for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     *
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
     *
     * GET /v1/catalog/services?dc={datacenter}
     *
     * @param datacenter datacenter
     * @param tag tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a map of service name to list of tags.
     */
    public ConsulResponse<Map<String, List<String>>> getServices(
                                    final String datacenter, final String tag,
                                    final RequestOptions requestOptions) {



        final String path = rootPath + "/services";


        final HttpRequestBuilder httpRequestBuilder = RequestUtils.getHttpRequestBuilder(datacenter, tag, requestOptions, path);


        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());
        if (httpResponse.code()!=200) {
            die("Unable to retrieve the datacenters", path, httpResponse.code(), httpResponse.body());
        }

        return (ConsulResponse<Map<String, List<String>>>)(Object) RequestUtils.consulResponse(Map.class, httpResponse);

    }

    /**
     * Retrieves a single service.
     *
     * GET /v1/catalog/service/{service}
     *
     * @param serviceName service name
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(final String serviceName) {
        return getService(serviceName, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single service for a given datacenter.
     *
     * GET /v1/catalog/service/{service}?dc={datacenter}
     * @param serviceName service name
     * @param datacenter datacenter
     * @param tag tag
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
     *
     * GET /v1/catalog/service/{service}
     * @param serviceName service name
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(String serviceName, RequestOptions requestOptions) {
        return getService(serviceName, null, null, requestOptions);
    }

    /**
     * Retrieves a single service for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     *
     * GET /v1/catalog/service/{service}?dc={datacenter}
     * @param serviceName service name
     * @param datacenter datacenter
     * @param tag tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing
     * {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<List<CatalogService>> getService(final String serviceName, final String datacenter, final String tag,
                                                           RequestOptions requestOptions) {


        final String path = rootPath + "/service/" + serviceName;

        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                                .getHttpRequestBuilder(datacenter, tag, requestOptions, path);


        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());
        if (httpResponse.code()!=200) {
            die("Unable to retrieve the service", path, httpResponse.code(), httpResponse.body());
        }

        return RequestUtils.consulResponseList(CatalogService.class, httpResponse);

    }

    /**
     * Retrieves a single node.
     *
     * GET /v1/catalog/node/{node}
     *
     * @param node node
     *
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node) {
        return getNode(node, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single node for a given datacenter.
     *
     * GET /v1/catalog/node/{node}?dc={datacenter}
     * @param node node
     * @param datacenter dc
     * @param tag tag
     *
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node, final String datacenter, final String tag
                                               ) {
        return getNode(node, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves a single node with {@link io.advantageous.consul.domain.option.RequestOptions}.
     *
     * GET /v1/catalog/node/{node}
     *
     * @param node node
     * @param requestOptions The Query Options to use.
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node,
                                               final RequestOptions requestOptions) {
        return getNode(node, null, null, requestOptions);
    }

    /**
     * Retrieves a single node for a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     *
     * GET /v1/catalog/node/{node}?dc={datacenter}
     *
     * @param node node
     * @param datacenter dc
     * @param tag tag
     * @param requestOptions The Query Options to use.
     * @return A list of matching {@link io.advantageous.consul.domain.CatalogService} objects.
     */
    public ConsulResponse<CatalogNode> getNode(final String node,
                                               final String datacenter,
                                               final String tag,
                                               final RequestOptions requestOptions) {

        final String path = rootPath + "/node/" + node;
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, path);

        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());
        if (httpResponse.code()!=200) {
            die("Unable to retrieve the node", path, httpResponse.code(), httpResponse.body());
        }
        return RequestUtils.consulResponse(CatalogNode.class, httpResponse);
    }

    public ConsulResponse<List<Node>> getNodes(final String datacenter,
                                               final String tag,
                                               final RequestOptions requestOptions) {


        final String path = rootPath + "/nodes";
        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, path);

        final HttpResponse httpResponse = httpClient.sendRequestAndWait(httpRequestBuilder.build());
        if (httpResponse.code()!=200) {
            die("Unable to retrieve the nodes", path, httpResponse.code(), httpResponse.body());
        }
        return RequestUtils.consulResponseList(Node.class, httpResponse);

    }


    public ConsulResponse<List<Node>> getNodes(final String datacenter) {
       return getNodes(datacenter, null, RequestOptions.BLANK);
    }

    public ConsulResponse<List<Node>> getNodes(final String datacenter, final String tag) {
        return getNodes(datacenter, tag, RequestOptions.BLANK);
    }

    public ConsulResponse<List<Node>> getNodes(RequestOptions requestOptions) {
        return getNodes(null, null, RequestOptions.BLANK);
    }


    public ConsulResponse<List<Node>> getNodes() {
        return getNodes(null, null, RequestOptions.BLANK);
    }
}

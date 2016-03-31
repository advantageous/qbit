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

import io.advantageous.consul.domain.ConsulResponse;
import io.advantageous.consul.domain.HealthCheck;
import io.advantageous.consul.domain.ServiceHealth;
import io.advantageous.consul.domain.Status;
import io.advantageous.consul.domain.option.RequestOptions;
import io.advantageous.qbit.http.HTTP;
import io.advantageous.qbit.http.request.HttpRequestBuilder;

import java.net.URI;
import java.util.List;

import static io.advantageous.consul.domain.ConsulException.die;

/**
 * HTTP Client for /v1/health/ endpoints.
 * <p>
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 */
public class HealthEndpoint extends Endpoint {

    public HealthEndpoint(String scheme, String host, String port, String rootPath) {
        super(scheme, host, port, rootPath);
    }

    public HealthEndpoint(URI rootURI, String rootPath) {
        super(rootURI, rootPath);
    }

    /**
     * Retrieves the healthchecks for a node.
     * <p>
     * GET /v1/health/node/{node}
     *
     * @param node node
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node) {
        return getNodeChecks(node, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a node in a given datacenter.
     * <p>
     * <code>GET /v1/health/node/{node}?dc={datacenter}</code>
     *
     * @param node       node
     * @param datacenter datacenter
     * @param tag        tag
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node, final String datacenter,
                                                           final String tag) {
        return getNodeChecks(node, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a node with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/node/{node}
     *
     * @param node           node
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(String node, RequestOptions requestOptions) {
        return getNodeChecks(node, null, null, requestOptions);
    }

    /**
     * Retrieves the healthchecks for a node in a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/node/{node}?dc={datacenter}
     *
     * @param node           node
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getNodeChecks(final String node,
                                                           final String datacenter,
                                                           final String tag,
                                                           final RequestOptions requestOptions) {


        final URI uri = createURI("/node/" + node);


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");


        final HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse == null || httpResponse.code() != 200) {
            die("Unable to retrieve the service", uri, httpResponse);
        }

        return RequestUtils.consulResponseList(HealthCheck.class, httpResponse);

    }

    /**
     * Retrieves the healthchecks for a service.
     * <p>
     * GET /v1/health/service/{service}
     *
     * @param service service
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(final String service) {
        return getNodeChecks(service, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a service in a given datacenter.
     * <p>
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service    service
     * @param datacenter datacenter
     * @param tag        tag
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service, final String datacenter,
                                                              final String tag) {
        return getNodeChecks(service, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a service with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/service/{service}
     *
     * @param service        service
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service, RequestOptions requestOptions) {
        return getNodeChecks(service, null, null, requestOptions);
    }

    /**
     * Retrieves the healthchecks for a service in a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service        service
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getServiceChecks(String service, final String datacenter,
                                                              final String tag,
                                                              RequestOptions requestOptions) {

        final URI uri = createURI("/checks/" + service);


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");


        final HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());


        if (httpResponse.code() != 200) {
            die("Unable to retrieve the service", uri, httpResponse.code(), httpResponse.body());
        }

        return RequestUtils.consulResponseList(HealthCheck.class, httpResponse);
    }

    /**
     * Retrieves the healthchecks for a state.
     * <p>
     * GET /v1/health/state/{state}
     *
     * @param status The state to query.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(Status status) {
        return getChecksByState(status, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a state in a given datacenter.
     * <p>
     * GET /v1/health/state/{state}?dc={datacenter}
     *
     * @param status     The state to query.
     * @param datacenter datacenter
     * @param tag        tag
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(final Status status, final String datacenter,
                                                              final String tag) {
        return getChecksByState(status, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for a state with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/state/{state}
     *
     * @param status         The state to query.
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(Status status, RequestOptions requestOptions) {
        return getChecksByState(status, null, null, requestOptions);
    }

    /**
     * Retrieves the healthchecks for a state in a given datacenter with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/state/{state}?dc={datacenter}
     *
     * @param status         The state to query.
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<HealthCheck>> getChecksByState(final Status status,
                                                              final String datacenter,
                                                              final String tag,
                                                              final RequestOptions requestOptions) {


        final URI uri = createURI("/state/" + status.getName());


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");


        final HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());


        if (httpResponse.code() != 200) {
            die("Unable to retrieve the service", uri, httpResponse.code(), httpResponse.body());
        }

        return RequestUtils.consulResponseList(HealthCheck.class, httpResponse);

    }

    /**
     * Retrieves the healthchecks for all healthy nodes.
     * <p>
     * GET /v1/health/service/{service}?passing
     *
     * @param serviceName The service to query.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServices(final String serviceName) {
        return getHealthyServices(serviceName, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all healthy nodes in a given datacenter.
     * <p>
     * <code>GET /v1/health/service/{service}?dc={datacenter}&amp;passing</code>
     *
     * @param serviceName The service to query.
     * @param datacenter  datacenter
     * @param tag         tag
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServicesByDatacenterAndTag(final String serviceName,
                                                                                    final String datacenter,
                                                                                    final String tag) {
        return getHealthyServices(serviceName, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all healthy nodes with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * <code>GET /v1/health/service/{service}?passing</code>
     *
     * @param serviceName    The service to query.
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServicesWithRequestOptions(String serviceName, RequestOptions requestOptions) {
        return getHealthyServices(serviceName, null, null, requestOptions);
    }

    /**
     * Retrieves the healthchecks for all healthy nodes in a given datacenter with
     * {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * <code>GET /v1/health/service/{service}?dc={datacenter}&amp;passing</code>
     *
     * @param serviceName    The service to query.
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getHealthyServices(final String serviceName,
                                                                  final String datacenter,
                                                                  final String tag,
                                                                  final RequestOptions requestOptions) {


        final URI uri = createURI("/service/" + serviceName);


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");


        httpRequestBuilder.addParam("passing", "true");


        final String url = uri.toString() + "?" + httpRequestBuilder.paramString();
        final HTTP.Response httpResponse = HTTP.getResponse(url);


        if (httpResponse == null) {
            die("Unable to retrieve the service, consul request timed out", uri);
        }

        if (httpResponse.code() != 200) {
            die("Unable to retrieve the service", uri, httpResponse.code(), httpResponse.body());
        }

        return RequestUtils.consulResponseList(ServiceHealth.class, httpResponse);
    }

    /**
     * Retrieves the healthchecks for all nodes.
     * <p>
     * GET /v1/health/service/{service}
     *
     * @param service The service to query.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllNodes(String service) {
        return getAllNodes(service, null, null, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all nodes in a given datacenter.
     * <p>
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service    The service to query.
     * @param datacenter datacenter
     * @param tag        tag
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllNodes(String service,
                                                           final String datacenter, final String tag) {
        return getAllNodes(service, datacenter, tag, RequestOptions.BLANK);
    }

    /**
     * Retrieves the healthchecks for all nodes with {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/service/{service}
     *
     * @param service        The service to query.
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllNodes(String service, RequestOptions requestOptions) {
        return getAllNodes(service, null, null, requestOptions);
    }

    /**
     * Retrieves the healthchecks for all nodes in a given datacenter with
     * {@link io.advantageous.consul.domain.option.RequestOptions}.
     * <p>
     * GET /v1/health/service/{service}?dc={datacenter}
     *
     * @param service        The service to query.
     * @param datacenter     datacenter
     * @param tag            tag
     * @param requestOptions The Query Options to use.
     * @return A {@link io.advantageous.consul.domain.ConsulResponse} containing a list of
     * {@link io.advantageous.consul.domain.HealthCheck} objects.
     */
    public ConsulResponse<List<ServiceHealth>> getAllNodes(final String service,
                                                           final String datacenter,
                                                           final String tag,
                                                           final RequestOptions requestOptions) {


        final URI uri = createURI("/service/" + service);


        final HttpRequestBuilder httpRequestBuilder = RequestUtils
                .getHttpRequestBuilder(datacenter, tag, requestOptions, "");


        final HTTP.Response httpResponse = HTTP.getResponse(uri.toString() + "?" + httpRequestBuilder.paramString());

        if (httpResponse == null) {
            die("No response from server for get all nodes request");
        }

        if (httpResponse.code() != 200) {
            die("Unable to retrieve the service", uri, httpResponse.code(), httpResponse.body());
        }

        return RequestUtils.consulResponseList(ServiceHealth.class, httpResponse);
    }
}

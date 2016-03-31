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

import io.advantageous.boon.core.Str;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.consul.domain.*;
import io.advantageous.qbit.http.HTTP;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.core.reflection.MapObjectConversion.fromMap;
import static io.advantageous.boon.json.JsonFactory.*;
import static io.advantageous.consul.domain.ConsulException.die;
import static io.advantageous.consul.domain.NotRegisteredException.notRegistered;

/**
 * <p>
 * HTTP Client for Consul agent HTTP API.
 * This is under the path: /v1/agent/ endpoints
 * </p>
 * <p>
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#agent">The Consul API Docs</a>
 */
@SuppressWarnings("WeakerAccess")
public class AgentEndpoint extends Endpoint {


    public AgentEndpoint(String scheme, String host, String port, String rootPath) {
        super(scheme, host, port, rootPath);
    }

    public AgentEndpoint(URI rootURI, String rootPath) {
        super(rootURI, rootPath);
    }

    /**
     * Checks to see if a service is registered with the local agent..
     *
     * @param serviceId service id
     * @return <code>true</code> if a particular service is registered with
     * the local Consul agent, otherwise <code>false</code>.
     */
    public boolean isServiceRegistered(final String serviceId) {
        Map<String, Service> serviceIdToService = getServices();
        return serviceIdToService.containsKey(serviceId);
    }

    /**
     * Pings the Consul Agent.
     */
    public void pingAgent() {

        HTTP.Response response = HTTP.getResponse(createURI("/self").toString());

        if (response.status() != 200) {
            die("Error pinging Consul", response.payloadAsString());
        }
    }


    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port        The public facing port of the service to register with Consul.
     * @param ttl         Time to live for the Consul dead man's switch.
     * @param serviceName Service name to register.
     * @param serviceId   Service id to register.
     * @param tags        Tags to register with.
     */
    public void registerService(final int port, final long ttl,
                                final String serviceName, final String serviceId,
                                final String... tags) {
        RegistrationCheck check = new RegistrationCheck();
        check.setTtl("" + ttl + "s");
        registerServiceWithRegistrationCheck(port, check, serviceName, serviceId, tags);
    }

    /**
     * Registers the client as a service with Consul.
     * Registration enables
     * the use of check with a script.
     *
     * @param port        The public facing port of the service to register with Consul.
     * @param script      Health script for Consul to use.
     * @param interval    Health script run interval in seconds.
     * @param serviceName Service name to register.
     * @param serviceId   Service id to register.
     * @param tags        Tags to register with.
     */
    public void registerServiceWithScript(final int port,
                                          final String script,
                                          final long interval,
                                          final String serviceName,
                                          final String serviceId,
                                          final String... tags) {
        RegistrationCheck check = new RegistrationCheck();
        check.setScript(script);
        check.setInterval("" + interval + "s");
        registerServiceWithRegistrationCheck(port, check, serviceName, serviceId, tags);
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port  The public facing port of the service to register with Consul.
     * @param check The health check to run periodically.  Can be null.
     * @param name  Service name to register.
     * @param id    Service id to register.
     * @param tags  Tags to register with.
     */
    public void registerServiceWithRegistrationCheck(int port, RegistrationCheck check, String name, String id, String... tags) {
        Registration registration = new Registration();
        registration.setPort(port);
        registration.setCheck(check);
        registration.setName(name);
        registration.setId(id);
        registration.setTags(tags);
        register(registration);
    }

    /**
     * Register a service with Consul.
     *
     * @param registration The registration payload.
     */
    public void register(final Registration registration) {


        final URI uri = createURI("/service/register");
        HTTP.Response response = HTTP.jsonRestCallViaPUT(uri.toString(), toJson(registration));

        if (response.status() != 200) {
            die("Error registering service with Consul", uri, registration, response.payloadAsString());
        }
    }

    /**
     * Remove registration of a particular service.
     *
     * @param serviceId the service id that you want to remove.
     */
    public void deregister(final String serviceId) {

        final URI uri = createURI("/service/deregister/" + serviceId);

        HTTP.Response response = HTTP.getResponse(uri.toString());

        if (response.status() != 200) {
            die("Error removing registration of service with Consul",
                    uri, serviceId, response.status(), response.payloadAsString());
        }

    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     */
    public void registerCheck(String checkId, String name, String script, long interval) {
        registerCheckWithNotes(checkId, name, script, interval, null);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId  The Check ID to use.  Must be unique for the Agent.
     * @param name     The Check Name.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param notes    Human readable notes.  Not used by Consul.
     */
    public void registerCheckWithNotes(String checkId, String name,
                                       String script, long interval,
                                       @SuppressWarnings("SameParameterValue") String notes) {
        Check check = new Check();
        check.setId(checkId);
        check.setName(name);
        check.setScript(script);
        check.setInterval(String.format("%ss", interval));
        check.setNotes(notes);
        registerCheck(check);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId The Check ID to use.  Must be unique for the Agent.
     * @param name    The Check Name.
     * @param ttl     Time to live for the Consul dead man's switch.
     */
    public void registerCheck(String checkId, String name, long ttl) {
        registerCheck(checkId, name, ttl, null);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param checkId The Check ID to use.  Must be unique for the Agent.
     * @param name    The Check Name.
     * @param ttl     Time to live for the Consul dead man's switch.
     * @param notes   Human readable notes.  Not used by Consul.
     */
    public void registerCheck(String checkId, String name, long ttl, @SuppressWarnings("SameParameterValue") String notes) {
        Check check = new Check();
        check.setId(checkId);
        check.setName(name);
        check.setTtl(String.format("%ss", ttl));
        check.setNotes(notes);
        registerCheck(check);
    }

    /**
     * Registers a Health Check with the Agent.
     *
     * @param check The Check to register.
     */
    public void registerCheck(Check check) {


        final URI uri = createURI("/check/register");


        HTTP.Response response = HTTP.jsonRestCallViaPUT(uri.toString(), toJson(check));

        if (response.status() != 200) {
            die("Error removing registration of service with Consul",
                    uri, check, response.status(), response.statusMessageAsString(),
                    response.payloadAsString());
        }
    }

    /**
     * De-registers a Health Check with the Agent
     *
     * @param checkId the id of the Check to deregister
     */
    public void deregisterCheck(String checkId) {

        final URI uri = createURI("/check/deregister/" + checkId);


        HTTP.Response response = HTTP.getResponse(uri.toString());

        if (response.status() != 200) {
            die("Error removing registration of service with Consul",
                    uri, checkId, response.status(), response.statusMessageAsString(),
                    response.payloadAsString());
        }


    }

    /**
     * <p>
     * Retrieves the Agent's configuration and member information.
     * </p>
     * GET /v1/agent/self
     *
     * @return The Agent information.
     */
    public AgentInfo getAgentInfo() {

        final URI uri = createURI("/self");


        HTTP.Response response = HTTP.getResponse(uri.toString());

        if (response.status() != 200) {
            die("Error getting info about this agent",
                    uri, response.status(), response.statusMessageAsString(),
                    response.payloadAsString());
        }


        return fromJson(response.payloadAsString(), AgentInfo.class);

    }

    /**
     * Retrieves all checks registered with the Agent.
     * <p>
     * GET /v1/agent/checks
     *
     * @return Map of Check ID to Checks.
     */
    public Map<String, HealthCheck> getChecks() {

        final URI uri = createURI("/checks");


        final HTTP.Response response = HTTP.getResponse(uri.toString());

        final JsonParserAndMapper jsonParserAndMapper = new JsonParserFactory().create();
        if (response.status() == 200) {
            final Map<String, Object> map = jsonParserAndMapper.parseMap(response.payloadAsString());
            final Map<String, HealthCheck> returnMap = new HashMap<>(map.size());
            map.entrySet().forEach(entry -> {
                @SuppressWarnings("unchecked") HealthCheck healthCheck = fromMap((Map<String, Object>) entry.getValue(), HealthCheck.class);
                returnMap.put(entry.getKey(), healthCheck);

            });
            return returnMap;
        }
        die("Unable to get health checks", uri, response.status(), response.statusMessageAsString(),
                response.payloadAsString());
        return null;
    }

    /**
     * Retrieves all services registered with the Agent.
     * <p>
     * GET /v1/agent/services
     *
     * @return Map of Service ID to Services.
     */
    public Map<String, Service> getServices() {


        final URI uri = createURI("/services");


        final HTTP.Response response = HTTP.getResponse(uri.toString());


        final JsonParserAndMapper jsonParserAndMapper = new JsonParserFactory().create();
        if (response.status() == 200) {
            final Map<String, Object> map = jsonParserAndMapper.parseMap(response.payloadAsString());
            final Map<String, Service> returnMap = new HashMap<>(map.size());
            map.entrySet().forEach(entry -> {
                @SuppressWarnings("unchecked") Service service = fromMap((Map<String, Object>) entry.getValue(), Service.class);
                returnMap.put(entry.getKey(), service);

            });
            return returnMap;
        }

        die("Unable to get list of services", uri, response.status(), response.payloadAsString());
        return null;
    }

    /**
     * Retrieves all members that the Agent can see in the gossip pool.
     * <p>
     * GET /v1/agent/members
     *
     * @return List of Members.
     */
    public List<Member> getMembers() {

        final URI uri = createURI("/members");
        final HTTP.Response response = HTTP.getResponse(uri.toString());
        if (response.code() == 200) {
            return fromJsonArray(response.body(), Member.class);
        }
        die("Unable to read members", uri, response.code(), response.body());
        return Collections.emptyList();

    }

    /**
     * GET /v1/agent/force-leave/{node}
     * <p>
     * Instructs the agent to force a node into the "left" state.
     *
     * @param node node
     */
    public void forceLeave(String node) {


        final URI uri = createURI("/force-leave/" + node);
        final HTTP.Response httpResponse = HTTP.getResponse(uri.toString());

        if (httpResponse.code() != 200) {
            die("Unable to force leave", uri, httpResponse.code(), httpResponse.body());
        }
    }

    /**
     * Checks in with Consul.
     *
     * @param checkId The Check ID to check in.
     * @param status  The current state of the Check.
     * @param note    Any note to associate with the Check.
     */
    public void check(String checkId, Status status, String note) {


        final URI uri = createURI("/check/" + status.getUri() + "/" + checkId);


        final HTTP.Response httpResponse = Str.isEmpty(note) ? HTTP.getResponse(uri.toString()) :
                HTTP.getResponse(uri.toString() + "?note=" + note);

        if (httpResponse.code() != 200) {
            notRegistered("Unable to perform check", uri, httpResponse.code(), httpResponse.statusMessageAsString(),
                    httpResponse.body());
        }

    }

    /**
     * Prepends the default TTL prefix to the serviceId to produce a check id,
     * then delegates to check(String checkId, State state, String note)
     * This method only works with TTL checks that have not been given a custom
     * name.
     *
     * @param serviceId service id
     * @param status    state
     * @param note      note
     */
    public void checkTtl(String serviceId, Status status, String note) {
        check("service:" + serviceId, status, note);
    }

    /**
     * Sets a TTL check to "passing" state
     *
     * @param checkId check id
     */
    public void pass(final String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.PASS, null);
    }

    /**
     * Sets a TTL check to "passing" state with a note
     *
     * @param checkId check id
     * @param note    note
     */
    public void pass(String checkId, @SuppressWarnings("SameParameterValue") String note) throws NotRegisteredException {
        checkTtl(checkId, Status.PASS, note);
    }

    /**
     * Sets a TTL check to "warning" state.
     *
     * @param checkId check id
     */
    public void warn(String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.WARN, null);
    }

    /**
     * Sets a TTL check to "warning" state with a note.
     *
     * @param checkId check id
     * @param note    note
     */
    public void warn(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.WARN, note);
    }

    /**
     * Sets a TTL check to "critical" state.
     *
     * @param checkId check id
     */
    public void fail(String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, null);
    }

    /**
     * Sets a TTL check to "critical" state with a note.
     *
     * @param checkId check id
     * @param note    note
     */
    public void fail(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, note);
    }

    /**
     * Sets a TTL check to "critical" state.
     *
     * @param checkId check id
     */
    public void critical(String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, null);
    }

    /**
     * Sets a TTL check to "critical" state with a note.
     *
     * @param checkId check id
     * @param note    note
     */
    public void critical(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, note);
    }

}

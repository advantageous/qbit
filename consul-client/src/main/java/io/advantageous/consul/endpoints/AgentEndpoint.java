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
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.consul.domain.NotRegisteredException;
import io.advantageous.consul.domain.*;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.request.HttpResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.advantageous.boon.core.reflection.MapObjectConversion.fromMap;
import static io.advantageous.consul.domain.ConsulException.die;
import static io.advantageous.consul.domain.NotRegisteredException.notRegistered;

/**
 * <p>
 * HTTP Client for Consul agent HTTP API.
 * This is under the path: /v1/agent/ endpoints
 *</p>
 *
 * Note this class was heavily influenced and inspired by the Orbitz Consul client.
 *
 * @see <a href="http://www.consul.io/docs/agent/http.html#agent">The Consul API Docs</a>
 */
public class AgentEndpoint {

    private final HttpClient httpClient;
    private final String rootPath;

    /**
     *
     * @param httpClient http client to make calls through
     * @param rootPath rootPath
     */
    public AgentEndpoint(final HttpClient httpClient, final String rootPath) {
        this.httpClient = httpClient;
        this.rootPath = rootPath;
    }

    /**
     *
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
        final String path = rootPath + "/self";
        final HttpResponse httpResponse = httpClient.get(path);
        if (httpResponse==null) {
            die("Error pinging Consul, no connection");
        }
        if (httpResponse.code() != 200) {
            die("Error pinging Consul", httpResponse.body());
        }
    }

    /**
     * Registers the client as a service with Consul.  Registration enables
     * the use of checks.
     *
     * @param port The public facing port of the service to registerService with Consul.
     * @param ttl  Time to live for the Consul dead man's switch.
     * @param serviceName Service name to registerService.
     * @param serviceId   Service id to registerService.
     * @param tags Tags to registerService with.
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
     * @param port     The public facing port of the service to registerService with Consul.
     * @param script   Health script for Consul to use.
     * @param interval Health script run interval in seconds.
     * @param serviceName     Service name to registerService.
     * @param serviceId       Service id to registerService.
     * @param tags     Tags to registerService with.
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
     * @param port  The public facing port of the service to registerService with Consul.
     * @param check The health check to run periodically.  Can be null.
     * @param name  Service name to registerService.
     * @param id    Service id to registerService.
     * @param tags  Tags to registerService with.
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
        final String path = rootPath + "/service/register";
        final HttpResponse httpResponse = httpClient.putJson(path, Boon.toJson(registration));
        if (httpResponse.code() != 200) {
            die("Error registering service with Consul", path, registration, httpResponse.body());
        }
    }

    /**
     * Remove registration of a particular service.
     * @param serviceId the service id that you want to remove.
     */
    public void deregister(final String serviceId) {
        final String path = rootPath + "/service/deregister/"+serviceId;
        final HttpResponse httpResponse = httpClient.get(path);

        if (httpResponse.code() != 200) {
            die("Error removing registration of service with Consul",
                    path, serviceId, httpResponse.code(), httpResponse.body());
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
                                       String notes) {
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
    public void registerCheck(String checkId, String name, long ttl, String notes) {
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
     * @param check The Check to registerService.
     */
    public void registerCheck(Check check) {

        final String path = rootPath + "/check/register";
        final HttpResponse httpResponse = httpClient.putJson(path, Boon.toJson(check));
        if (httpResponse.code() != 200) {
            die("Error removing registration of service with Consul",
                    path, check, httpResponse.code(), httpResponse.body());
        }
    }

    /**
     * De-registers a Health Check with the Agent
     *
     * @param checkId the id of the Check to deregister
     */
    public void deregisterCheck(String checkId) {

        final String path = rootPath + "/check/deregister/"+checkId;
        final HttpResponse httpResponse = httpClient.get(path);
        if (httpResponse.code() != 200) {
            die("Error removing registration of check with Consul agent",
                    path, checkId, httpResponse.code(), httpResponse.body());
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

        final String path = rootPath + "/self";
        final HttpResponse httpResponse = httpClient.get(path);
        if (httpResponse.code() != 200) {
            die("Error getting info about this agent",
                    path, httpResponse.code(), httpResponse.body());
        }

        return Boon.fromJson(httpResponse.body(), AgentInfo.class);

    }

    /**
     * Retrieves all checks registered with the Agent.
     *
     * GET /v1/agent/checks
     *
     * @return Map of Check ID to Checks.
     */
    public Map<String, HealthCheck> getChecks() {

        final String path = rootPath + "/checks";
        final HttpResponse httpResponse = httpClient.get(path);
        final JsonParserAndMapper jsonParserAndMapper = new JsonParserFactory().create();
        if (httpResponse.code() == 200) {
            final Map<String, Object> map = jsonParserAndMapper.parseMap(httpResponse.body());
            final Map<String, HealthCheck> returnMap = new HashMap<>(map.size());
            map.entrySet().forEach(entry -> {
                HealthCheck healthCheck = fromMap((Map<String, Object>) entry.getValue(), HealthCheck.class);
                returnMap.put(entry.getKey(), healthCheck);

            });
            return returnMap;
        }
        die("Unable to get health checks", path, httpResponse.code(), httpResponse.body());
        return null;
    }

    /**
     * Retrieves all services registered with the Agent.
     *
     * GET /v1/agent/services
     *
     * @return Map of Service ID to Services.
     */
    public Map<String, Service> getServices() {

        final String path = rootPath + "/services";
        final HttpResponse httpResponse = httpClient.get(path);
        final JsonParserAndMapper jsonParserAndMapper = new JsonParserFactory().create();
        if (httpResponse.code() == 200) {
            final Map<String, Object> map = jsonParserAndMapper.parseMap(httpResponse.body());
            final Map<String, Service> returnMap = new HashMap<>(map.size());
            map.entrySet().forEach(entry -> {
                Service service = fromMap((Map<String, Object>) entry.getValue(), Service.class);
                returnMap.put(entry.getKey(), service);

            });
            return returnMap;
        }

        die("Unable to get list of services", path, httpResponse.code(), httpResponse.body());
        return null;
    }

    /**
     * Retrieves all members that the Agent can see in the gossip pool.
     *
     * GET /v1/agent/members
     *
     * @return List of Members.
     */
    public List<Member> getMembers() {
        final String path = rootPath + "/members";
        final HttpResponse httpResponse = httpClient.get(path);
        if (httpResponse.code() == 200) {
            return Boon.fromJsonArray(httpResponse.body(), Member.class);
        }
        die("Unable to read members", path, httpResponse.code(), httpResponse.body());
        return Collections.emptyList();

    }

    /**
     * GET /v1/agent/force-leave/{node}
     *
     * Instructs the agent to force a node into the "left" state.
     *
     * @param node node
     */
    public void forceLeave(String node) {
        final String path = rootPath + "/force-leave";
        final HttpResponse httpResponse = httpClient.get(path);
        if (httpResponse.code()!=200) {
            die("Unable to force leave", path, httpResponse.code(), httpResponse.body());
        }
    }

    /**
     * Checks in with Consul.
     *
     * @param checkId The Check ID to check in.
     * @param status   The current state of the Check.
     * @param note    Any note to associate with the Check.
     */
    public void check(String checkId, Status status, String note) {
        final String path = rootPath + "/check/" + status.getUri() + "/" + checkId;
        final HttpResponse httpResponse = Str.isEmpty(note) ?
                httpClient.getWith1Param(path, "note", note) :
                httpClient.get(path);
        if (httpResponse.code()!=200) {
            notRegistered("Unable to perform check", path, httpResponse.code(), httpResponse.body());
        }

    }

    /**
     * Prepends the default TTL prefix to the serviceId to produce a check id,
     * then delegates to check(String checkId, State state, String note)
     * This method only works with TTL checks that have not been given a custom
     * name.
     *
     * @param serviceId service id
     * @param status state
     * @param note note
     */
    public void checkTtl(String serviceId, Status status, String note)  {
        check("service:" + serviceId, status, note);
    }

    /**
     * Sets a TTL check to "passing" state
     * @param checkId check id
     */
    public void pass(final String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.PASS, null);
    }

    /**
     * Sets a TTL check to "passing" state with a note
     * @param checkId check id
     * @param note note
     */
    public void pass(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.PASS, note);
    }

    /**
     * Sets a TTL check to "warning" state.
     * @param checkId check id
     */
    public void warn(String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.WARN, null);
    }

    /**
     * Sets a TTL check to "warning" state with a note.
     * @param checkId check id
     * @param note note
     */
    public void warn(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.WARN, note);
    }

    /**
     * Sets a TTL check to "critical" state.
     * @param checkId check id
     */
    public void fail(String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, null);
    }

    /**
     * Sets a TTL check to "critical" state with a note.
     * @param checkId check id
     * @param note note
     */
    public void fail(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, note);
    }

    /**
     * Sets a TTL check to "critical" state.
     * @param checkId check id
     */
    public void critical(String checkId) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, null);
    }

    /**
     * Sets a TTL check to "critical" state with a note.
     * @param checkId check id
     * @param note note
     */
    public void critical(String checkId, String note) throws NotRegisteredException {
        checkTtl(checkId, Status.FAIL, note);
    }

}

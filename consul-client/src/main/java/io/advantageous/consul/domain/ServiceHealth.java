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
package io.advantageous.consul.domain;

import io.advantageous.boon.json.annotations.JsonProperty;

import java.util.List;

public class ServiceHealth {

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Service")
    private Service service;

    @JsonProperty("Checks")
    private List<HealthCheck> checks;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public List<HealthCheck> getChecks() {
        return checks;
    }

    public void setChecks(List<HealthCheck> checks) {
        this.checks = checks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceHealth)) return false;

        ServiceHealth that = (ServiceHealth) o;

        if (checks != null ? !checks.equals(that.checks) : that.checks != null) return false;
        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (service != null ? !service.equals(that.service) : that.service != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (checks != null ? checks.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServiceHealth{" +
                "node=" + node +
                ", service=" + service +
                ", checks=" + checks +
                '}';
    }
}

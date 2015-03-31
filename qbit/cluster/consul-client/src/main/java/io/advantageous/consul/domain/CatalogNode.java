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

import java.util.Map;

/**
 * Holds a catalog node.
 */
public class CatalogNode {

    @JsonProperty("Node")
    private Node node;

    @JsonProperty("Services")
    private Map<String, Service> services;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public void setServices(Map<String, Service> services) {
        this.services = services;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogNode)) return false;

        CatalogNode that = (CatalogNode) o;

        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (services != null ? !services.equals(that.services) : that.services != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (services != null ? services.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CatalogNode{" +
                "node=" + node +
                ", services=" + services +
                '}';
    }
}

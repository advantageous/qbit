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

import java.util.Arrays;
import java.util.List;

/**
 * Holds service information.
 */
public class Service {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Service")
    private String service;

    @JsonProperty("Tags")
    private List<String> tags;

    @JsonProperty("Port")
    private int port;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;

        Service service1 = (Service) o;

        if (port != service1.port) return false;
        if (id != null ? !id.equals(service1.id) : service1.id != null) return false;
        if (service != null ? !service.equals(service1.service) : service1.service != null) return false;
        if (tags != null ? !tags.equals(service1.tags) : service1.tags != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Service{" +
                "id='" + id + '\'' +
                ", service='" + service + '\'' +
                ", tags=" + tags +
                ", port=" + port +
                '}';
    }
}

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

public class CatalogService {

    @JsonProperty("Node")
    private String node;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("ServiceName")
    private String serviceName;

    @JsonProperty("ServiceID")
    private String serviceId;

	@JsonProperty("ServiceAddress")
    private String serviceAddress;

    @JsonProperty("ServicePort")
    private int servicePort;

    @JsonProperty("ServiceTags")
    private List<String> serviceTags;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public List<String> getServiceTags() {
        return serviceTags;
    }

    public void setServiceTags(List<String> serviceTags) {
        this.serviceTags = serviceTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogService)) return false;

        CatalogService that = (CatalogService) o;

        if (servicePort != that.servicePort) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (serviceAddress != null ? !serviceAddress.equals(that.serviceAddress) : that.serviceAddress != null)
            return false;
        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (serviceTags != null ? !serviceTags.equals(that.serviceTags) : that.serviceTags != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        result = 31 * result + (serviceAddress != null ? serviceAddress.hashCode() : 0);
        result = 31 * result + servicePort;
        result = 31 * result + (serviceTags != null ? serviceTags.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CatalogService{" +
                "node='" + node + '\'' +
                ", address='" + address + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", serviceAddress='" + serviceAddress + '\'' +
                ", servicePort=" + servicePort +
                ", serviceTags=" + serviceTags +
                '}';
    }
}

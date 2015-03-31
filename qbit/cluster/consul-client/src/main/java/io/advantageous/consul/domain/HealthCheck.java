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

/**
 * Health check information/status.
 */
public class HealthCheck {

    @JsonProperty("Node")
    private String node;

    @JsonProperty("CheckID")
    private String checkId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Notes")
    private String notes;

    @JsonProperty("Output")
    private String output;

    @JsonProperty("ServiceID")
    private String serviceId;

    @JsonProperty("ServiceName")
    private String serviceName;

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthCheck)) return false;

        HealthCheck that = (HealthCheck) o;

        if (checkId != null ? !checkId.equals(that.checkId) : that.checkId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (node != null ? !node.equals(that.node) : that.node != null) return false;
        if (notes != null ? !notes.equals(that.notes) : that.notes != null) return false;
        if (output != null ? !output.equals(that.output) : that.output != null) return false;
        if (serviceId != null ? !serviceId.equals(that.serviceId) : that.serviceId != null) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (checkId != null ? checkId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (notes != null ? notes.hashCode() : 0);
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + (serviceId != null ? serviceId.hashCode() : 0);
        result = 31 * result + (serviceName != null ? serviceName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HealthCheck{" +
                "node='" + node + '\'' +
                ", checkId='" + checkId + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", output='" + output + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}

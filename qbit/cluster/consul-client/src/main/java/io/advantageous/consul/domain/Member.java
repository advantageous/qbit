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
 * Member of consul cluster.
 */
public class Member {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Port")
    private int port;

    @JsonProperty("Tags")
    private Map<String, String> tags;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("ProtocolMin")
    private int protocolMin;

    @JsonProperty("ProtocolMax")
    private int protocolMax;

    @JsonProperty("ProtocolCur")
    private int protocolCur;

    @JsonProperty("DelegateMin")
    private int delegateMin;

    @JsonProperty("DelegateMax")
    private int delegateMax;

    @JsonProperty("DelegateCur")
    private int delegateCur;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProtocolMin() {
        return protocolMin;
    }

    public void setProtocolMin(int protocolMin) {
        this.protocolMin = protocolMin;
    }

    public int getProtocolMax() {
        return protocolMax;
    }

    public void setProtocolMax(int protocolMax) {
        this.protocolMax = protocolMax;
    }

    public int getProtocolCur() {
        return protocolCur;
    }

    public void setProtocolCur(int protocolCur) {
        this.protocolCur = protocolCur;
    }

    public int getDelegateMin() {
        return delegateMin;
    }

    public void setDelegateMin(int delegateMin) {
        this.delegateMin = delegateMin;
    }

    public int getDelegateMax() {
        return delegateMax;
    }

    public void setDelegateMax(int delegateMax) {
        this.delegateMax = delegateMax;
    }

    public int getDelegateCur() {
        return delegateCur;
    }

    public void setDelegateCur(int delegateCur) {
        this.delegateCur = delegateCur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member)) return false;

        Member member = (Member) o;

        if (delegateCur != member.delegateCur) return false;
        if (delegateMax != member.delegateMax) return false;
        if (delegateMin != member.delegateMin) return false;
        if (port != member.port) return false;
        if (protocolCur != member.protocolCur) return false;
        if (protocolMax != member.protocolMax) return false;
        if (protocolMin != member.protocolMin) return false;
        if (status != member.status) return false;
        if (address != null ? !address.equals(member.address) : member.address != null) return false;
        if (name != null ? !name.equals(member.name) : member.name != null) return false;
        if (tags != null ? !tags.equals(member.tags) : member.tags != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + status;
        result = 31 * result + protocolMin;
        result = 31 * result + protocolMax;
        result = 31 * result + protocolCur;
        result = 31 * result + delegateMin;
        result = 31 * result + delegateMax;
        result = 31 * result + delegateCur;
        return result;
    }

    @Override
    public String toString() {
        return "Member{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", tags=" + tags +
                ", status=" + status +
                ", protocolMin=" + protocolMin +
                ", protocolMax=" + protocolMax +
                ", protocolCur=" + protocolCur +
                ", delegateMin=" + delegateMin +
                ", delegateMax=" + delegateMax +
                ", delegateCur=" + delegateCur +
                '}';
    }
}

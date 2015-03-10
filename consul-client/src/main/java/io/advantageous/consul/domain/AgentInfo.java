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
 * Agent information.
 */
public class AgentInfo {

    @JsonProperty("Config")
    private Config config;

    @JsonProperty("Member")
    private Member member;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                "config=" + config +
                ", member=" + member +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentInfo)) return false;

        AgentInfo agentInfo = (AgentInfo) o;

        if (config != null ? !config.equals(agentInfo.config) : agentInfo.config != null) return false;
        if (member != null ? !member.equals(agentInfo.member) : agentInfo.member != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = config != null ? config.hashCode() : 0;
        result = 31 * result + (member != null ? member.hashCode() : 0);
        return result;
    }
}
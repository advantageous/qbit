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
 * Holds cluster topology information for Consul.
 */
public class Ports {

    @JsonProperty("DNS")
    private int dns;

    @JsonProperty("HTTP")
    private int http;

    @JsonProperty("RPC")
    private int rpc;

    @JsonProperty("SerfLan")
    private int serfLan;

    @JsonProperty("SerfWan")
    private int serfWan;

    @JsonProperty("Server")
    private int server;

    public int getDns() {
        return dns;
    }

    public void setDns(int dns) {
        this.dns = dns;
    }

    public int getHttp() {
        return http;
    }

    public void setHttp(int http) {
        this.http = http;
    }

    public int getRpc() {
        return rpc;
    }

    public void setRpc(int rpc) {
        this.rpc = rpc;
    }

    public int getSerfLan() {
        return serfLan;
    }

    public void setSerfLan(int serfLan) {
        this.serfLan = serfLan;
    }

    public int getSerfWan() {
        return serfWan;
    }

    public void setSerfWan(int serfWan) {
        this.serfWan = serfWan;
    }

    public int getServer() {
        return server;
    }

    public void setServer(int server) {
        this.server = server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ports)) return false;

        Ports ports = (Ports) o;

        if (dns != ports.dns) return false;
        if (http != ports.http) return false;
        if (rpc != ports.rpc) return false;
        if (serfLan != ports.serfLan) return false;
        if (serfWan != ports.serfWan) return false;
        if (server != ports.server) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dns;
        result = 31 * result + http;
        result = 31 * result + rpc;
        result = 31 * result + serfLan;
        result = 31 * result + serfWan;
        result = 31 * result + server;
        return result;
    }

    @Override
    public String toString() {
        return "Ports{" +
                "dns=" + dns +
                ", http=" + http +
                ", rpc=" + rpc +
                ", serfLan=" + serfLan +
                ", serfWan=" + serfWan +
                ", server=" + server +
                '}';
    }
}

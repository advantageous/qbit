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

public class Registration {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Id")
    private String id;

    @JsonProperty("Port")
    private int port;

    @JsonProperty("Check")
    private RegistrationCheck check;

    @JsonProperty("Tags")
    private String[] tags;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public RegistrationCheck getCheck() {
        return check;
    }

    public void setCheck(RegistrationCheck check) {
        this.check = check;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String... tags) {
        this.tags = tags;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Registration)) return false;

        Registration that = (Registration) o;

        if (port != that.port) return false;
        if (check != null ? !check.equals(that.check) : that.check != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (!Arrays.equals(tags, that.tags)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + port;
        result = 31 * result + (check != null ? check.hashCode() : 0);
        result = 31 * result + (tags != null ? Arrays.hashCode(tags) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Registration{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", port=" + port +
                ", check=" + check +
                ", tags=" + Arrays.toString(tags) +
                '}';
    }


}

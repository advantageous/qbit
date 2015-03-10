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
* Created by rhightower on 3/9/15.
*/
public class RegistrationCheck {

    @JsonProperty("Script")
    private String script;

    @JsonProperty("Interval")
    private String interval;

    @JsonProperty("TTL")
    private String ttl;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegistrationCheck)) return false;

        RegistrationCheck that = (RegistrationCheck) o;

        if (interval != null ? !interval.equals(that.interval) : that.interval != null) return false;
        if (script != null ? !script.equals(that.script) : that.script != null) return false;
        if (ttl != null ? !ttl.equals(that.ttl) : that.ttl != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = script != null ? script.hashCode() : 0;
        result = 31 * result + (interval != null ? interval.hashCode() : 0);
        result = 31 * result + (ttl != null ? ttl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RegistrationCheck{" +
                "script='" + script + '\'' +
                ", interval='" + interval + '\'' +
                ", ttl='" + ttl + '\'' +
                '}';
    }
}

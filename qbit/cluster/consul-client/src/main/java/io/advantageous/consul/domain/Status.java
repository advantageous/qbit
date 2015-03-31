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

/**
 * Represents the possible status for a health check.
 */
public enum Status {

    PASS("pass", "passing"),
    WARN("warn", "warning"),
    FAIL("fail", "critical"),
    ANY("any", "any"),
    UNKNOWN("unknown", "unknown");

    private String uri;
    private String name;

    /**
     * Private constructor.
     *
     * @param uri Consul API uri value.
     */
    private Status(String uri, String name) {
        this.uri = uri;
        this.name = name;
    }

    /**
     * The uri uri for the Consul check API endpoints.
     *
     * @return The uri value, e.g. "pass" for PASS.
     */
    public String getUri() {
        return uri;
    }

    /**
     * The name value for the Consul check API endpoints.  This is the value
     * to use for querying services by health state.
     *
     * @return The name, e.g. "passing" for PASS.
     */
    public String getName() {
        return name;
    }
}

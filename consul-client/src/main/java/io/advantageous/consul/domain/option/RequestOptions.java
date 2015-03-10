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
package io.advantageous.consul.domain.option;

/**
 * Request options used by the Consul API.
 */
public class RequestOptions {

    private boolean blocking;
    private String wait;
    private int index;
    private Consistency consistency;

    public static RequestOptions BLANK = new RequestOptions(null, 0, Consistency.DEFAULT);

    /**
     * @param wait Wait string, e.g. "10s" or "10m"
     * @param index Lock index.
     * @param consistency Consistency mode to use for query.
     */
    public RequestOptions(String wait, int index, Consistency consistency) {
        this.wait = wait;
        this.index = index;
        this.consistency = consistency;
        this.blocking = wait != null;
    }

    public String getWait() {
        return wait;
    }

    public int getIndex() {
        return index;
    }

    public Consistency getConsistency() {
        return consistency;
    }

    public boolean isBlocking() {
        return blocking;
    }
}

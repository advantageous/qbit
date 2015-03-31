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
 * Contains index, leader, response, known leader for agent.
 * @param <T> T
 */
public class ConsulResponse<T> {

    private T response;
    private long lastContact;
    private boolean knownLeader;
    private int index;

    public ConsulResponse(T response, long lastContact, boolean knownLeader, int index) {
        this.response = response;
        this.lastContact = lastContact;
        this.knownLeader = knownLeader;
        this.index = index;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }

    public long getLastContact() {
        return lastContact;
    }

    public void setLastContact(long lastContact) {
        this.lastContact = lastContact;
    }

    public boolean isKnownLeader() {
        return knownLeader;
    }

    public void setKnownLeader(boolean knownLeader) {
        this.knownLeader = knownLeader;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}

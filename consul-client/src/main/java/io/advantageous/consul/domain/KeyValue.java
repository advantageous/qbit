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

public class KeyValue {

    @JsonProperty("CreateIndex")
    private long createIndex;

    @JsonProperty("ModifyIndex")
    private long modifyIndex;

    @JsonProperty("LockIndex")
    private long lockIndex;

    @JsonProperty("Key")
    private String key;

    @JsonProperty("Flags")
    private long flags;

    @JsonProperty("Value")
    private String value;

    @JsonProperty("Session")
    private String session;

    public long getCreateIndex() {
        return createIndex;
    }

    public void setCreateIndex(long createIndex) {
        this.createIndex = createIndex;
    }

    public long getModifyIndex() {
        return modifyIndex;
    }

    public void setModifyIndex(long modifyIndex) {
        this.modifyIndex = modifyIndex;
    }

    public long getLockIndex() {
        return lockIndex;
    }

    public void setLockIndex(long lockIndex) {
        this.lockIndex = lockIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValue)) return false;

        KeyValue keyValue = (KeyValue) o;

        if (createIndex != keyValue.createIndex) return false;
        if (flags != keyValue.flags) return false;
        if (lockIndex != keyValue.lockIndex) return false;
        if (modifyIndex != keyValue.modifyIndex) return false;
        if (key != null ? !key.equals(keyValue.key) : keyValue.key != null) return false;
        if (session != null ? !session.equals(keyValue.session) : keyValue.session != null) return false;
        if (value != null ? !value.equals(keyValue.value) : keyValue.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (createIndex ^ (createIndex >>> 32));
        result = 31 * result + (int) (modifyIndex ^ (modifyIndex >>> 32));
        result = 31 * result + (int) (lockIndex ^ (lockIndex >>> 32));
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (int) (flags ^ (flags >>> 32));
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "createIndex=" + createIndex +
                ", modifyIndex=" + modifyIndex +
                ", lockIndex=" + lockIndex +
                ", key='" + key + '\'' +
                ", flags=" + flags +
                ", value='" + value + '\'' +
                ", session='" + session + '\'' +
                '}';
    }
}

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

package io.advantageous.qbit.events.spi;

import io.advantageous.qbit.message.Event;

/**
 *
 * @param <T> T
 */
public class EventTransferObject<T> implements Event<T> {

    private final T body;
    private final long id;
    private final String topic;

    public EventTransferObject(T body, long id, String topic) {
        this.body = body;
        this.id = id;
        this.topic = topic;
    }


    public EventTransferObject() {
        this.body = null;
        this.id = 0L;
        this.topic = "";
    }

    @Override
    public long id() {
        return id;
    }

    @Override
    public T body() {
        return body;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public String toString() {
        return "EventImpl{" +
                "body=" + body +
                ", id=" + id +
                '}';
    }

    @Override
    public String channel() {
        return topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        Event event = (Event) o;

        if (id != event.id()) return false;
        if (topic != null ? !topic.equals(event.channel()) : event.channel() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = body != null ? body.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        return result;
    }
}

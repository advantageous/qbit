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

package io.advantageous.qbit.message;

import io.advantageous.qbit.reactive.Callback;

import java.util.List;

/**
 * This represents an async method call.
 *
 * @param <T> Type
 */
public interface MethodCall<T> extends Request<T> {

    String name();

    long timestamp();

    default String objectName() {
        return "";
    }

    boolean hasCallback();

    Callback<Object> callback();

    default Object[] args() {
        Object body = this.body();

        if (body instanceof Object[]) {
            return (Object[]) body;
        } else if (body instanceof List) {
            List list = ((List) body);
            return list.toArray(new Object[list.size()]);
        } else {
            return new Object[]{body};
        }
    }
}

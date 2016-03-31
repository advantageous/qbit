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

package io.advantageous.qbit.http.request;


import java.nio.charset.StandardCharsets;

/**
 * When you register this with a request, it means that you expect a binary result from the server.
 *
 * @author rhightower on 1/15/15.
 */
public interface HttpBinaryReceiver extends HttpResponseReceiver<byte[]> {

    default boolean isText() {
        return false;
    }


    default void respondOK(String json) {
        respond(200, json);
    }

    default void error(String json) {
        respond(500, json);
    }


    default void respond(int code, String json) {
        response(code, "application/json", json.getBytes(StandardCharsets.UTF_8));
    }
}

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
package io.advantageous.qbit.meta.transformer;

import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.message.MethodCall;

import java.util.List;

/**
 * Converts an HTTP request into a method call.
 */
public interface RequestTransformer {

    default MethodCall<Object> transform(HttpRequest request, List<String> errorsList) {
        
        return transformByPosition(request, errorsList, false);
    }

    MethodCall<Object> transformByPosition(final HttpRequest request,
                                           final List<String> errorsList, boolean byPosition);


    default MethodCall<Object> transFormBridgeBody(Object body, List<String> errors, String address, String method) {
        final String uri = ("/" + address + "/" + method).replace("//", "/");
        final HttpRequest request = HttpRequestBuilder.httpRequestBuilder().setUri(uri).setBody(body == null ? null : body.toString()).setMethod("BRIDGE").build();
        return this.transformByPosition(request, errors, true);
    }

}

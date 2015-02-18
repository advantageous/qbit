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

package io.advantageous.qbit.transforms;

import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Request;

import java.util.List;

/**
 * Transforms a JSON request body to an argument list.
 * <p>
 * Created by Richard on 8/11/14.
 *
 * @author rhightower
 */
public class JsonRequestBodyToArgListTransformer implements Transformer<Request, Object> {

    private final JsonMapper mapper;

    public JsonRequestBodyToArgListTransformer(final JsonMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object transform(final Request request) {
        if (request.body() instanceof List) {
            final List list = (List) request.body();
            return mapper.fromJson(((String) list.get(0)));
        } else if (request.body() instanceof String) {
            return mapper.fromJson(((String) request.body()));
        } else {
            throw new IllegalArgumentException("Unable to handle request");
        }
    }
}

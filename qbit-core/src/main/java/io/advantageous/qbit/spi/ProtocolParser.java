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

package io.advantageous.qbit.spi;

import io.advantageous.qbit.message.Message;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.util.MultiMap;

import java.util.List;

/**
 * This parses the wire format to get method calls.  Could also be called a decoder.
 * <p>
 * Created by Richard on 9/26/14.
 *
 * @author rhightower
 */
public interface ProtocolParser {

    boolean supports(Object object, MultiMap<String, String> params);

    MethodCall<Object> parseMethodCall(Object body);

    MethodCall<Object> parseMethodCallUsingAddressPrefix(String addressPrefix, Object body);

    List<Message<Object>> parse(String address, Object body);

    List<MethodCall<Object>> parseMethods(Object body);


    List<MethodCall<Object>> parseMethodCallListUsingAddressPrefix(String addressPrefix, Object body);

    Response<Object> parseResponse(Object body);
}

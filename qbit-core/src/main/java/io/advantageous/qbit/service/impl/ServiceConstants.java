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

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.transforms.NoOpRequestTransform;

/**
 * Created by Richard on 9/8/14.
 */
public class ServiceConstants {

    public static final Response<Object> VOID = new Response<Object>() {
        @Override
        public boolean wasErrors() {
            return false;
        }

        @Override
        public void body(Object body) {

        }

        @Override
        public String returnAddress() {
            return "";
        }

        @Override
        public String address() {
            return "";
        }

        @Override
        public long timestamp() {
            return 0;
        }

        @Override
        public Request<Object> request() {
            return null;
        }

        @Override
        public long id() {
            return 0;
        }

        @Override
        public Object body() {
            return "VOID";
        }

        @Override
        public boolean isSingleton() {
            return false;
        }
    };


    public static final NoOpBeforeMethodCall NO_OP_BEFORE_METHOD_CALL = new NoOpBeforeMethodCall();
    public static final NoOpAfterMethodCall NO_OP_AFTER_METHOD_CALL = new NoOpAfterMethodCall();

    public static final NoOpRequestTransform NO_OP_ARG_TRANSFORM = new NoOpRequestTransform();
}

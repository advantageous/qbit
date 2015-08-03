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

package io.advantageous.qbit.example.hello;

import io.advantageous.boon.core.Lists;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.reactive.Callback;

import java.util.List;

/**
 * created by rhightower on 2/10/15.
 */

@RequestMapping("/helloservice")
public class HelloService {


    @RequestMapping("/hello")
    public HelloObject hello() {
        return new HelloObject("Hello World!");
    }


    @RequestMapping("/hello2")
    public void hello2(Callback<List<HelloObject>> listCallback) {

        listCallback.accept(Lists.list(new HelloObject("Hello World!"),
                new HelloObject("Hello World!")));
    }


    @RequestMapping(value = "/hello3", method = RequestMethod.POST)
    public void hello3(Callback<List<HelloObject>> listCallback, List<HelloObject> helloObjects) {

        listCallback.accept(helloObjects);
    }

}

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
package io.advantageous.qbit.meta;


import io.advantageous.qbit.annotation.HeaderParam;
import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;

import static io.advantageous.boon.core.Str.sputs;


@RequestMapping("/sample/service")
public class SampleService {


    @RequestMapping("/simple1/")
    public String simple1() {
        return "simple1";
    }


    @RequestMapping("/call1/foo/{arg4}/{2}")
    public String method1(@RequestParam("arg1") final String arg1,
                          @HeaderParam("arg2") final int arg2,
                          @PathVariable final float arg3,
                          @PathVariable("arg4") final double arg4) {


        return sputs(arg1, arg2, arg3, arg4);
    }


    //"/call2/{2}/{arg4}")
    public String method2(final String arg1,
                          final int arg2,
                          final float arg3,
                          final double arg4) {

        return sputs(arg1, arg2, arg3, arg4);
    }


    @RequestMapping("/simple2/path/")
    public String simple2(@RequestParam("arg1") final String arg1) {
        return "simple2";
    }


}

package io.advantageous.qbit.meta.swagger;

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

import io.advantageous.qbit.annotation.*;
import io.advantageous.qbit.reactive.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.advantageous.boon.core.Str.sputs;


@RequestMapping("/sample/service")
public class SampleService {


    @HideMethod
    public String hideMe() {
        return "simple1";
    }

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


    @RequestMapping("/simple2/path/")
    public String simple2(@RequestParam("arg1") final String arg1) {
        return "simple2";
    }


    @RequestMapping("/add/dept/")
    public Department addDepartment(final Department department) {

        return department;

    }


    @RequestMapping("/add/dept/all")
    public List<Department> listDepts() {
        return Collections.emptyList();
    }


    @RequestMapping(value = "/todo", method = RequestMethod.POST)
    public void add(final Callback<Boolean> callback, final Department todo) {
        callback.accept(true);
    }


    @RequestMapping(value = "/todo", method = RequestMethod.GET)
    public void list(final Callback<List<Department>> callback) {
        callback.accept(new ArrayList<>());
    }


    @RequestMapping("/add/dept/2")
    public void addDepartment2(final Department department) {


    }

}

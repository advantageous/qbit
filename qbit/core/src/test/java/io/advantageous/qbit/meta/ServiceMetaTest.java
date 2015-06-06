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


import io.advantageous.boon.core.reflection.ClassMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.boon.core.reflection.ClassMeta.classMeta;
import static io.advantageous.boon.json.JsonFactory.toJson;
import static io.advantageous.qbit.meta.ParameterMeta.*;
import static io.advantageous.qbit.meta.RequestMeta.getRequest;
import static io.advantageous.qbit.meta.ServiceMeta.service;
import static io.advantageous.qbit.meta.ServiceMethodMeta.method;
import static io.advantageous.qbit.meta.params.Param.*;
import static org.junit.Assert.assertEquals;

public class ServiceMetaTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void test() throws Exception {

        final ServiceMeta myService = service("myService", "/myservice");

        puts(toJson(myService));
    }

    @Test
    public void test2() throws Exception {

        final ClassMeta<MyService> classMeta = classMeta(MyService.class);

        final ServiceMeta myService =
                service("myService", "/myservice/{score}",

                        method(
                                "hello",
                                getRequest(
                                        "/callme/",
                                        stringParam(
                                                headParam("foobarHeader")
                                        ),
                                        intParam(
                                                requestParam("helloMom")
                                        ),
                                        doubleParam(
                                                pathParam("score", 9)
                                        )
                                )
                        )
                );

        puts(toJson(myService));
    }

    @Test
    public void test3() throws Exception {

        final ClassMeta<MyService> classMeta = classMeta(MyService.class);

        final ContextMeta context =
                ContextMeta.context("/root/",
                        service("myService", "/myservice",

                                method(
                                        classMeta.method("hello"),
                                        getRequest(
                                                "/callme/",
                                                stringParam(
                                                        headParam("foobarHeader")
                                                ),
                                                intParam(
                                                        requestParam("helloMom")
                                                ),
                                                doubleParam(
                                                        pathParam("score", 9)
                                                )
                                        )
                                )
                        )

                );

        assertEquals("hello", context.getServices().get(0).getMethods().get(0).getName());

        assertEquals("/callme/", context.getServices().get(0).getMethods().get(0).getRequestEndpoints().get(0).getRequestURI());
    }

    public static class MyService {

        public String hello(String arg, int arg2, float arg3) {
            return sputs(arg, arg2, arg3);
        }
    }


}
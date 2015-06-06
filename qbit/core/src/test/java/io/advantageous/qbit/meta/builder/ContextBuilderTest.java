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
package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.SampleService;
import io.advantageous.qbit.meta.params.ParamType;
import io.advantageous.qbit.meta.params.URIPositionalParam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.advantageous.qbit.meta.ParameterMeta.*;
import static io.advantageous.qbit.meta.RequestMeta.getRequest;
import static io.advantageous.qbit.meta.ServiceMeta.service;
import static io.advantageous.qbit.meta.ServiceMethodMeta.method;
import static io.advantageous.qbit.meta.builder.RequestMetaBuilder.requestMetaBuilder;
import static io.advantageous.qbit.meta.builder.ServiceMetaBuilder.serviceMetaBuilder;
import static io.advantageous.qbit.meta.builder.ServiceMethodMetaBuilder.serviceMethodMetaBuilder;
import static io.advantageous.qbit.meta.params.Param.*;
import static org.junit.Assert.assertEquals;

public class ContextBuilderTest {

    ContextMetaBuilder contextMetaBuilder;

    @Before
    public void setUp() throws Exception {
        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();


    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBuild() throws Exception {

        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
        contextMetaBuilder.addService(service("myService", "/myservice",

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
        ));

        contextMetaBuilder.setServices(contextMetaBuilder.getServices());

        ContextMeta context = contextMetaBuilder.build();

        assertEquals("hello", context.getServices().get(0).getMethods().get(0).getName());

        assertEquals("/callme/", context.getServices()
                .get(0).getMethods().get(0).getRequestEndpoints().get(0).getRequestURI());

    }


    @Test
    public void testBuildNestedBuilder() throws Exception {

        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
        contextMetaBuilder.addService(
                serviceMetaBuilder()
                        .setName("myService")
                        .addRequestPath("/myservice")
                        .addMethod(method("hello",
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
                        ))
                        .build());


        contextMetaBuilder.setServices(contextMetaBuilder.getServices());

        ContextMeta context = contextMetaBuilder.build();

        assertEquals("hello", context.getServices().get(0).getMethods().get(0).getName());

        assertEquals("/callme/", context.getServices()
                .get(0).getMethods().get(0).getRequestEndpoints().get(0).getRequestURI());

    }


    @Test
    public void testBuildNestedBuilder2() throws Exception {

        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
        contextMetaBuilder.addService(
                serviceMetaBuilder()
                        .setName("myService")
                        .addRequestPath("/myservice")
                        .addMethod(

                                serviceMethodMetaBuilder().setName("hello")
                                        .addRequestEndpoint(getRequest(
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
                                        )).build()
                        )
                        .build());


        contextMetaBuilder.setServices(contextMetaBuilder.getServices());

        ContextMeta context = contextMetaBuilder.build();

        assertEquals("hello", context.getServices().get(0).getMethods().get(0).getName());

        assertEquals("/callme/", context.getServices()
                .get(0).getMethods().get(0).getRequestEndpoints().get(0).getRequestURI());

    }


    @Test
    public void testBuildNestedBuilder3() throws Exception {

        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
        contextMetaBuilder.addService(
                serviceMetaBuilder()
                        .setName("myService")
                        .addRequestPath("/myservice")
                        .addMethod(

                                serviceMethodMetaBuilder().setName("hello")
                                        .addRequestEndpoint(

                                                requestMetaBuilder().setRequestURI("/callme/").addParameters(
                                                        stringParam(
                                                                headParam("foobarHeader")
                                                        ),
                                                        intParam(
                                                                requestParam("helloMom")
                                                        ),
                                                        doubleParam(
                                                                pathParam("score", 9)
                                                        )
                                                ).build()

                                        ).build()
                        )
                        .build());


        contextMetaBuilder.setServices(contextMetaBuilder.getServices());

        ContextMeta context = contextMetaBuilder.build();

        assertEquals("hello", context.getServices().get(0).getMethods().get(0).getName());

        assertEquals("/callme/", context.getServices()
                .get(0).getMethods().get(0).getRequestEndpoints().get(0).getRequestURI());

    }


    @Test
    public void usingReflection() throws Exception {


        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
        contextMetaBuilder.addService(SampleService.class);


        ContextMeta context = contextMetaBuilder.build();

        assertEquals("/servicesEngine", context.getRootURI());


        assertEquals("sampleservice", context.getServices().get(0).getName());


        assertEquals("/sample/service", context.getServices().get(0).getRequestPaths().get(0));


        assertEquals("simple1", context.getServices().get(0).getMethods().get(0).getName());


        assertEquals("/simple1/", context.getServices().get(0).getMethods()
                .get(0).getRequestEndpoints().get(0).getRequestURI());


        assertEquals("method1", context.getServices().get(0).getMethods().get(1).getName());

        assertEquals("/call1/foo/{arg4}/{2}", context.getServices().get(0).getMethods()
                .get(1).getRequestEndpoints().get(0).getRequestURI());


        final List<ParameterMeta> parameters = context.getServices().get(0).getMethods()
                .get(1).getRequestEndpoints().get(0).getParameters();

        assertEquals(TypeType.STRING, parameters.get(0).getType());
        assertEquals(TypeType.INT, parameters.get(1).getType());
        assertEquals(TypeType.FLOAT, parameters.get(2).getType());
        assertEquals(TypeType.DOUBLE, parameters.get(3).getType());


        assertEquals(ParamType.REQUEST, parameters.get(0).getParam().getParamType());
        assertEquals(ParamType.HEADER, parameters.get(1).getParam().getParamType());
        assertEquals(ParamType.PATH_BY_POSITION, parameters.get(2).getParam().getParamType());
        assertEquals(ParamType.PATH_BY_NAME, parameters.get(3).getParam().getParamType());


        final URIPositionalParam param = (URIPositionalParam) parameters.get(2).getParam();

        assertEquals(2, param.getPosition());

        //       0      1     2        3      4   5     6
        //"/servcies/sample/myservice/call1/foo/{arg4}/{2}"
        assertEquals(7, param.getIndexIntoURI());


        final String[] split = Str.split("/servcies/sample/myservice/call1/foo/{arg4}/foo", '/');


        assertEquals("foo", split[7]);


        final String json = JsonFactory.toJson(context);

        System.out.println(json);

    }
}
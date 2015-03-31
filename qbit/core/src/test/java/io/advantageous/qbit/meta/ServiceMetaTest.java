package io.advantageous.qbit.meta;


import io.advantageous.boon.core.reflection.ClassMeta;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.boon.core.Str.sputs;
import static io.advantageous.boon.core.reflection.ClassMeta.classMeta;
import static io.advantageous.boon.json.JsonFactory.toJson;
import static io.advantageous.qbit.meta.ParameterMeta.doubleParam;
import static io.advantageous.qbit.meta.ParameterMeta.intParam;
import static io.advantageous.qbit.meta.ParameterMeta.stringParam;
import static io.advantageous.qbit.meta.RequestMeta.getRequest;
import static io.advantageous.qbit.meta.ServiceMeta.service;
import static io.advantageous.qbit.meta.ServiceMethodMeta.method;
import static io.advantageous.qbit.meta.params.Param.headParam;
import static io.advantageous.qbit.meta.params.Param.pathParam;
import static io.advantageous.qbit.meta.params.Param.requestParam;
import static org.junit.Assert.assertEquals;

public class ServiceMetaTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }


    public static class MyService {

        public String hello(String arg, int arg2, float arg3) {
            return sputs(arg, arg2, arg3);
        }
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




}
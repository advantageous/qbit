package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.meta.*;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.advantageous.qbit.meta.ParameterMeta.doubleParam;
import static io.advantageous.qbit.meta.ParameterMeta.intParam;
import static io.advantageous.qbit.meta.ParameterMeta.stringParam;
import static io.advantageous.qbit.meta.RequestMeta.getRequest;
import static io.advantageous.qbit.meta.ServiceMeta.service;
import static io.advantageous.qbit.meta.ServiceMethodMeta.method;
import static io.advantageous.qbit.meta.params.Param.headParam;
import static io.advantageous.qbit.meta.params.Param.pathParam;
import static io.advantageous.qbit.meta.params.Param.requestParam;

public class MetaTransformerFromQbitMetaToSwaggerTest {


        MetaTransformerFromQbitMetaToSwagger metaToSwagger;
        ContextMetaBuilder contextMetaBuilder;

        @Before
        public void setUp() throws Exception {
            contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();

            metaToSwagger = new MetaTransformerFromQbitMetaToSwagger();

            contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");



            contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
            contextMetaBuilder.addService(io.advantageous.qbit.meta.SampleService.class);


            final ContextMeta context = contextMetaBuilder.build();

            final ServiceEndpointInfo serviceEndpointInfo = metaToSwagger.serviceEndpointInfo(context);

            final String json = JsonFactory.toJson(serviceEndpointInfo);

            System.out.println(json);

        }

        @After
        public void tearDown() throws Exception {

        }

        @Test
        public void testBuild() throws Exception {



        }




}
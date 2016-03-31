package io.advantageous.qbit.meta.swagger;

import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.qbit.meta.ContextMeta;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class MetaTransformerFromQbitMetaToSwaggerTest {


    MetaTransformerFromQbitMetaToSwagger metaToSwagger;
    ContextMetaBuilder contextMetaBuilder;

    @Before
    public void setUp() throws Exception {
        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();

        contextMetaBuilder.setContactEmail("rick@rick.com");
        contextMetaBuilder.setContactName("Rick Hightower");
        contextMetaBuilder.setContactURL("https://github.com/advantageous/qbit");
        contextMetaBuilder.setHostAddress("localhost:9090");
        contextMetaBuilder.setDescription("Test set of services");
        contextMetaBuilder.setLicenseName("APACHE 2");
        contextMetaBuilder.setLicenseURL("https://github.com/advantageous/qbit/blob/master/License");
        metaToSwagger = new MetaTransformerFromQbitMetaToSwagger();

        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");

        contextMetaBuilder.addService(SampleService.class);


        final ContextMeta context = contextMetaBuilder.build();

        final ServiceEndpointInfo serviceEndpointInfo = metaToSwagger.serviceEndpointInfo(context);

        JsonSerializer jsonSerializer = new JsonSerializerFactory().setUseAnnotations(true).create();

        System.out.println(jsonSerializer.serialize(serviceEndpointInfo));

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBuild() throws Exception {


    }


}
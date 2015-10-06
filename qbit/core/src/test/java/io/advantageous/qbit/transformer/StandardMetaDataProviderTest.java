package io.advantageous.qbit.transformer;

import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.RequestMetaData;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class StandardMetaDataProviderTest {


    ContextMetaBuilder contextMetaBuilder;
    StandardMetaDataProvider provider;

    @Before
    public void setUp() throws Exception {
        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();

        contextMetaBuilder.setRootURI(contextMetaBuilder.getRootURI() + "Engine");
        contextMetaBuilder.addService(
                SampleService.class);

        provider = new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.GET);


    }

    @After
    public void tearDown() throws Exception {

    }


    @Test
    public void test() throws Exception {

        final RequestMetaData metaData = provider.get("/servicesengine/sample/service/method2");
        assertNotNull(metaData);
    }

    @Test
    public void test2() throws Exception {

        final RequestMetaData metaData = provider.get("/servicesengine/boo/baz/nothere");
        assertNull(metaData);
    }

}
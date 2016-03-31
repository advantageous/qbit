package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.core.Maps;
import io.advantageous.boon.core.Predicate;
import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.annotation.http.GET;
import io.advantageous.qbit.annotation.http.NoCacheHeaders;
import io.advantageous.qbit.annotation.http.ResponseHeader;
import io.advantageous.qbit.annotation.http.ResponseHeaders;
import io.advantageous.qbit.http.HttpHeaders;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.ServiceMeta;
import io.advantageous.qbit.meta.ServiceMethodMeta;
import io.advantageous.qbit.meta.params.BodyParam;
import io.advantageous.qbit.util.MultiMap;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServiceMetaBuilderTest {

    @Test
    public void shouldReturnBodyParamForParamWithoutQBitAnnotations() {
        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("foo", Lists.list(classMeta.methods()));

        ServiceMethodMeta serviceMethodMeta = Lists.filterBy(serviceMetaBuilder.getMethods(), new Predicate<ServiceMethodMeta>() {
            @Override
            public boolean test(ServiceMethodMeta serviceMethodMeta) {
                return "otherAnnotation".equals(serviceMethodMeta.getName());
            }
        }).iterator().next();

        assertTrue(serviceMethodMeta.getRequestEndpoints().iterator().hasNext());
        assertTrue(serviceMethodMeta.getRequestEndpoints().iterator().next()
                .getParameters().iterator().hasNext());
        assertTrue(serviceMethodMeta.getRequestEndpoints().iterator().next()
                .getParameters().iterator().next()
                .getParam() instanceof BodyParam);
    }

    @Test
    public void test() {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("foo", Lists.list(classMeta.methods()));

        final Map<String, ServiceMethodMeta> serviceMethodMetaMap = Maps.toMap("name", serviceMetaBuilder.getMethods());

        final ServiceMethodMeta serviceMethodMeta = serviceMethodMetaMap.get("foo");


        assertEquals(int.class, serviceMethodMeta.getReturnType());

        final RequestMeta requestMeta = serviceMethodMeta.getRequestEndpoints().get(0);

        assertEquals("/foo", requestMeta.getRequestURI());

    }

    @Test
    public void testRequestHeader() {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("/foo", Lists.list(classMeta.methods()));


        final Map<String, ServiceMethodMeta> serviceMethodMetaMap = Maps.toMap("name", serviceMetaBuilder.getMethods());

        final ServiceMethodMeta serviceMethodMeta = serviceMethodMetaMap.get("methodWithHeaders");

        final RequestMeta requestMeta = serviceMethodMeta.getRequestEndpoints().get(0);

        assertEquals("/headers", requestMeta.getRequestURI());

        assertTrue(requestMeta.hasResponseHeaders());
        MultiMap<String, String> headers = requestMeta.getResponseHeaders();


        assertEquals(1, headers.size());


        assertEquals("BAR", headers.getFirst("FOO"));

    }

    @Test
    public void testManyRequestHeaders() {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("/foo", Lists.list(classMeta.methods()));

        final Map<String, ServiceMethodMeta> serviceMethodMetaMap = Maps.toMap("name", serviceMetaBuilder.getMethods());

        final ServiceMethodMeta serviceMethodMeta = serviceMethodMetaMap.get("methodWithManyHeaders");


        final RequestMeta requestMeta = serviceMethodMeta.getRequestEndpoints().get(0);

        assertEquals("/manyheaders", requestMeta.getRequestURI());

        assertTrue(requestMeta.hasResponseHeaders());
        MultiMap<String, String> headers = requestMeta.getResponseHeaders();


        assertEquals(2, headers.size());


        assertEquals("BAR", headers.getFirst("FOO"));

        assertEquals("BAZ", headers.getFirst("BAR"));

    }

    @Test
    public void testNoCache() {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("/foo", Lists.list(classMeta.methods()));

        final Map<String, ServiceMethodMeta> serviceMethodMetaMap = Maps.toMap("name", serviceMetaBuilder.getMethods());

        final ServiceMethodMeta serviceMethodMeta = serviceMethodMetaMap.get("noCache");


        final RequestMeta requestMeta = serviceMethodMeta.getRequestEndpoints().get(0);

        assertEquals("/nocache", requestMeta.getRequestURI());

        assertTrue(requestMeta.hasResponseHeaders());
        MultiMap<String, String> headers = requestMeta.getResponseHeaders();


        assertEquals(1, headers.size());

        final List<String> controls = (List<String>) headers.getAll(HttpHeaders.CACHE_CONTROL);

        assertEquals("max-age=0", controls.get(0));

        assertEquals("no-cache, no-store", controls.get(1));

    }

    @Test
    public void testNoCacheWithGet() {


        final ClassMeta<?> classMeta = ClassMeta.classMeta(Foo.class);

        ServiceMetaBuilder serviceMetaBuilder = new ServiceMetaBuilder().setRequestPaths(Lists.list("foo"));

        serviceMetaBuilder.addMethods("/foo", Lists.list(classMeta.methods()));

        final Map<String, ServiceMethodMeta> serviceMethodMetaMap = Maps.toMap("name", serviceMetaBuilder.getMethods());

        final ServiceMethodMeta serviceMethodMeta = serviceMethodMetaMap.get("getNoCache");


        final RequestMeta requestMeta = serviceMethodMeta.getRequestEndpoints().get(0);

        assertEquals("/nocache", requestMeta.getRequestURI());

        assertTrue(requestMeta.hasResponseHeaders());
        MultiMap<String, String> headers = requestMeta.getResponseHeaders();


        assertEquals(1, headers.size());

        final List<String> controls = (List<String>) headers.getAll(HttpHeaders.CACHE_CONTROL);

        assertEquals("max-age=0", controls.get(0));

        assertEquals("no-cache, no-store", controls.get(1));

    }

    @Test
    public void testRequestHeaderWithParentHeaders() {


        ContextMetaBuilder contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();
        contextMetaBuilder.addService(Foo1.class);


        final ServiceMeta serviceMeta = contextMetaBuilder.getServices().get(0);

        final Map<String, ServiceMethodMeta> serviceMethodMetaMap = Maps.toMap("name", serviceMeta.getMethods());

        final ServiceMethodMeta serviceMethodMeta = serviceMethodMetaMap.get("methodWithHeaders");

        final RequestMeta requestMeta = serviceMethodMeta.getRequestEndpoints().get(0);

        assertEquals("/headers", requestMeta.getRequestURI());

        assertTrue(requestMeta.hasResponseHeaders());
        MultiMap<String, String> headers = requestMeta.getResponseHeaders();


        assertEquals(5, headers.size());


        assertEquals("BAR", headers.getFirst("FOO"));
        assertEquals("1.BAR", headers.getFirst("1.FOO"));
        assertEquals("2.BAR2", headers.getFirst("2.FOO2"));


        final List<String> controls = (List<String>) headers.getAll(HttpHeaders.CACHE_CONTROL);

        assertEquals("max-age=0", controls.get(0));

        assertEquals("no-cache, no-store", controls.get(1));


    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target(value = {ElementType.PARAMETER})
    public @interface Other {
        String name();
    }

    public static class Foo {


        @RequestMapping("/foo")
        public int foo() {
            return 0;
        }

        @RequestMapping("/headers")
        @ResponseHeader(name = "FOO", value = "BAR")
        public int methodWithHeaders() {
            return 0;
        }

        @RequestMapping("/manyheaders")
        @ResponseHeaders({@ResponseHeader(name = "FOO", value = "BAR"),
                @ResponseHeader(name = "BAR", value = "BAZ")})
        public int methodWithManyHeaders() {
            return 0;
        }


        @RequestMapping("/nocache")
        @NoCacheHeaders
        public int noCache() {
            return 0;
        }

        @GET(value = "/nocache", noCache = true)
        public int getNoCache() {
            return 0;
        }

        @RequestMapping(value = "/otherAnnotation", method = RequestMethod.POST)
        public void otherAnnotation(@Other(name = "bbb") String body) {

        }
    }

    @ResponseHeaders({@ResponseHeader(name = "1.FOO", value = "1.BAR"),
            @ResponseHeader(name = "1.BAR", value = "1.BAZ")})
    @ResponseHeader(name = "2.FOO2", value = "2.BAR2")
    @NoCacheHeaders
    public static class Foo1 {


        @RequestMapping("/headers")
        @ResponseHeader(name = "FOO", value = "BAR")
        public int methodWithHeaders() {
            return 0;
        }


    }


}
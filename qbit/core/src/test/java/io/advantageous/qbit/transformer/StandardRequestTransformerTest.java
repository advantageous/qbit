package io.advantageous.qbit.transformer;

import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.http.request.HttpRequest;
import io.advantageous.qbit.http.request.HttpRequestBuilder;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import io.advantageous.qbit.meta.transformer.StandardRequestTransformer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.boon.core.Maps.safeMap;
import static io.advantageous.boon.json.JsonFactory.toJson;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class StandardRequestTransformerTest {

    StandardRequestTransformer standardRequestTransformer;


    ContextMetaBuilder contextMetaBuilder;
    StandardMetaDataProvider provider;

    @Before
    public void setUp() throws Exception {


        contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();

        contextMetaBuilder.addService(SampleService.class);

        provider = new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.GET);

        StandardMetaDataProvider postProvider = new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.POST);


        standardRequestTransformer = new StandardRequestTransformer(
                safeMap(RequestMethod.GET, provider, RequestMethod.POST, postProvider), Optional.empty()
        );
    }


    @Test
    public void testTransform() throws Exception {

        /*

                /services


        @RequestMapping("/sample/service")
        public class SampleService {


            @RequestMapping("/simple2/path/")
            public String simple2(@RequestParam("arg1") final String arg1) {
                return "simple2";
            }
         */

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.addParam("arg1", "" + 1);
        requestBuilder.setUri("/services/sample/service/simple2/path/");
        final HttpRequest request = requestBuilder.build();

        List<String> errorsList = new ArrayList<>();

        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorsList);

        assertEquals("simple2", methodCall.name());


        @SuppressWarnings("unchecked") List<Object> args = (List<Object>) methodCall.body();

        assertEquals("1", args.get(0));
    }

    @Test
    public void testTransformBadPathParamIndex() throws Exception {

        /*

                /services


        @RequestMapping("/sample/service")
        public class SampleService {


            @RequestMapping("/simpleBadConfig1/{0}")
            public void simpleBadConfig1(Callback<String> callback, @PathVariable(defaultValue = "missing" final String arg1) {
                callback.accept("simple3");
            }
         */

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("/services/sample/service/simpleBadConfig1/someValue");
        final HttpRequest request = requestBuilder.build();

        List<String> errorsList = new ArrayList<>();

        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorsList);

        @SuppressWarnings("unchecked") List<Object> args = (List<Object>) methodCall.body();

        assertEquals("missing", args.get(0));
    }

    @Test
    public void testTransformComplex() throws Exception {

        /*

        /services


        @RequestMapping("/sample/service")
        public class SampleService {

            @RequestMapping("/call1/foo/{arg4}/{2}")
            public String method1(@RequestParam("arg1") final String arg1,
                                  @HeaderParam("arg2") final int arg2,
                                  @PathVariable final float arg3,
                                  @PathVariable("arg4") final double arg4) {
         */

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.addHeader("arg2", "" + 2);
        requestBuilder.addParam("arg1", "" + 1);
        requestBuilder.setUri("/services/sample/service/call1/foo/1.1/2.2");
        final HttpRequest request = requestBuilder.build();


        List<String> errorsList = new ArrayList<>();

        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorsList);
        assertNotNull(methodCall);
        assertEquals(0, errorsList.size());
        @SuppressWarnings("unchecked") List<Object> args = (List<Object>) methodCall.body();
        assertEquals(4, args.size());
        assertEquals("1", args.get(0));
        assertEquals("2", args.get(1));
        assertEquals("2.2", args.get(2));
        assertEquals("1.1", args.get(3));


    }


    @Test
    public void testTransformComplexWithBody() throws Exception {

        /*

            //services

            @RequestMapping("/sample/service")
            public class SampleService {

            @RequestMapping(value = "/method3/", method= RequestMethod.POST)
            public String method3(@RequestParam("arg1") final String arg1,
                          @HeaderParam("arg2") final int arg2,
                          Employee employee) {

         */

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.addHeader("arg2", "" + 2);
        requestBuilder.addParam("arg1", "" + 1);
        requestBuilder.setUri("/services/sample/service/method3/");
        requestBuilder.setBody(toJson(new Employee("Rick", "Hightower")));
        requestBuilder.setMethod("POST");
        final HttpRequest request = requestBuilder.build();


        List<String> errorsList = new ArrayList<>();

        final MethodCall<Object> methodCall = standardRequestTransformer.transform(request, errorsList);
        assertNotNull(methodCall);
        assertEquals(0, errorsList.size());
        @SuppressWarnings("unchecked") List<Object> args = (List<Object>) methodCall.body();
        assertEquals(3, args.size());
        assertEquals("1", args.get(0));
        assertEquals("2", args.get(1));

        puts(args.get(2));

    }
}
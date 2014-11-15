package io.advantageous.qbit.http;

import org.junit.Test;

import static org.boon.Exceptions.die;

public class HttpRequestBuilderTest {

    boolean ok;

    @Test
    public void testGetWithParams() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.addParam("user", "rick");
        requestBuilder.addParam("password", "duck soup");


        final HttpRequest request = requestBuilder.build();



        ok = "foo/bar/baz?password=duck+soup&user=rick".equals(request.getUri())
                || die();


        ok = "GET".equals(request.getMethod())
                || die();


        ok = request.getBody().length == 0 || die();


    }



    @Test
    public void testFormPost() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.addParam("user", "rick");
        requestBuilder.addParam("password", "duck soup");
        requestBuilder.setMethod("POST");


        final HttpRequest request = requestBuilder.build();



        ok = "password=duck+soup&user=rick".equals(request.getBodyAsString())
                || die(request.getBodyAsString());


        ok = "POST".equals(request.getMethod())
                || die();



        ok = "foo/bar/baz".equals(request.getUri())
                || die();


    }




    @Test
    public void testFormJsonPost() throws Exception {

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        requestBuilder.setUri("foo/bar/baz");
        requestBuilder.setJsonBodyForPost("\"hi\"");

        final HttpRequest request = requestBuilder.build();



        ok = "\"hi\"".equals(request.getBodyAsString())
                || die(request.getBodyAsString());


        ok = "POST".equals(request.getMethod())
                || die();



        ok = "foo/bar/baz".equals(request.getUri())
                || die();


    }
}
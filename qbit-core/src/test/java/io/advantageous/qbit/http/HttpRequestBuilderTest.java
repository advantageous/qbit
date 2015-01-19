package io.advantageous.qbit.http;

import org.junit.Test;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

public class HttpRequestBuilderTest {

    boolean ok;

    @Test
    public void test() throws Exception {

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

        requestBuilder.setId(9);
        requestBuilder.setTimestamp(10);
        requestBuilder.setRemoteAddress("remote");

        final HttpRequest request1 = requestBuilder.build();


        final HttpRequest request2 = requestBuilder.build();



        ok = request1.hashCode() == request2.hashCode() || die();
        ok = request1.equals(request2) || die();
        ok = request.getBody().equals(request1.getBody()) || die();
        ok = request.getBodyAsString().equals(request1.getBodyAsString()) || die();
        ok = request.getContentType().equals(request1.getContentType()) || die();
        ok = request.getMethod().equals(request1.getMethod()) || die();
        ok = request2.getMessageId() == request1.getMessageId() || die();
        ok = request2.getRemoteAddress().equals(request1.getRemoteAddress()) || die();

        puts(request);

        request.isJson();

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
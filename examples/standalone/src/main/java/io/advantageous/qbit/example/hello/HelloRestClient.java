package io.advantageous.qbit.example.hello;

import io.advantageous.boon.core.Lists;
import io.advantageous.boon.json.JsonFactory;
import io.advantageous.qbit.http.HTTP;

public class HelloRestClient {

    public static void main(String... args) {
        System.out.println(HTTP.postJSON("http://localhost:9999/services/helloservice/hello3",
                JsonFactory.toJson(Lists.list(new HelloObject("hi"), new HelloObject("mom")))));
    }

}

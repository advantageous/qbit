package io.advantageous.qbit.example.vertx;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;

@RequestMapping("/hello")
public class HelloWorldService {


    @RequestMapping(value = "/world", method = RequestMethod.POST)
    public String hello(String body) {
            return "hello " + body;
    }


    @RequestMapping(value = "/hi/", method = RequestMethod.GET)
    public String hi() {
        return "hi ";
    }

}

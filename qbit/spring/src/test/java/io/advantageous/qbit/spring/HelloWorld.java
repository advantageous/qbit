package io.advantageous.qbit.spring;


import io.advantageous.qbit.annotation.RequestMapping;

@RequestMapping("/hw")
public class HelloWorld {


    @RequestMapping("/hello/")
    public String hello() {
        return "hello";
    }
}

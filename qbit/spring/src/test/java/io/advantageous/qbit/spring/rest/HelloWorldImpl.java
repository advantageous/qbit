package io.advantageous.qbit.spring.rest;


import io.advantageous.qbit.annotation.RequestMapping;

@RequestMapping("/hw")
public class HelloWorldImpl {


    @RequestMapping("/hello/")
    public String hello() {
        System.out.println("HELLO!");
        return "hello";
    }
}

package io.advantageous.qbit.example.hello;

import io.advantageous.qbit.annotation.RequestMapping;

/**
* Created by rhightower on 2/10/15.
*/

@RequestMapping("/helloservice")
public class HelloService {


    @RequestMapping("/hello")
    public HelloObject hello() {
        return new HelloObject("Hello World!");
    }

}

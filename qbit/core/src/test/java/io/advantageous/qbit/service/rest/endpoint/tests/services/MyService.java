package io.advantageous.qbit.service.rest.endpoint.tests.services;


import io.advantageous.qbit.annotation.RequestMapping;

@RequestMapping("/")
public class MyService {


    @RequestMapping("/ping")
    public boolean ping() {
        return true;
    }
}

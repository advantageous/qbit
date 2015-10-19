package io.advantageous.qbit.spring.rest;

import io.advantageous.qbit.spring.annotation.QBitService;
import io.advantageous.qbit.spring.annotation.QBitSpringApplication;
import org.springframework.context.annotation.Bean;

@QBitSpringApplication
public class SpringConfig {

    @Bean
    @QBitService(exposeRemoteEndpoint = true)
    public HelloWorldImpl helloWorld() {
        return new HelloWorldImpl();
    }



}

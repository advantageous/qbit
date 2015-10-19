package io.advantageous.qbit.spring;

import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.spring.annotation.QBitService;
import io.advantageous.qbit.spring.annotation.QBitSpringApplication;
import org.springframework.context.annotation.Bean;

@QBitSpringApplication
public class SpringConfig {

    @Bean
    @QBitService(exposeRemoteEndpoint = true)
    public HelloWorld helloWorld() {
        return new HelloWorld();
    }
}

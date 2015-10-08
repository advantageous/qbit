package io.advantageous.qbit.example.spring.remote;

import io.advantageous.qbit.example.spring.common.RandomNumberService;
import io.advantageous.qbit.example.spring.common.RandomNumberServiceAsync;
import io.advantageous.qbit.example.spring.common.RandomNumberServiceImpl;
import io.advantageous.qbit.spring.annotation.EnableQBit;
import io.advantageous.qbit.spring.annotation.EnableQBitServiceEndpointServer;
import io.advantageous.qbit.spring.annotation.QBitService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@EnableQBit
@EnableQBitServiceEndpointServer
@Configuration
@EnableAutoConfiguration
public class RandomNumberServer {

    public static void main(String[] args) {
        SpringApplication.run(RandomNumberServer.class);
    }

    @Bean
    @QBitService(asyncInterface = RandomNumberServiceAsync.class, exposeRemoteEndpoint = true)
    public RandomNumberService randomNumberService() {
        return new RandomNumberServiceImpl();
    }

}

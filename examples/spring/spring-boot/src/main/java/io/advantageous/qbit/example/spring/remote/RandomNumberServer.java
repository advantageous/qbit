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

/**
 * This is a QBit service running on an HTTP server.  Start this service, then run the client.  The
 * {@link EnableQBitServiceEndpointServer @EnableQBitServiceEndpointServer} annotation tells QBit to start a server for
 * remote connections. The port and path come from the default settings in
 * {@link io.advantageous.qbit.spring.properties.ServiceEndpointServerProperties ServiceEndpointServerProperties}.
 * These defaults can be overridden by creating an application.properties or application.yml in the classpath root.
 *
 * @author geoffc@gmail.com (Geoff Chandler)
 */
@EnableQBit
@EnableQBitServiceEndpointServer
@Configuration
@EnableAutoConfiguration
public class RandomNumberServer {

    public static void main(String[] args) {
        SpringApplication.run(RandomNumberServer.class);
    }

    /**
     * This is the QBit service bean that we will inject into our other class.  We annotate it with
     * {@link QBitService @QbitService} so QBit knows to create a proxy for it.  QBit will create a proxy with the
     * interface specified by the asyncInterface annotation metadata.  In this example we also indicate that we want to
     * expose a remote endpoint.  This flag only works when
     * {@link EnableQBitServiceEndpointServer EnableQBitServiceEndpointServer} is activated and will expose this as an
     * endpoint on the server that has been created.
     *
     * @return the service bean definition.
     */
    @Bean
    @QBitService(asyncInterface = RandomNumberServiceAsync.class, exposeRemoteEndpoint = true)
    public RandomNumberService randomNumberService() {
        return new RandomNumberServiceImpl();
    }

}

package io.advantageous.qbit.spring;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.spring.properties.ConsulProperties;
import io.advantageous.qbit.spring.properties.EventBusProperties;
import io.advantageous.qbit.spring.properties.ServiceDiscoveryProperties;
import io.advantageous.qbit.spring.properties.ServiceEndpointServerProperties;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;

import static org.junit.Assert.*;

import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SpringConfig.class})
@EnableConfigurationProperties({ServiceEndpointServerProperties.class})
@Ignore //added to make travis happy
public class ServiceQueueInitializerTest {


    @Autowired
    ServiceEndpointServerProperties serviceEndpointServerProperties;

    @Test
    public void test() {
        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setPort(serviceEndpointServerProperties.getPort()).buildAndStart();

        final HttpTextResponse httpTextResponse = httpClient.get("/services/hw/hello/");

        System.out.println(httpTextResponse);
    }
}
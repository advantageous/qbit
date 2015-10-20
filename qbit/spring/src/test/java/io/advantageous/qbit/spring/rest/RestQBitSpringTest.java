package io.advantageous.qbit.spring.rest;

import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.spring.properties.ServiceEndpointServerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {SpringConfig.class})
@EnableConfigurationProperties({ServiceEndpointServerProperties.class})
public class RestQBitSpringTest {

    @Autowired
    ServiceEndpointServerProperties serviceEndpointServerProperties;

    @Test
    public void test() {
        final HttpClient httpClient = HttpClientBuilder.httpClientBuilder()
                .setPort(serviceEndpointServerProperties.getPort()).buildAndStart();
        final HttpTextResponse httpTextResponse = httpClient.get("/services/hw/hello/");
        assertEquals("\"hello\"", httpTextResponse.body());
        assertEquals(200, httpTextResponse.code());
        assertEquals("application/json", httpTextResponse.contentType());
    }
}
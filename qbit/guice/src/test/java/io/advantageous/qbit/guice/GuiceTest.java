package io.advantageous.qbit.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.test.TimedTesting;
import org.junit.Test;

import static io.advantageous.qbit.server.EndpointServerBuilder.endpointServerBuilder;
import static org.junit.Assert.assertEquals;

/**
 * Created by marat on 12/30/15
 */
public class GuiceTest extends TimedTesting {

    @Test
    public void shouldPass() {
        UserEndpoint userEndpoint = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new AuthorizedGuiceModule());
            }
        }).getInstance(UserEndpoint.class);

        endpointServerBuilder()
                .setUri("")
                .setHost("localhost")
                .setPort(9090)
                .addServices(userEndpoint)
                .build()
                .start();

        final HttpClient client = HttpClientBuilder.httpClientBuilder().setHost("localhost").setPort(9090).buildAndStart();
        final HttpTextResponse response = client.getWith2Params("/user/register", "email", "email", "password", "password");
        System.out.println(response.body());
        assertEquals(200, response.code());

    }
}

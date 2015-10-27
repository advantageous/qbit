package io.advantageous.qbit.example.websocket.service;


import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.service.ServiceProxyUtils;

public class EchoClientMainWithServiceDiscovery {

    public static void main(final String... args) throws Exception {

        final ClientBuilder clientBuilder = ClientBuilder.clientBuilder();
        final Client client = clientBuilder.setServiceDiscovery(new ServiceDiscoveryMock(), "echo")
                .setUri("/echo").build().startClient();

        final EchoAsync echoClient = client.createProxy(EchoAsync.class, "echo");


        for (int index = 0; index < 100; index++) {
            echoClient.echo(s -> System.out.println(s), "index" + index);

        }

        ServiceProxyUtils.flushServiceProxy(echoClient);

    }
}

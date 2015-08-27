package io.advantageous.qbit.admin;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.http.request.HttpTextResponse;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.health.HealthServiceAsync;

import java.util.concurrent.TimeUnit;

import static io.advantageous.boon.core.IO.puts;

public class RunAdmin {

    public static void main(String... args) throws Exception {


        final AdminBuilder adminBuilder = AdminBuilder.adminBuilder();

        final ServiceEndpointServer adminServer =
                adminBuilder.build();

        adminServer.startServer();

        final HealthServiceAsync healthService = adminBuilder.getHealthService();
        healthService.register("foo", 1, TimeUnit.DAYS);
        healthService.checkInOk("foo");
        healthService.register("bar", 1, TimeUnit.DAYS);
        healthService.checkInOk("bar");

        healthService.clientProxyFlush();

        Sys.sleep(100);
        final HttpClient client = HttpClientBuilder.httpClientBuilder()
                .setPort(adminBuilder.getPort()).buildAndStart();

        final HttpTextResponse httpResponse = client.get("/services/qbit-admin/ok");

        puts(httpResponse.body());

        final HttpTextResponse httpResponsePage = client.get("/qbit/admin.html");

        puts(httpResponsePage.body());

    }
}

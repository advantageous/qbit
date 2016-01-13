package io.advantageous.qbit.sample.server;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.sample.server.client.TodoServiceClient;

import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.boon.core.IO.puts;

/**
 * Created by rhightower on 6/24/15.
 */
public class Five12Main {


    public static void main(String... args) {


        String host = "localhost";
        int port = 8080;
        if (args.length > 1) {

            host = args[1];
        }


        if (args.length > 2) {

            port = Integer.parseInt(args[2]);
        }


        final Client client = new ClientBuilder()
                .setPort(port)
                .setHost(host)
                .setAutoFlush(true)
                .setFlushInterval(100)
                .setProtocolBatchSize(2_000)
                .build();

        TodoServiceClient todoService = client.createProxy(TodoServiceClient.class, "todo-manager");

        client.start();

        AtomicInteger atomicInteger = new AtomicInteger();
        for (int index = 0; index < 512; index++) {

            todoService.size(size -> {
                atomicInteger.incrementAndGet();
            });
        }


        for (int index = 0; index < 100; index++) {
            Sys.sleep(10);
            if (atomicInteger.get() >= 512) {
                break;
            }

            puts(atomicInteger);

        }


        puts(atomicInteger);


    }

}
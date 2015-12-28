package io.advantageous.qbit.example.perf.websocket;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.client.ClientBuilder.clientBuilder;

public class TradeServiceLoadTestWebSocket {


    public static void main(final String... args) {

        final int numClients = 3;
        final int numCalls = 50_000_000;
        final List<Thread> threadList = new ArrayList<>(numClients);

        final List<AtomicInteger> counts = new ArrayList<>();


        for (int c =0; c < numClients; c++) {
            final AtomicInteger count = new AtomicInteger();
            counts.add(count);
            threadList.add(new Thread(() -> {
                runCalls(numCalls, count);
            }));
        }

        threadList.forEach(Thread::start);
        long startTime = System.currentTimeMillis();

        for (int index =0; index<1000; index++) {
            Sys.sleep(1000);

            long totalCount = 0L;

            for (int c = 0; c < counts.size(); c++) {
                totalCount += counts.get(c).get();
            }

            puts(Str.num(totalCount),  Str.num(System.currentTimeMillis()-startTime),
                    Str.num(totalCount/(System.currentTimeMillis()-startTime)*1000));
        }

    }

    private static void runCalls(final int numCalls, final AtomicInteger count) {
        final Client client = clientBuilder().setAutoFlush(false).setProtocolBatchSize(100).build();

        final TradeServiceAsync tradeService = client.createProxy(TradeServiceAsync.class, "tradeservice");

        client.startClient();

        for (int call=0; call < numCalls; call++) {
            tradeService.trade(response -> {
                if (response) {
                    count.incrementAndGet();
                }
            }, new Trade("IBM", 1));

            while (call - 5_000 > count.get()) {
                Sys.sleep(10);
            }
        }

        flushServiceProxy(tradeService);
    }

}

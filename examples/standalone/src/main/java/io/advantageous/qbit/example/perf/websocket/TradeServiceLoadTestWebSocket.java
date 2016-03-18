package io.advantageous.qbit.example.perf.websocket;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.client.ClientBuilder.clientBuilder;
import static io.advantageous.qbit.service.ServiceProxyUtils.flushServiceProxy;

public class TradeServiceLoadTestWebSocket {


    public static void main(final String... args) {

        /** Hold the number of clients we will run. */
        final int numClients = 3;

        /** Hold the number of calls each thread will make. */
        final int numCalls = 50_000_000;

        /** Hold the client threads to run. */
        final List<Thread> threadList = new ArrayList<>(numClients);

        /** Hold the counts to total. */
        final List<AtomicInteger> counts = new ArrayList<>();


        /** Create the client threads. */
        for (int c =0; c < numClients; c++) {
            final AtomicInteger count = new AtomicInteger();
            counts.add(count);
            threadList.add(new Thread(() -> {
                runCalls(numCalls, count);
            }));
        }

        /** Start the threads. */
        threadList.forEach(Thread::start);

        /** Grab the start time. */
        long startTime = System.currentTimeMillis();

        for (int index =0; index<1000; index++) {
            Sys.sleep(1000);

            long totalCount = 0L;

            for (int c = 0; c < counts.size(); c++) {
                totalCount += counts.get(c).get();
            }

            puts("total", Str.num(totalCount),
                    "\telapsed time", Str.num(System.currentTimeMillis()-startTime),
                    "\trate", Str.num(totalCount/(System.currentTimeMillis()-startTime)*1000));
        }

    }

    /** Each client will run this
     *
     * @param numCalls number of times to make calls
     * @param count holds the total count
     */
    private static void runCalls(final int numCalls, final AtomicInteger count) {
        final Client client = clientBuilder().setUri("/")
                //.setHost("192.168.0.1")
                .setAutoFlush(false).build();

        final TradeServiceAsync tradeService = client.createProxy(TradeServiceAsync.class, "t");

        client.startClient();

        for (int call=0; call < numCalls; call++) {
            tradeService.t(response -> {
                if (response) {
                    count.incrementAndGet();
                }
            }, new Trade("IBM", 1));

            /** Apply some back pressure. */
            if (call % 10 == 0) {
                while (call - 5_000 > count.get()) {
                    Sys.sleep(10);
                }
            }
        }

        flushServiceProxy(tradeService);
    }

}

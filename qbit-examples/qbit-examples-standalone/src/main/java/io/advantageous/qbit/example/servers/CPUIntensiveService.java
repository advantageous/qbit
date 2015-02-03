package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import org.boon.core.Sys;

import java.util.Collections;
import java.util.List;

@RequestMapping("/myservice")
public class CPUIntensiveService {


    ActualService actualService = new ActualService();

    @RequestMapping("/ping")
    public List ping() {
        return Collections.singletonList("Hello World!");
    }

    @RequestMapping("/addkey/" )
    public double addKey(@RequestParam("key") int key, @RequestParam("value") String value) {

        return actualService.addKey(key, value);
    }

    public static void main(String... args) throws Exception {



        final ServiceServer serviceServer = new ServiceServerBuilder().setManageQueues(true)
                .setQueueBuilder(QueueBuilder.queueBuilder().setLinkTransferQueue()
                        .setBatchSize(10).setArrayBlockingQueue().setSize(1_000_000))
                .setPort(6060).setFlushInterval(50)
                .build();

        serviceServer.initServices(new CPUIntensiveService());
        serviceServer.start();


        while (true) Sys.sleep(100_000_000);
    }


}

package hello;


import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import org.boon.core.Sys;

import java.util.Collections;
import java.util.List;

import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;

/**
 * Created by rhightower on 2/2/15.
 */
@RequestMapping("/myservice")
public class MyServiceQBit {



    ActualService actualService = new ActualService();
    int count = 0;

    @RequestMapping("/ping")
    public List ping() {
        return Collections.singletonList("Hello World!");
    }

    @RequestMapping("/addkey/" )
    public double addKey(@RequestParam("key") int key, @RequestParam("value") String value) {

        count++;
        return actualService.addKey(key, value);
    }

    void queueLimit() {
        if (count > 5) {
            count = 0;
            actualService.write();
        }
    }

    void queueEmpty() {

        if (count > 5) {
            count = 0;
            actualService.write();
        }
    }

    public static void main(String... args) throws Exception {



        //86K TPS QBit
        //Jetty/Spring boot 47K
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setArrayBlockingQueue().setSize(10_000))
//                .setPort(6060).setFlushInterval(10)
//                .build();

        //81K TPS QBit
        //J S/B 47K
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setLinkTransferQueue())
//                .setPort(6060).setFlushInterval(10)
//                .build();


        //83K TPS QBit
        //J S/B 47K
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setLinkTransferQueue().setCheckEvery(10).setTryTransfer(true))
//                .setPort(6060).setFlushInterval(10)
//                .build();


        //80K TPS QBit
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(1_000).setLinkTransferQueue().setCheckEvery(10).setTryTransfer(true))
//                .setPort(6060).setFlushInterval(10)
//                .build();


        //78K TPS QBit
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(10).setTryTransfer(true))
//                .setPort(6060).setFlushInterval(10)
//                .build();

        //78K TPS QBit
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(1).setTryTransfer(true))
//                .setPort(6060).setFlushInterval(10)
//                .build();


        //78K TPS QBit
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(1_000).setLinkTransferQueue().setCheckEvery(1).setTryTransfer(true))
//                .setPort(6060).setFlushInterval(10)
//                .build();

//        //After REFACTOR 82K
//        //Before 78K TPS QBit
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(1_000).setLinkTransferQueue().setCheckEvery(1).setTryTransfer(true))
//                .setPort(6060).setFlushInterval(10)
//                .build();



//        //After REFACTOR 82K NO DIF
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setRequestQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(100))
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(100))
//                .setPort(6060).setFlushInterval(100).setRequestBatchSize(1000)
//                .build();

        //After REFACTOR 82K
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setRequestQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(100))
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(10_000).setLinkTransferQueue().setCheckEvery(100))
//                .setPort(6060).setFlushInterval(100).setRequestBatchSize(1000)
//                .build();


        //Just realized all that work was with ping

        //./wrk -c 200 -d 10s "http://localhost:6060/services/myservice/addkey/?key=0&value=mom" -H "X_USER_ID: RICK"  --timeout 100000s -t 8
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setArrayBlockingQueue().setSize(10_000))
//                .setPort(6060).setFlushInterval(10)
//                .build();
//                //QBit 1354.81
//                //SB 5696.89

        //./wrk -c 200 -d 10s "http://localhost:6060/services/myservice/addkey/?key=0&value=mom" -H "X_USER_ID: RICK"  --timeout 100000s -t 8
//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                .setRequestQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setArrayBlockingQueue().setSize(10_000))
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setArrayBlockingQueue().setSize(10_000))
//                .setPort(6060).setFlushInterval(10)
//                .build();
        //QBit 1354.81
        //SB 5696.89

        //Up connections by 10x
        //./wrk -c 2000 -d 10s "http://localhost:6060/services/myservice/addkey/?key=0&value=mom" -H "X_USER_ID: RICK"  --timeout 100000s -t 16
        //QBit 6362.00
        //SB 5674.28

        //Up connections by 2x more
        //./wrk -c 2000 -d 10s "http://localhost:6060/services/myservice/addkey/?key=0&value=mom" -H "X_USER_ID: RICK"  --timeout 100000s -t 16
        //QBit 6320.62.00
        //SB 5551.37


        //Now pipeline test
        /*
        init = function(args)
           wrk.init(args)

           local r = {}

           r[1] = wrk.format("GET", "/services/myservice/addkey/?key=1&value=mom")
           r[2] = wrk.format("GET", "/services/myservice/addkey/?key=2&value=mom")
           r[3] = wrk.format("GET", "/services/myservice/addkey/?key=3&value=mom")
           r[4] = wrk.format("GET", "/services/myservice/addkey/?key=4&value=mom")
           r[5] = wrk.format("GET", "/services/myservice/addkey/?key=5&value=mom")
           r[6] = wrk.format("GET", "/services/myservice/addkey/?key=6&value=mom")
           r[7] = wrk.format("GET", "/services/myservice/addkey/?key=7&value=mom")
           r[8] = wrk.format("GET", "/services/myservice/addkey/?key=8&value=mom")
           r[9] = wrk.format("GET", "/services/myservice/addkey/?key=9&value=mom")
           r[10] = wrk.format("GET", "/services/myservice/addkey/?key=0&value=mom")

           req = table.concat(r)
        end

        request = function()
           return req
        end


        bash
         ./wrk -c 1000 -d 10s "http://localhost:6060" -s pipeline.lua --timeout 100000s -t 16
Running 10s test @ http://localhost:6060
         */

        //QBit 49,102
        //SB/J 30,640

//        final ServiceServer serviceServer = new ServiceServerBuilder()
//                //2,500,000 454,065
//                .setRequestQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setArrayBlockingQueue().setSize(100_000))
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setLinkTransferQueue().setCheckEvery(5))
//                .setPort(6060).setFlushInterval(10)
//                .build();

//                final ServiceServer serviceServer = new ServiceServerBuilder()
//                //2,500,000 454,065
//                .setRequestQueueBuilder(
//                        QueueBuilder.queueBuilder()
//                                .setBatchSize(250).setLinkTransferQueue().setCheckEvery(5)
//                )
//                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
//                        .setBatchSize(250).setLinkTransferQueue().setCheckEvery(5))
//                .setPort(6060).setFlushInterval(10).setRequestBatchSize(100)
//                .build();

                //50089.54
                //30360.42

        final ServiceServer serviceServer = serviceServerBuilder()
                //2,500,000 454,065
                .setRequestQueueBuilder(
                        QueueBuilder.queueBuilder()
                                .setBatchSize(1000).setLinkTransferQueue().setCheckEvery(50)
                )
                .setServiceBundleQueueBuilder(QueueBuilder.queueBuilder()
                        .setBatchSize(250).setLinkTransferQueue().setCheckEvery(5))
                .setPort(6060).setFlushInterval(10).setRequestBatchSize(100)
                .setTimeoutSeconds(60)
                .build();


        serviceServer.initServices(new MyServiceQBit());
        serviceServer.start();


        while (true) Sys.sleep(100_000_000);
    }



}

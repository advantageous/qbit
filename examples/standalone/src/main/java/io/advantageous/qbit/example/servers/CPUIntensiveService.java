/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.example.servers;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestParam;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.boon.core.Sys;

import java.util.Collections;
import java.util.List;

import static io.advantageous.qbit.server.ServiceServerBuilder.serviceServerBuilder;

@RequestMapping("/myservice")
public class CPUIntensiveService {


    ActualService actualService = new ActualService();
    int count = 0;

    public static void main(String... args) throws Exception {


        final ServiceServer serviceServer = serviceServerBuilder()
                //2,500,000 454,065
                .setHttpRequestQueueBuilder(
                        QueueBuilder.queueBuilder()
                                .setBatchSize(1000).setLinkTransferQueue().setCheckEvery(50).setPollWait(100)
                )
                .setResponseQueueBuilder(QueueBuilder.queueBuilder()
                        .setBatchSize(250).setLinkTransferQueue().setCheckEvery(5))
                .setPort(6060).setFlushInterval(10).setRequestBatchSize(100)
                .setTimeoutSeconds(60)
                .build();

        serviceServer.initServices(new CPUIntensiveService());
        serviceServer.start();


        while (true) Sys.sleep(100_000_000);
    }

    @RequestMapping("/ping")
    public List ping() {
        return Collections.singletonList("Hello World!");
    }

    @RequestMapping("/addkey/")
    public double addKey(@RequestParam("key") int key, @RequestParam("value") String value) {

        count++;
        return actualService.addKey(key, value);
    }

    void queueLimit() {
        if (count > 1000) {
            count = 0;
            actualService.write();
        }
    }

    void queueEmpty() {

        if (count > 1000) {
            count = 0;
            actualService.write();
        }
    }


}

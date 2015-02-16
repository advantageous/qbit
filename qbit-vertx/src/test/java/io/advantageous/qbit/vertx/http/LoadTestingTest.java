/*******************************************************************************

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
  *  ________ __________.______________
  *  \_____  \\______   \   \__    ___/
  *   /  / \  \|    |  _/   | |    |  ______
  *  /   \_/.  \    |   \   | |    | /_____/
  *  \_____\ \_/______  /___| |____|
  *         \__>      \/
  *  ___________.__                  ____.                        _____  .__                                             .__
  *  \__    ___/|  |__   ____       |    |____ ___  _______      /     \ |__| ___________  ____  ______ ______________  _|__| ____  ____
  *    |    |   |  |  \_/ __ \      |    \__  \\  \/ /\__  \    /  \ /  \|  |/ ___\_  __ \/  _ \/  ___// __ \_  __ \  \/ /  |/ ___\/ __ \
  *    |    |   |   Y  \  ___/  /\__|    |/ __ \\   /  / __ \_ /    Y    \  \  \___|  | \(  <_> )___ \\  ___/|  | \/\   /|  \  \__\  ___/
  *    |____|   |___|  /\___  > \________(____  /\_/  (____  / \____|__  /__|\___  >__|   \____/____  >\___  >__|    \_/ |__|\___  >___  >
  *                  \/     \/                \/           \/          \/        \/                 \/     \/                    \/    \/
  *  .____    ._____.
  *  |    |   |__\_ |__
  *  |    |   |  || __ \
  *  |    |___|  || \_\ \
  *  |_______ \__||___  /
  *          \/       \/
  *       ____. _________________    _______         __      __      ___.     _________              __           __      _____________________ ____________________
  *      |    |/   _____/\_____  \   \      \       /  \    /  \ ____\_ |__  /   _____/ ____   ____ |  | __ _____/  |_    \______   \_   _____//   _____/\__    ___/
  *      |    |\_____  \  /   |   \  /   |   \      \   \/\/   // __ \| __ \ \_____  \ /  _ \_/ ___\|  |/ // __ \   __\    |       _/|    __)_ \_____  \   |    |
  *  /\__|    |/        \/    |    \/    |    \      \        /\  ___/| \_\ \/        (  <_> )  \___|    <\  ___/|  |      |    |   \|        \/        \  |    |
  *  \________/_______  /\_______  /\____|__  / /\    \__/\  /  \___  >___  /_______  /\____/ \___  >__|_ \\___  >__| /\   |____|_  /_______  /_______  /  |____|
  *                   \/         \/         \/  )/         \/       \/    \/        \/            \/     \/    \/     )/          \/        \/        \/
  *  __________           __  .__              __      __      ___.
  *  \______   \ ____   _/  |_|  |__   ____   /  \    /  \ ____\_ |__                                                                                               
  *  |    |  _// __ \  \   __\  |  \_/ __ \  \   \/\/   // __ \| __ \
  *   |    |   \  ___/   |  | |   Y  \  ___/   \        /\  ___/| \_\ \
  *   |______  /\___  >  |__| |___|  /\___  >   \__/\  /  \___  >___  /
  *          \/     \/             \/     \/         \/       \/    \/
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html

 ******************************************************************************/

package io.advantageous.qbit.vertx.http;

import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.http.client.HttpClient;
import io.advantageous.qbit.http.client.HttpClientBuilder;
import io.advantageous.qbit.server.ServiceServer;
import io.advantageous.qbit.server.ServiceServerBuilder;
import io.advantageous.qbit.service.Callback;
import org.boon.core.Sys;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.boon.Boon.puts;
import static org.boon.Exceptions.die;

/**
 * Created by rhightower on 1/21/15.
 */
public class LoadTestingTest {

    public static final int WARMUP = 10_000;
    static volatile int callCount;
    static volatile int returnCount;
    final Callback<String> callback = new Callback<String>() {
        @Override
        public void accept(String s) {

            returnCount++;
        }
    };
    static volatile int port = 5555;
    Client client;
    ServiceServer server;
    HttpClient httpClient;
    ClientServiceInterface clientProxy;
    AtomicReference<String> pongValue;
    boolean ok;

    //@Test
    public void warmup() throws Exception {


        final long startTime = System.currentTimeMillis();


        for (int index = 0; index < WARMUP; index++) {

            clientProxy.ping(callback, "hi");

        }

        client.flush();

        while (returnCount < WARMUP) {
            Sys.sleep(10);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();

        Sys.sleep(1000);

        ok = returnCount == callCount || die(callCount, returnCount);

        final long duration = endTime - startTime;

        puts(duration);


        client.flush();

        Sys.sleep(1000);


    }

    //@Test
    public void test100K() throws Exception {


        returnCount = 0;
        callCount = 0;
        Sys.sleep(100);


        returnCount = 0;
        callCount = 0;


        final long startTime = System.currentTimeMillis();


        for (int index = 0; index < 100_000; index++) {

            clientProxy.ping(callback, "hi");

        }


        client.flush();

        while (returnCount < 100_000 - 1) {
            Sys.sleep(1);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();


        client.flush();


        ok = returnCount == callCount || die();

        final long duration = endTime - startTime;

        puts(duration);


    }

    @Test
    public void test() throws Exception {

    }

    //@Test
    public void test1M() throws Exception {


        returnCount = 0;
        callCount = 0;
        Sys.sleep(10000);


        returnCount = 0;
        callCount = 0;


        final long startTime = System.currentTimeMillis();


        for (int index = 0; index < 1_000_000; index++) {

            clientProxy.ping(callback, "hi");


        }


        client.flush();

        while (returnCount < 1_000_000) {
            Sys.sleep(100);
        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime = System.currentTimeMillis();

        ok = returnCount == callCount || die();


        final long duration = endTime - startTime;

        puts("DURATION 1", duration);


        returnCount = 0;
        callCount = 0;
        Sys.sleep(10000);


        returnCount = 0;
        callCount = 0;


        ok = returnCount == callCount || die();


        final long startTime2 = System.currentTimeMillis();


        for (int index = 0; index < 5_000_000; index++) {

            clientProxy.ping(callback, "hi");


        }


        client.flush();

        while (returnCount < 5_000_000) {
            Sys.sleep(100);

        }


        puts("HERE                        ", callCount, returnCount);


        final long endTime2 = System.currentTimeMillis();


        Sys.sleep(5_000);

        ok = returnCount == callCount || die();

        final long duration2 = endTime2 - startTime2;


        puts("DURATION 2", duration2);

    }

    @Before
    public void setup() throws Exception {

        Sys.sleep(100);
        pongValue = new AtomicReference<>();

        httpClient = new HttpClientBuilder().setPort(port).build();

        client = new ClientBuilder().setRequestBatchSize(20000).setFlushInterval(200).setPort(port).build();
        server = new ServiceServerBuilder().setRequestBatchSize(20000).setFlushInterval(200).setTimeoutSeconds(20)
                .setPollTime(10).setPort(port).build();

        server.initServices(new MockService());

        server.start();

        Sys.sleep(200);

        clientProxy = client.createProxy(ClientServiceInterface.class, "mockService");
        client.start();
        httpClient.start();

        callCount = 0;
        pongValue.set(null);

        Sys.sleep(200);


    }

    @After
    public void teardown() throws Exception {

        port++;


        Sys.sleep(200);
        server.stop();
        Sys.sleep(200);
        client.stop();
        httpClient.stop();
        Sys.sleep(200);
        server = null;
        client = null;
        System.gc();
        Sys.sleep(100);

    }

    static interface ClientServiceInterface {
        String ping(Callback<String> callback, String ping);
    }

    class MockService {

        @RequestMapping(method = RequestMethod.POST)
        public String ping(String ping) {
            callCount++;
            return ping + " pong";
        }
    }
}

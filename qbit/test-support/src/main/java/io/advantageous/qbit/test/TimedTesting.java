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

package io.advantageous.qbit.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Created by rhightower on 2/17/15.
 */
public class TimedTesting {


    protected boolean ok;
    protected CountDownLatch latch = new CountDownLatch(1);
    protected AtomicBoolean stop = new AtomicBoolean();

    protected void setupLatch() {
        latch = new CountDownLatch(1);
    }

    protected void waitForTrigger(int seconds, Predicate predicate) {

        triggerLatchWhen(predicate);
        waitForLatch(seconds);
    }

    protected void triggerLatchWhen(Predicate predicate) {

        Thread thread = new Thread(() -> {

            while (true) {

                if (predicate.test(null)) {
                    latch.countDown();
                    return;
                }

                if (stop.get()) {
                    return;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

    protected void waitForLatch(int seconds) {

        try {
            latch.await(seconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stop.set(true);


        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

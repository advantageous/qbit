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
  *
  * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
  *  http://rick-hightower.blogspot.com/2014/12/rise-of-machines-writing-high-speed.html
  *  http://rick-hightower.blogspot.com/2014/12/quick-guide-to-programming-services-in.html
  *  http://rick-hightower.blogspot.com/2015/01/quick-start-qbit-programming.html
  *  http://rick-hightower.blogspot.com/2015/01/high-speed-soa.html
  *  http://rick-hightower.blogspot.com/2015/02/qbit-event-bus.html
 ******************************************************************************/

package io.advantageous.qbit.queue.impl;

import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.queue.SendQueue;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author rhightower
 * on 2/24/15.
 */
public class AutoFlushingSendQueue<T> implements SendQueue<T> {

    private final SendQueue<T> sendQueue;
    private final ReentrantLock lock = new ReentrantLock();

    private final PeriodicScheduler periodicScheduler;
    private final int interval;
    private final TimeUnit timeUnit;

    private  ScheduledFuture scheduledFuture;

    public AutoFlushingSendQueue(final SendQueue<T> sendQueue,
                                 final PeriodicScheduler periodicScheduler,
                                 int interval,
                                 TimeUnit timeUnit) {
        this.sendQueue = sendQueue;

        this.periodicScheduler = periodicScheduler;
        this.interval = interval;
        this.timeUnit = timeUnit;
    }

    @Override
    public void start() {
        scheduledFuture = periodicScheduler.repeat(() -> flushSends(), interval, timeUnit);
    }

    @Override
    public void stop() {
        scheduledFuture.cancel(true);
    }

    @Override
    public void send(T item) {

        try {
            lock.lock();
            sendQueue.send(item);
        }finally {
            lock.unlock();
        }

    }

    @Override
    public void sendAndFlush(T item) {

        try {
            lock.lock();
            sendQueue.sendAndFlush(item);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void sendMany(T... items) {

        try {
            lock.lock();
            sendQueue.sendMany(items);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void sendBatch(Collection<T> items) {

        try {
            lock.lock();
            sendQueue.sendBatch(items);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void sendBatch(Iterable<T> items) {

        try {
            lock.lock();
            sendQueue.sendBatch(items);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean shouldBatch() {
        return sendQueue.shouldBatch();
    }

    @Override
    public void flushSends() {


        try {
            lock.lock();
            sendQueue.flushSends();
        }finally {
            lock.unlock();
        }
    }
}
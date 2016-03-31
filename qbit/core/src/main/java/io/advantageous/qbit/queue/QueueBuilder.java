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

package io.advantageous.qbit.queue;

import io.advantageous.qbit.config.PropertyResolver;
import io.advantageous.qbit.queue.impl.AddTimeoutUnableToEnqueueHandler;
import io.advantageous.qbit.queue.impl.BasicQueue;
import io.advantageous.qbit.queue.impl.DefaultUnableToEnqueueHandler;

import java.util.Properties;
import java.util.concurrent.*;

/**
 * Allows for the programmatic construction of a queue.
 * <p>
 * created by rhightower on 12/14/14.
 */
public class QueueBuilder implements Cloneable {

    public static final String QBIT_QUEUE_BUILDER = "qbit.queue.builder.";

    private int batchSize;

    private int limit = -1;
    private int pollWait;
    private int size;
    private int checkEvery;
    private boolean tryTransfer;
    private String name;
    private Class<? extends BlockingQueue> queueClass;
    private boolean checkIfBusy = false;
    private TimeUnit pollTimeUnit = TimeUnit.MILLISECONDS;
    private TimeUnit enqueueTimeoutTimeUnit = null;
    private int enqueueTimeout;

    private UnableToEnqueueHandler unableToEnqueueHandler;

    public QueueBuilder(PropertyResolver propertyResolver) {
        this.pollWait = propertyResolver
                .getIntegerProperty("pollWaitMS", 15);
        this.enqueueTimeout = propertyResolver
                .getIntegerProperty("enqueueTimeoutSeconds", 1000);
        this.batchSize = propertyResolver
                .getIntegerProperty("batchSize", 10);
        this.checkEvery = propertyResolver
                .getIntegerProperty("checkEvery", 10);
        this.size = propertyResolver
                .getIntegerProperty("size", 100_000);
        this.checkIfBusy = propertyResolver
                .getBooleanProperty("checkIfBusy", false);
        this.tryTransfer = propertyResolver
                .getBooleanProperty("tryTransfer", false);

        this.queueClass = propertyResolver
                .getGenericPropertyWithDefault("queueClass", ArrayBlockingQueue.class);

    }

    public QueueBuilder() {
        this(PropertyResolver.createSystemPropertyResolver(QBIT_QUEUE_BUILDER));
    }

    public QueueBuilder(final Properties properties) {
        this(PropertyResolver.createPropertiesPropertyResolver(
                QBIT_QUEUE_BUILDER, properties));
    }

    public static QueueBuilder queueBuilder() {
        return new QueueBuilder();
    }

    public int getLimit() {
        if (limit == -1) {
            if (batchSize != 1) {
                limit = batchSize;
            } else {
                limit = 10;
            }
        }
        return limit;
    }

    public QueueBuilder setLimit(final int limit) {
        this.limit = limit;
        return this;
    }

    public UnableToEnqueueHandler getUnableToEnqueueHandler() {

        if (unableToEnqueueHandler == null) {

            if (enqueueTimeoutTimeUnit == null) {
                unableToEnqueueHandler = new DefaultUnableToEnqueueHandler();
            } else {
                unableToEnqueueHandler = new AddTimeoutUnableToEnqueueHandler(enqueueTimeout, enqueueTimeoutTimeUnit);
            }
        }

        return unableToEnqueueHandler;
    }

    public QueueBuilder setUnableToEnqueueHandler(UnableToEnqueueHandler unableToEnqueueHandler) {
        this.unableToEnqueueHandler = unableToEnqueueHandler;
        return this;
    }

    public TimeUnit getEnqueueTimeoutTimeUnit() {
        return enqueueTimeoutTimeUnit;
    }

    public QueueBuilder setEnqueueTimeoutTimeUnit(TimeUnit enqueueTimeoutTimeUnit) {
        this.enqueueTimeoutTimeUnit = enqueueTimeoutTimeUnit;
        return this;
    }

    public TimeUnit getPollTimeUnit() {
        return pollTimeUnit;
    }

    public QueueBuilder setPollTimeUnit(TimeUnit pollTimeUnit) {
        this.pollTimeUnit = pollTimeUnit;
        return this;
    }

    public int getEnqueueTimeout() {
        return enqueueTimeout;
    }

    public QueueBuilder setEnqueueTimeout(int enqueueTimeout) {
        this.enqueueTimeout = enqueueTimeout;
        return this;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getCheckEvery() {
        return checkEvery;
    }

    public QueueBuilder setCheckEvery(int checkEvery) {
        this.checkEvery = checkEvery;
        this.checkIfBusy = true;
        return this;
    }

    public boolean isTryTransfer() {
        return tryTransfer;
    }

    public QueueBuilder setTryTransfer(boolean tryTransfer) {
        this.tryTransfer = tryTransfer;
        return this;
    }

    public Class<? extends BlockingQueue> getQueueClass() {
        return queueClass;
    }

    public void setQueueClass(Class<? extends BlockingQueue> queueClass) {
        this.queueClass = queueClass;
    }

    public QueueBuilder setLinkedBlockingQueue() {
        queueClass = LinkedBlockingQueue.class;
        return this;
    }

    public QueueBuilder setArrayBlockingQueue() {
        if (size == -1) {
            size = 100_000;
        }

        queueClass = ArrayBlockingQueue.class;
        return this;
    }


    public QueueBuilder setLinkTransferQueue() {
        size = -1;
        batchSize = checkEvery * 10;
        queueClass = LinkedTransferQueue.class;
        return this;
    }


    public int getSize() {
        return size;
    }

    public QueueBuilder setSize(int size) {
        this.size = size;
        return this;
    }

    public boolean isCheckIfBusy() {
        return checkIfBusy;
    }

    public QueueBuilder setCheckIfBusy(boolean checkIfBusy) {
        this.checkIfBusy = checkIfBusy;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public QueueBuilder setBatchSize(int batchSize) {

        if (batchSize == 1) {
            this.setLinkTransferQueue();
        }
        this.batchSize = batchSize;

        return this;
    }

    public int getPollWait() {
        return pollWait;
    }

    public QueueBuilder setPollWait(int pollWait) {
        this.pollWait = pollWait;
        return this;
    }


    public String getName() {
        return name;
    }

    public QueueBuilder setName(String name) {
        this.name = name;
        return this;

    }


    public <T> Queue<T> build() {
        return new BasicQueue<>(this.getName(),
                this.getPollWait(),
                this.getPollTimeUnit(),
                this.getBatchSize(),
                this.getQueueClass(),
                this.isCheckIfBusy(),
                this.getSize(),
                this.getCheckEvery(),
                this.isTryTransfer(),
                this.getUnableToEnqueueHandler(),
                this.getLimit());
    }

}

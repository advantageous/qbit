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

package io.advantageous.qbit.service;

import io.advantageous.qbit.GlobalConstants;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.message.Request;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.service.impl.ServiceConstants;
import io.advantageous.qbit.system.QBitSystemManager;
import io.advantageous.qbit.transforms.Transformer;

/**
 * Allows for the programmatic construction of a service.
 *
 * @author rhightower
 *         Created by Richard on 11/14/14.
 *         <p>
 *         Created by rhightower on 1/19/15.
 */
public class ServiceBundleBuilder {


    public static ServiceBundleBuilder serviceBundleBuilder() {
        return new ServiceBundleBuilder();
    }

    private QueueBuilder requestQueueBuilder;
    private QueueBuilder responseQueueBuilder;
    private int pollTime = GlobalConstants.POLL_WAIT;
    private int requestBatchSize = GlobalConstants.BATCH_SIZE;
    private boolean invokeDynamic = true;
    private String address = "/services";
    private boolean eachServiceInItsOwnThread = true;
    private QBitSystemManager qBitSystemManager;

    private Queue<Response<Object>> responseQueue;
    /**
     * Allows interception of method calls before they get sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCall = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows interception of method calls before they get transformed and sent to a client.
     * This allows us to transform or reject method calls.
     */
    private BeforeMethodCall beforeMethodCallAfterTransform = ServiceConstants.NO_OP_BEFORE_METHOD_CALL;
    /**
     * Allows transformation of arguments, for example from JSON to Java objects.
     */
    private Transformer<Request, Object> argTransformer = ServiceConstants.NO_OP_ARG_TRANSFORM;

    public QBitSystemManager getSystemManager() {
        return qBitSystemManager;
    }

    public ServiceBundleBuilder setSystemManager(QBitSystemManager qBitSystemManager) {
        this.qBitSystemManager = qBitSystemManager;
        return this;
    }

    public boolean isInvokeDynamic() {
        return invokeDynamic;
    }

    public ServiceBundleBuilder setInvokeDynamic(boolean invokeDynamic) {
        this.invokeDynamic = invokeDynamic;
        return this;
    }

    public boolean isEachServiceInItsOwnThread() {
        return eachServiceInItsOwnThread;
    }

    public ServiceBundleBuilder setEachServiceInItsOwnThread(boolean eachServiceInItsOwnThread) {
        this.eachServiceInItsOwnThread = eachServiceInItsOwnThread;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCall() {
        return beforeMethodCall;
    }

    public ServiceBundleBuilder setBeforeMethodCall(BeforeMethodCall beforeMethodCall) {
        this.beforeMethodCall = beforeMethodCall;
        return this;
    }

    public BeforeMethodCall getBeforeMethodCallAfterTransform() {
        return beforeMethodCallAfterTransform;

    }

    public ServiceBundleBuilder setBeforeMethodCallAfterTransform(BeforeMethodCall beforeMethodCallAfterTransform) {
        this.beforeMethodCallAfterTransform = beforeMethodCallAfterTransform;
        return this;

    }

    public Transformer<Request, Object> getArgTransformer() {
        return argTransformer;

    }

    public ServiceBundleBuilder setArgTransformer(Transformer<Request, Object> argTransformer) {
        this.argTransformer = argTransformer;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public ServiceBundleBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public int getPollTime() {
        return pollTime;
    }

    public ServiceBundleBuilder setPollTime(int pollTime) {
        this.pollTime = pollTime;
        return this;
    }

    public int getRequestBatchSize() {
        return requestBatchSize;
    }

    public ServiceBundleBuilder setRequestBatchSize(int requestBatchSize) {
        this.requestBatchSize = requestBatchSize;
        return this;
    }

    public QueueBuilder getRequestQueueBuilder() {
        return requestQueueBuilder;
    }

    public QueueBuilder getResponseQueueBuilder() {
        return responseQueueBuilder;
    }

    public ServiceBundleBuilder setResponseQueueBuilder(QueueBuilder queueBuilder) {
        this.responseQueueBuilder = queueBuilder;
        return this;
    }


    public ServiceBundleBuilder setRequestQueueBuilder(QueueBuilder queueBuilder) {
        this.requestQueueBuilder = queueBuilder;
        return this;
    }

    public Queue<Response<Object>> getResponseQueue() {
        return responseQueue;
    }

    public ServiceBundleBuilder setResponseQueue(final Queue<Response<Object>> responseQueue) {
        this.responseQueue = responseQueue;
        return this;
    }

    public ServiceBundle build() {

        if (responseQueueBuilder == null) {

            if (responseQueue!=null) {

                responseQueueBuilder = new QueueBuilder(){

                    @Override
                    public <T> Queue<T> build() {
                        return (Queue<T>) responseQueue;
                    }
                };

            } else {

                responseQueueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize()).setPollWait(this.getPollTime());
            }

        }


        if (requestQueueBuilder == null) {
            requestQueueBuilder = new QueueBuilder().setBatchSize(this.getRequestBatchSize())
                    .setPollWait(this.getPollTime());
        }

        final ServiceBundle serviceBundle = QBit.factory().createServiceBundle(this.getAddress(),
                requestQueueBuilder,
                responseQueueBuilder,
                QBit.factory(),
                eachServiceInItsOwnThread, this.getBeforeMethodCall(), this.getBeforeMethodCallAfterTransform(),
                this.getArgTransformer(), invokeDynamic, this.getSystemManager());


        if (serviceBundle != null && qBitSystemManager != null) {
            qBitSystemManager.registerServiceBundle(serviceBundle);
        }

        if (serviceBundle==null) {
            throw new IllegalStateException("Service Bundle was null");
        }

        return serviceBundle;


    }

    public ServiceBundle buildAndStart() {
        final ServiceBundle build = build();
        build.start();
        return build;
    }
}


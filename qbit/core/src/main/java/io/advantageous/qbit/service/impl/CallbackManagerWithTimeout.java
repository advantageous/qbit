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

package io.advantageous.qbit.service.impl;

import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps incoming call returns to client callback objects.
 */
public class CallbackManagerWithTimeout implements CallbackManager {

    private final String name;
    private final boolean handleTimeouts;
    private final long timeOutMS;
    private final long checkInterval;
    private final Timer timer;
    private final Logger logger = LoggerFactory.getLogger(CallbackManagerWithTimeout.class);
    private final boolean debug = logger.isDebugEnabled();
    /**
     * Maps incoming calls with outgoing handlers (returns, async returns really).
     */
    private final Map<HandlerKey, Callback<Object>> handlers = new ConcurrentHashMap<>();
    private long lastCheckTime;
    private long now;

    public CallbackManagerWithTimeout(final Timer timer, final String name,
                                      boolean handleTimeouts,
                                      long timeOutMS, long checkInterval) {

        this.name = name;
        this.handleTimeouts = handleTimeouts;

        this.timeOutMS = timeOutMS;
        this.checkInterval = checkInterval > 0 ? checkInterval : 5_000;
        this.lastCheckTime = timer.now();
        this.now = lastCheckTime;
        this.timer = timer;
    }

    /**
     * Register a callbackWithTimeout handler
     *
     * @param methodCall method call
     * @param handler    call back handler to register
     */
    private void registerHandlerCallbackForClient(final MethodCall<Object> methodCall,
                                                  final Callback<Object> handler) {

        final HandlerKey handlerKey = new HandlerKey(methodCall.returnAddress(), methodCall.address(),
                methodCall.id(), methodCall.timestamp());

        if (debug) {
            if (handlers.containsKey(handlerKey)) {
                logger.debug("DUPLICATE HANDLERS {}", handlerKey);
            }
        }

        handlers.put(handlerKey, handler);

    }


    @Override
    public void registerCallbacks(final MethodCall<Object> methodCall) {
        registerHandlerCallbackForClient(methodCall, methodCall.callback());
    }


    /**
     * Handles responses coming back from services.
     *
     * @param responseQueue response queue
     */
    @Override
    public void startReturnHandlerProcessor(final Queue<Response<Object>> responseQueue) {

        //noinspection Convert2MethodRef
        responseQueue.startListener(response -> handleResponse(response));
    }

    @Override
    public void handleResponse(final Response<Object> response) {

        final HandlerKey handlerKey = new HandlerKey(
                response.returnAddress(),
                response.address(),
                response.id(),
                response.timestamp());


        final Callback<Object> handler = handlers.remove(handlerKey);

        if (handler == null) {
            if (response.request().hasCallback()) {
                logger.error("Could not find handler for key {}", handlerKey);
            }
            return;
        } else {

            if (debug)
                logger.info("FOUND HANDLER {}", handlerKey);
        }

        if (response.wasErrors()) {

            if (debug) {
                logger.debug("Service threw an exception address {} return address {} message id {} response error {}",
                        response.address(),
                        response.returnAddress(),
                        response.id(),
                        response.body());
            }

            if (response.body() instanceof Throwable) {
                handler.onError(((Throwable) response.body()));
            } else {
                handler.onError(new Exception(response.body().toString()));
            }
        } else {
            handler.accept(response.body());
        }

    }

    @Override
    public void process(long currentTime) {
        if (currentTime != 0) {
            this.now = currentTime;
        } else {
            this.now = timer.now();
        }


        long duration = this.now - lastCheckTime;

        if (duration > checkInterval) {
            lastCheckTime = this.now;
            if (handleTimeouts) {
                checkForTimeOuts(timeOutMS);
            } else {
                if (handlers.size() > 8_000) {
                    if (debug) {
                        logger.debug("Issue with handlers growing too large size {} " +
                                        "service name {}",
                                handlers.size(), this.name);
                    }
                }


                if (handlers.size() > 32_000) {
                    logger.error("Issue with handlers growing very large size {} " +
                                    "service name {}",
                            handlers.size(), this.name);
                    checkForTimeOuts(60_000);
                }

            }
        }

    }

    private void checkForTimeOuts(long timeOutMS) {

        if (debug) {
            logger.debug("checking for timeouts");
        }

        final ArrayList<Map.Entry<HandlerKey, Callback<Object>>> entries = new ArrayList<>(handlers.entrySet());


        for (Map.Entry<HandlerKey, Callback<Object>> entry : entries) {
            long duration = now - entry.getKey().timestamp;
            if (duration > timeOutMS) {


                if (debug) logger.debug("{} Call has timed out duration {} {} {}", name,
                        now - entry.getKey().timestamp,
                        entry.getKey().returnAddress,
                        entry.getKey().messageId,
                        new Date(entry.getKey().timestamp));

                handlers.remove(entry.getKey());
                entry.getValue().onTimeout();
            }
        }

    }


    public int outstandingCallbacksCount() {
        return handlers.size();
    }

}

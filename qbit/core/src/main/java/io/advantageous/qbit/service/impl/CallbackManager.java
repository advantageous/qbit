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
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Maps incoming call returns to client callback objects.
 */
public class CallbackManager {

    private final String name;
    private final boolean handleTimeouts;
    private final long timeOutMS;
    private final long checkInterval;
    private final Timer timer;
    private long lastCheckTime;


    public static CallbackManager callbackManager(String name) {
        return new CallbackManager(Timer.timer(), name, true, 1000, 1000);
    }


    public CallbackManager(final Timer timer, final String name, boolean handleTimeouts,
                    long timeOutMS, long checkInterval) {

        this.name = name;
        this.handleTimeouts = handleTimeouts;
        this.timeOutMS = timeOutMS;
        this.checkInterval = checkInterval;
        this.timer = timer;
    }

    private final Logger logger = LoggerFactory.getLogger(CallbackManager.class);
    private final boolean debug = logger.isDebugEnabled();
    /**
     * Maps incoming calls with outgoing handlers (returns, async returns really).
     */
    private final Map<HandlerKey, Callback<Object>> handlers = new ConcurrentHashMap<>();

    /**
     * Register a callbackWithTimeout handler
     *
     * @param methodCall method call
     * @param handler    call back handler to register
     */
    private void registerHandlerCallbackForClient(final MethodCall<Object> methodCall,
                                                  final Callback<Object> handler) {
        handlers.put(new HandlerKey(methodCall.returnAddress(), methodCall.id(), now), handler);
    }


    public void registerCallbacks(MethodCall<Object> methodCall) {
        Object args = methodCall.body();

        /** Look for callbackWithTimeout handler in the args */
        if (args instanceof Iterable) {
            final Iterable list = (Iterable) args;
            for (Object arg : list) {
                if (arg instanceof Callback) {
                    //noinspection unchecked
                    registerHandlerCallbackForClient(methodCall, (Callback) arg);
                }
            }
        } else if (args instanceof Object[]) {
            final Object[] array = (Object[]) args;
            for (Object arg : array) {
                if (arg instanceof Callback) {
                    //noinspection unchecked
                    registerHandlerCallbackForClient(methodCall, ((Callback) arg));
                }
            }
        }
    }


    /**
     * Handles responses coming back from services.
     *
     * @param responseQueue response queue
     */
    public void startReturnHandlerProcessor(final Queue<Response<Object>> responseQueue) {

        //noinspection Convert2MethodRef
        responseQueue.startListener(response -> handleResponse(response));
    }

    public void handleResponse(Response<Object> response) {
        final Callback<Object> handler = handlers.get(new HandlerKey(response.returnAddress(), response.id(), now));

        if (handler == null) {
            return;
        }

        if (response.wasErrors()) {

            if (debug)  {
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

    private long now;
    public void process(long currentTime) {

        if (!handleTimeouts) {
            return;
        }
        if (currentTime != 0) {
            this.now = currentTime;
        } else {
            this.now = timer.now();
        }

        long duration = this.now - lastCheckTime;

        if (duration > checkInterval) {
            checkForTimeOuts();
            lastCheckTime = this.now;
        }

    }

    private void checkForTimeOuts() {

        if (debug) {
            logger.debug("checking for timeouts");
        }

        final ArrayList<Map.Entry<HandlerKey, Callback<Object>>> entries = new ArrayList<>(handlers.entrySet());

        entries.stream().filter(handlerKeyCallbackEntry -> now - handlerKeyCallbackEntry.getKey().timestamp > timeOutMS)
                .forEach(handlerKeyCallbackEntry -> {


                    logger.info("Call has timed out duration {} {} {}",
                            now - handlerKeyCallbackEntry.getKey().timestamp,
                            handlerKeyCallbackEntry.getKey().returnAddress,
                            handlerKeyCallbackEntry.getKey().messageId,
                            new Date(handlerKeyCallbackEntry.getKey().timestamp));

                    handlers.remove(handlerKeyCallbackEntry.getKey());
                    handlerKeyCallbackEntry.getValue().onTimeout();
                });

    }


}

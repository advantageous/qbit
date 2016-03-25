package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.boon.core.Maps;
import io.advantageous.boon.core.Predicate;
import io.advantageous.boon.core.value.ValueContainer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.reactive.CallbackBuilder;
import io.advantageous.qbit.util.Timer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeoutException;

public class VertxEventBusBridge {
    private final Set<String> addressesToBridge;
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final JsonMapper jsonMapper;
    private final Vertx vertx;
    private final Predicate<MethodCall<Object>> methodCallPredicate;
    private final int flushIntervalMS;
    private final Timer timer;
    private long messageId = 0;
    private final Logger logger = LoggerFactory.getLogger(VertxEventBusBridge.class);
    private final EventBus vertxEventBus;

    public VertxEventBusBridge(final Set<String> addressesToBridge,
                               final SendQueue<MethodCall<Object>> methodCallSendQueue,
                               final JsonMapper jsonMapper,
                               final Vertx vertx,
                               final EventBus vertxEventBus,
                               final Timer timer,
                               final Predicate<MethodCall<Object>> methodCallPredicate,
                               final int flushIntervalMS,
                               final boolean autoStart) {


        this.timer = timer;
        this.vertxEventBus = vertxEventBus;
        this.addressesToBridge = Collections.unmodifiableSet(addressesToBridge);
        this.methodCallSendQueue = methodCallSendQueue;

        this.jsonMapper = jsonMapper;
        this.vertx = vertx;
        this.methodCallPredicate = methodCallPredicate;
        this.flushIntervalMS = flushIntervalMS;

        if (autoStart) {
            start();
        }

    }

    private void flush() {
        methodCallSendQueue.flushSends();
    }

    private void start() {
        /* Flush. */
        vertx.periodicStream(this.flushIntervalMS).handler(event -> flush());


        /* Register the service addresses */
        addressesToBridge.stream().forEach(address -> {
            /* register the consumer per service. */
            logger.debug("Registering address {}", address);
            final MessageConsumer<String> consumer = vertxEventBus.consumer(address);
            consumer.handler(message -> handleIncomingMessage(address, message));
            consumer.exceptionHandler(error -> logger.error("Error handling address " + address,  error));
        });
    }


    private void handleIncomingMessage(final String address, final Message<String> message) {
        final Map<String,Object> map = jsonMapper.fromJson(message.body(), Map.class);
        final Object method = map.get("method");
        final ValueContainer args = (ValueContainer) map.get("args");

        final Object body = args.toValue();


        final CallbackBuilder callbackBuilder = CallbackBuilder.callbackBuilder();
        callbackBuilder.setOnError(throwable -> {
            message.reply(jsonMapper.toJson(Maps.map("error", true, "cause", throwable)));
        });
        callbackBuilder.setCallback(returnedValue -> {
            message.reply(jsonMapper.toJson(Maps.map("returned", returnedValue)));
        });
        callbackBuilder.setOnTimeout(() -> {
            message.reply(jsonMapper.toJson(Maps.map(
                    "error", true,
                    "timeout", true, "cause",
                    new TimeoutException("Timed out call to " + address + " method " + method))));
        });


        final MethodCall<Object> methodCall = MethodCallBuilder
                .methodCallBuilder()
                .setAddress(address)
                .setBody(body)
                .setTimestamp(this.timer.time())
                .setName(method.toString())
                .setId(messageId++)
                .setCallback(callbackBuilder.build())
                .build();
        if (logger.isDebugEnabled()) {
            logger.debug("Calling method {} {}", methodCall.name(), message.body());
        }

        if (methodCallPredicate.test(methodCall)) {
            this.methodCallSendQueue.send(methodCall);
        }

    }



}

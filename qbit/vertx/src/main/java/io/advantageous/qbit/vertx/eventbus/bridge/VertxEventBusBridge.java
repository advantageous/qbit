package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.boon.core.Predicate;
import io.advantageous.boon.core.Sets;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.meta.transformer.StandardRequestTransformer;
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

public class VertxEventBusBridge {
    private final Set<String> addressesToBridge;
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final JsonMapper jsonMapper;
    private final Vertx vertx;
    private final Predicate<MethodCall<Object>> methodCallPredicate;
    private final int flushIntervalMS;
    private final Timer timer;
    private final Logger logger = LoggerFactory.getLogger(VertxEventBusBridge.class);
    private final EventBus vertxEventBus;
    private final StandardRequestTransformer standardRequestTransformer;
    private final JsonParserAndMapper jsonParserAndMapper;
    private long messageId = 0;


    public VertxEventBusBridge(final Set<String> addressesToBridge,
                               final SendQueue<MethodCall<Object>> methodCallSendQueue,
                               final JsonMapper jsonMapper,
                               final Vertx vertx,
                               final EventBus vertxEventBus,
                               final Timer timer,
                               final Predicate<MethodCall<Object>> methodCallPredicate,
                               final int flushIntervalMS,
                               final boolean autoStart,
                               final StandardRequestTransformer standardRequestTransformer) {


        this.standardRequestTransformer = standardRequestTransformer;
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
        jsonParserAndMapper = new JsonParserFactory().setIgnoreSet(Sets.set("metaClass")).createJsonCharArrayParser();

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
            consumer.exceptionHandler(error -> logger.error("Error handling address " + address, error));
        });
    }

    private void handleIncomingMessage(final String address, final Message<String> message) {

        try {

            final String json = message.body();

            final Map<String, Object> map = jsonParserAndMapper.parseMap(json);

            final String method = map.get("method").toString();
            final Object body = map.get("args");


            final List<String> errors = new ArrayList<>();
            final MethodCall<Object> transform = standardRequestTransformer.transFormBridgeBody(jsonMapper.toJson(body),
                    errors, address, method);

            if (errors.size() > 0) {
                logger.error("Error marshaling message body to method call to service errors {}", errors);
                message.fail(500, errors.toString());
                return;
            }

            final CallbackBuilder callbackBuilder = CallbackBuilder.callbackBuilder();
            callbackBuilder.setOnError(throwable -> {
                logger.error("Error from calling " + address, throwable);
                message.fail(500, throwable.getMessage());
            });
            callbackBuilder.setCallback(returnedValue -> message.reply(encodeOutput(returnedValue)));
            callbackBuilder.setOnTimeout(() -> {
                logger.error("Timed out call to " + address + " method " + method);
                message.fail(408, "Timed out call to " + address + " method " + method);
            });

            final MethodCall<Object> methodCall = MethodCallBuilder
                    .methodCallBuilder()
                    .setAddress(address)
                    .setBody(transform.body())
                    .setTimestamp(this.timer.time())
                    .setName(method)
                    .setId(messageId++)
                    .setCallback(callbackBuilder.build())
                    .build();
            if (logger.isDebugEnabled()) {
                logger.debug("Calling method {} {}", methodCall.name(), message.body());
            }

            if (methodCallPredicate.test(methodCall)) {
                this.methodCallSendQueue.send(methodCall);
            }

        } catch (Exception ex) {
            logger.error("Error marshaling message body to method call to service", ex);
            message.fail(500, ex.getMessage());
        }

    }

    private String encodeOutput(Object returnedValue) {

        return jsonMapper.toJson(returnedValue);
    }

}

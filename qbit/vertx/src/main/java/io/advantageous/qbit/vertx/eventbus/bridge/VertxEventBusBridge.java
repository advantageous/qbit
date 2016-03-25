package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.boon.core.Predicate;
import io.advantageous.boon.core.value.ValueContainer;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.Event;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.message.MethodCallBuilder;
import io.advantageous.qbit.message.Response;
import io.advantageous.qbit.queue.ReceiveQueue;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.util.Timer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VertxEventBusBridge {
    private final Set<String> addressesToBridge;
    private final SendQueue<MethodCall<Object>> methodCallSendQueue;
    private final ReceiveQueue<Response<Object>> receiveResponseQueue;
    private final Optional<SendQueue<Event<Object>>> eventSendQueue;
    private final JsonMapper jsonMapper;
    private final Vertx vertx;
    private final Predicate<MethodCall<Object>> methodCallPredicate;
    private final int flushIntervalMS;
    private final int pollResponsesInterval;
    private final Timer timer;
    private long messageId = 0;
    private final Logger logger = LoggerFactory.getLogger(VertxEventBusBridge.class);
    private final EventBus vertxEventBus;
    private List<SendQueue> queuesFlush;
    private final Map<MessageKey, Message<String>> messageKeyMessageMap;

    public VertxEventBusBridge(final Set<String> addressesToBridge,
                               final SendQueue<MethodCall<Object>> methodCallSendQueue,
                               final ReceiveQueue<Response<Object>> receiveResponseQueue,
                               final SendQueue<Event<Object>> eventSendQueue,
                               final JsonMapper jsonMapper,
                               final Vertx vertx,
                               final EventBus vertxEventBus,
                               final Timer timer,
                               final Predicate<MethodCall<Object>> methodCallPredicate,
                               final int flushIntervalMS,
                               final int pollResponsesIntervalMS,
                               final boolean autoStart) {


        this.timer = timer;
        this.vertxEventBus = vertxEventBus;
        this.addressesToBridge = Collections.unmodifiableSet(addressesToBridge);
        this.methodCallSendQueue = methodCallSendQueue;
        this.receiveResponseQueue = receiveResponseQueue;
        this.eventSendQueue = Optional.ofNullable(eventSendQueue);

        final ArrayList<SendQueue> flushList = new ArrayList<>(2);
        this.eventSendQueue.ifPresent(flushList::add);
        flushList.add(methodCallSendQueue);
        this.queuesFlush = Collections.unmodifiableList(flushList);
        this.jsonMapper = jsonMapper;
        this.vertx = vertx;
        this.methodCallPredicate = methodCallPredicate;
        this.flushIntervalMS = flushIntervalMS;
        this.pollResponsesInterval = pollResponsesIntervalMS;
        this.messageKeyMessageMap = new ConcurrentHashMap<>();

        if (autoStart) {
            start();
        }

    }

    private void flush() {
        queuesFlush.forEach(SendQueue::flushSends);
    }

    private void start() {
        /* Flush. */
        vertx.periodicStream(this.flushIntervalMS).handler(event -> flush());
        /* Check for responses. */
        vertx.periodicStream(this.pollResponsesInterval).handler(event -> checkForResponses());


        /* Sanity Check. */
        vertx.periodicStream(1000).handler(event -> sanityCheck());

        /* Register the service addresses */
        addressesToBridge.stream().forEach(address -> {
            /* register the consumer per service. */
            logger.debug("Registering address {}", address);
            final MessageConsumer<String> consumer = vertxEventBus.consumer(address);
            consumer.handler(message -> handleIncomingMessage(address, message));
            consumer.exceptionHandler(error -> logger.error("Error handling address " + address,  error));
        });
    }

    private void sanityCheck() {
        final long now = timer.now();

        if (messageKeyMessageMap.size() > 10_000) {
            logger.warn("Abnormal program state, it seems async method calls are not returning from service {}", messageKeyMessageMap.size());
            final List<MessageKey> timedOutKeys = messageKeyMessageMap.keySet().stream()
                    .filter(messageKey -> (now - messageKey.timeStamp) > 30_000)
                    .collect(Collectors.toList());
            logger.warn("There were {} timed out method calls", timedOutKeys.size());
            timedOutKeys.forEach(messageKeyMessageMap::remove);
        }

        if (logger.isDebugEnabled()) {
            logger.warn("Outstanding calls {}", messageKeyMessageMap.size());
            final List<MessageKey> timedOutKeys = messageKeyMessageMap.keySet().stream()
                    .filter(messageKey -> (now - messageKey.timeStamp) > 10_000)
                    .collect(Collectors.toList());
            logger.warn("There are {} out method calls that are older than 10 seconds", timedOutKeys.size());

        }
    }

    private void handleIncomingMessage(final String address, final Message<String> message) {
        final Map<String,Object> map = jsonMapper.fromJson(message.body(), Map.class);
        final Object method = map.get("method");
        final ValueContainer args = (ValueContainer) map.get("args");

        final Object body = args.toValue();


        final MethodCall<Object> methodCall = MethodCallBuilder
                .methodCallBuilder()
                .setAddress(address)
                .setBody(body)
                .setTimestamp(this.timer.time())
                .setName(method.toString())
                .setId(messageId++)
                .build();
        if (logger.isDebugEnabled()) {
            logger.debug("Calling method {} {}", methodCall.name(), message.body());
        }
        if (methodCallPredicate.test(methodCall)) {
            messageKeyMessageMap.put(new MessageKey(methodCall), message);
            methodCallSendQueue.send(methodCall);
        }
        checkForResponses();
    }

    private void checkForResponses() {
        /** Check for responses when the vertx event loop is not busy. */
        vertx.runOnContext(event -> doCheckForResponses());
    }

    private void doCheckForResponses() {
        Response<Object> response = receiveResponseQueue.poll();
        while (response != null) {
            handleResponse(response);
            response = receiveResponseQueue.poll();
        }
    }

    private void handleResponse(final Response<Object> response) {
        final Message<String> message = messageKeyMessageMap.remove(new MessageKey(response));
        if (message == null) {
            logger.error("Message {} not found", new MessageKey(response));
            return;
        }

        final String json = jsonMapper.toJson(response.body());

        if (logger.isDebugEnabled()) {
            logger.debug("Reply for message {} response {}", message, json);
        }
        message.reply(json);
    }

    private static final class MessageKey {
        final long id;
        final long timeStamp;
        final String address;
        final String methodName;

        private MessageKey(MethodCall methodCall) {
            this.id = methodCall.id();
            this.timeStamp = methodCall.timestamp();
            this.address = methodCall.address();
            this.methodName = methodCall.name();
        }

        private MessageKey(Response response) {
            this.id = response.id();
            this.timeStamp = response.timestamp();
            this.address = response.address();
            MethodCall methodCall = (MethodCall) response.request();
            this.methodName = methodCall.name();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MessageKey that = (MessageKey) o;
            if (id != that.id) return false;
            if (timeStamp != that.timeStamp) return false;
            if (address != null ? !address.equals(that.address) : that.address != null) return false;
            return methodName != null ? methodName.equals(that.methodName) : that.methodName == null;
        }

        @Override
        public int hashCode() {
            int result = (int) (id ^ (id >>> 32));
            result = 31 * result + (int) (timeStamp ^ (timeStamp >>> 32));
            result = 31 * result + (address != null ? address.hashCode() : 0);
            result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "MessageKey{" +
                    "id=" + id +
                    ", timeStamp=" + timeStamp +
                    ", address='" + address + '\'' +
                    ", methodName='" + methodName + '\'' +
                    '}';
        }
    }


}

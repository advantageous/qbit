package io.advantageous.qbit.vertx.eventbus.bridge;

import io.advantageous.boon.core.Predicate;
import io.advantageous.qbit.Factory;
import io.advantageous.qbit.QBit;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.json.JsonMapper;
import io.advantageous.qbit.message.MethodCall;
import io.advantageous.qbit.meta.builder.ContextMetaBuilder;
import io.advantageous.qbit.meta.provider.StandardMetaDataProvider;
import io.advantageous.qbit.meta.transformer.StandardRequestTransformer;
import io.advantageous.qbit.queue.SendQueue;
import io.advantageous.qbit.service.ServiceBundle;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.util.Timer;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Bridges the event bus bridge to QBit.
 * The vertx event bus is clustered and supports HA.
 * This bridge builder builds and event bus bridge into the QBit world.
 * This allows method calls, events and responses to be propagated to/from QBit vertx.
 */
public class VertxEventBusBridgeBuilder {

    private final Logger logger = LoggerFactory.getLogger(VertxEventBusBridgeBuilder.class);

    private int flushIntervalMS = 10;
    private boolean autoStart = true;
    private SendQueue<MethodCall<Object>> methodCallSendQueue;
    private Factory factory;
    private JsonMapper jsonMapper;
    private Vertx vertx;
    private Predicate<MethodCall<Object>> methodCallPredicate = methodCall -> true;
    private Set<String> addressesToBridge;
    private EventBus vertxEventBus;
    private Timer timer;


    private ContextMetaBuilder contextMetaBuilder = ContextMetaBuilder.contextMetaBuilder();


    public static VertxEventBusBridgeBuilder vertxEventBusBridgeBuilder() {
        return new VertxEventBusBridgeBuilder();
    }

    public Timer getTimer() {
        if (timer == null) {
            timer = Timer.timer();
        }
        return timer;
    }

    public VertxEventBusBridgeBuilder setTimer(Timer timer) {
        this.timer = timer;
        return this;
    }

    public int getFlushIntervalMS() {
        return flushIntervalMS;
    }

    public VertxEventBusBridgeBuilder setFlushIntervalMS(int flushIntervalMS) {
        this.flushIntervalMS = flushIntervalMS;
        return this;
    }


    public boolean isAutoStart() {
        return autoStart;
    }

    public VertxEventBusBridgeBuilder setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public SendQueue<MethodCall<Object>> getMethodCallSendQueue() {
        Objects.requireNonNull(methodCallSendQueue, "methodCallSendQueue must be set");
        return methodCallSendQueue;
    }

    public VertxEventBusBridgeBuilder setMethodCallSendQueue(SendQueue<MethodCall<Object>> methodCallSendQueue) {
        this.methodCallSendQueue = methodCallSendQueue;
        return this;
    }

    public Factory getFactory() {
        if (factory == null) {
            logger.debug("Factory not set using default QBit factory");
            factory = QBit.factory();
        }
        return factory;
    }

    public VertxEventBusBridgeBuilder setFactory(Factory factory) {
        this.factory = factory;
        return this;
    }

    public JsonMapper getJsonMapper() {
        if (jsonMapper == null) {
            logger.debug("JsonMapper not set using default JsonMapper");
            jsonMapper = getFactory().createJsonMapper();
        }
        return jsonMapper;
    }

    public VertxEventBusBridgeBuilder setJsonMapper(final JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        return this;
    }

    public Vertx getVertx() {
        if (vertx == null) {
            logger.warn("Vertx not set using default Vertx which is likely not what you want!!");
            vertx = Vertx.vertx();
        }
        return vertx;
    }

    public VertxEventBusBridgeBuilder setVertx(Vertx vertx) {
        this.vertx = vertx;
        return this;
    }

    public Predicate<MethodCall<Object>> getMethodCallPredicate() {
        return methodCallPredicate;
    }

    public VertxEventBusBridgeBuilder setMethodCallPredicate(final Predicate<MethodCall<Object>> methodCallPredicate) {
        this.methodCallPredicate = methodCallPredicate;
        return this;
    }

    public Set<String> getAddressesToBridge() {
        Objects.requireNonNull(addressesToBridge, "Addresses to bridge must be set");
        return addressesToBridge;
    }

    public VertxEventBusBridgeBuilder setAddressesToBridge(final Set<String> addressesToBridge) {
        if (this.addressesToBridge != null) {
            logger.warn("Addresses were already set, overwriting addresses");
        }
        this.addressesToBridge = addressesToBridge;
        return this;
    }


    public VertxEventBusBridgeBuilder addBridgeAddress(String address, Class<?> classWithAnnotations) {

        if (addressesToBridge == null) {
            addressesToBridge = new HashSet<>();
        }
        addressesToBridge.add(address);
        if (address == null) {
            contextMetaBuilder.setRootURI("/").addService(classWithAnnotations);
        } else {
            contextMetaBuilder.setRootURI("/").addService(address, classWithAnnotations);
        }
        return this;
    }

    public EventBus getVertxEventBus() {
        if (vertxEventBus == null) {
            vertxEventBus = getVertx().eventBus();
        }
        return vertxEventBus;
    }

    public VertxEventBusBridgeBuilder setVertxEventBus(EventBus vertxEventBus) {
        this.vertxEventBus = vertxEventBus;
        return this;
    }

    public VertxEventBusBridge build() {


        final Map<RequestMethod, StandardMetaDataProvider> metaDataProviderMap = new ConcurrentHashMap<>();
        metaDataProviderMap.put(RequestMethod.BRIDGE, new StandardMetaDataProvider(contextMetaBuilder.build(), RequestMethod.BRIDGE));
        final StandardRequestTransformer standardRequestTransformer = new StandardRequestTransformer(metaDataProviderMap, Optional.of((Consumer<Throwable>) throwable -> logger.error("", throwable)));

        final VertxEventBusBridge bridge = new VertxEventBusBridge(this.getAddressesToBridge(),
                this.getMethodCallSendQueue(),
                this.getJsonMapper(),
                this.getVertx(),
                this.getVertxEventBus(),
                this.getTimer(),
                this.getMethodCallPredicate(),
                this.getFlushIntervalMS(),
                this.isAutoStart(),
                standardRequestTransformer);
        return bridge;
    }

    public VertxEventBusBridgeBuilder setServiceQueue(ServiceQueue serviceQueue) {
        this.setMethodCallSendQueue(serviceQueue.requests());
        return this;
    }

    public VertxEventBusBridgeBuilder setServiceBundle(ServiceBundle serviceBundle) {
        this.setMethodCallSendQueue(serviceBundle.methodSendQueue());
        return this;
    }
}

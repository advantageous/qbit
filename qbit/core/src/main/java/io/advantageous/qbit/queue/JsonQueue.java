package io.advantageous.qbit.queue;

import io.advantageous.qbit.QBit;
import io.advantageous.qbit.concurrent.PeriodicScheduler;
import io.advantageous.qbit.json.JsonMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Wraps a QBit Queue<String> and converts items into JSON and from JSON.
 *
 * @param <T> convert to this type.
 */
public class JsonQueue<T> implements Queue<T> {

    /**
     * Queue we are wrapping with JSON encoding / decoding.
     */
    private final Queue<String> queue;

    /**
     * Decoder.
     */
    private final Function<String, T> fromJsonFunction;

    /**
     * Encoder.
     */
    private final Function<T, String> toJsonFunction;

    /**
     * @param queue            queue
     * @param fromJsonFunction fromJsonFunction function decoder
     * @param toJsonFunction   toJsonFunction function encoder
     */
    public JsonQueue(final Queue<String> queue,
                     final Function<String, T> fromJsonFunction,
                     final Function<T, String> toJsonFunction) {
        this.queue = queue;
        this.fromJsonFunction = fromJsonFunction;
        this.toJsonFunction = toJsonFunction;
    }

    /**
     * Create a simple JsonQueue that encodes one object via JSON.
     *
     * @param classType  classType
     * @param queue      queue
     * @param jsonMapper jsonMapper
     */
    public JsonQueue(final Class<T> classType,
                     final Queue<String> queue,
                     final JsonMapper jsonMapper) {
        this(queue, json -> jsonMapper.fromJson(json, classType), jsonMapper::toJson);
    }

    /**
     * Create a simple JsonQueue that encodes one object via JSON.
     *
     * @param classType classType
     * @param queue     queue
     */
    public JsonQueue(Class<T> classType, Queue<String> queue) {
        this(classType, queue, QBit.factory().createJsonMapper());
    }

    /**
     * @param componentClass component class type
     * @param queue          queue
     * @param <T>            T
     * @return new JsonQueue that works with lists of componentClass instances
     */
    public static <T> JsonQueue<List<T>> createListQueueWithMapper(final Class<T> componentClass,
                                                                   final Queue<String> queue,
                                                                   final JsonMapper jsonMapper) {
        return new JsonQueue<>(queue,
                json -> jsonMapper.fromJsonArray(json, componentClass),
                jsonMapper::toJson);

    }

    /**
     * @param componentClass component class type
     * @param queue          queue
     * @param <T>            T
     * @return new JsonQueue that works with lists of componentClass instances
     */
    public static <T> JsonQueue<List<T>> createListQueue(final Class<T> componentClass,
                                                         final Queue<String> queue) {

        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        return createListQueueWithMapper(componentClass, queue, jsonMapper);

    }

    /**
     * Create a  JsonQueue that can send a Map of values
     *
     * @param mapKeyClass   mapKey class
     * @param valueKeyClass valueKey class
     * @param queue         queue
     * @param jsonMapper    jsonMapper
     * @param <K>           K
     * @param <V>           V
     * @return new JsonQueue that works with maps.
     */
    public static <K, V> JsonQueue<Map<K, V>> createMapQueueWithMapper(
            final Class<K> mapKeyClass,
            final Class<V> valueKeyClass,
            final Queue<String> queue,
            final JsonMapper jsonMapper) {

        return new JsonQueue<>(queue,
                json -> jsonMapper.fromJsonMap(json, mapKeyClass, valueKeyClass),
                jsonMapper::toJson);
    }

    /**
     * Create a  JsonQueue that can send a Map of values
     *
     * @param mapKeyClass   mapKey class
     * @param valueKeyClass valueKey class
     * @param queue         queue
     * @param <K>           K
     * @param <V>           V
     * @return new JsonQueue that works with maps.
     */
    public static <K, V> JsonQueue<Map<K, V>> createMapQueue(final Class<K> mapKeyClass,
                                                             final Class<V> valueKeyClass,
                                                             final Queue<String> queue) {
        final JsonMapper jsonMapper = QBit.factory().createJsonMapper();
        return createMapQueueWithMapper(mapKeyClass, valueKeyClass, queue, jsonMapper);
    }

    /**
     * Create a wrapper ReceiveQueue that does decoding on the fly.
     *
     * @return wrapped ReceiveQueue
     */
    @Override
    public ReceiveQueue<T> receiveQueue() {

        final ReceiveQueue<String> receiveQueue = queue.receiveQueue();

        return new ReceiveQueue<T>() {
            @Override
            public T pollWait() {
                final String item = receiveQueue.pollWait();
                return getParsedItem(item);
            }

            private T getParsedItem(String item) {
                if (item != null) {
                    return fromJsonFunction.apply(item);
                } else {
                    return null;
                }
            }

            @Override
            public T poll() {
                final String item = receiveQueue.pollWait();
                return getParsedItem(item);
            }

            @Override
            public T take() {
                final String item = receiveQueue.take();
                return getParsedItem(item);
            }

            @Override
            public Iterable<T> readBatch(int max) {
                final Iterable<String> iterable = receiveQueue.readBatch(max);
                return getParsedItems(iterable);
            }

            private Iterable<T> getParsedItems(Iterable<String> iterable) {
                int size = 16;
                if (iterable instanceof List) {
                    size = ((List) iterable).size();
                }
                final List<T> items = new ArrayList<>(size);
                for (String item : iterable) {
                    items.add(getParsedItem(item));
                }
                return items;
            }

            @Override
            public Iterable<T> readBatch() {

                final Iterable<String> iterable = receiveQueue.readBatch();
                return getParsedItems(iterable);
            }

            @Override
            public void stop() {
                receiveQueue.stop();
            }
        };
    }

    /**
     * Create a wrapper SendQueue that encoders the objects to JSON
     * before putting them into the queue.
     *
     * @return returns wrapped SendQueue tht does JSON encoding.
     */
    @Override
    public SendQueue<T> sendQueue() {

        final SendQueue<String> sendQueue = queue.sendQueue();

        return createJsonSendQueue(sendQueue);
    }

    /**
     * Helper method to create SendQueue wrappers that do JSON encoding.
     *
     * @param sendQueue sendQueue
     * @return sendQueue decorated with JSON encoding.
     */
    private SendQueue<T> createJsonSendQueue(final SendQueue<String> sendQueue) {

        return new SendQueue<T>() {
            @Override
            public boolean send(T item) {

                sendQueue.send(toJsonFunction.apply(item));
                return false;
            }

            @Override
            public void sendAndFlush(T item) {

                sendQueue.sendAndFlush(toJsonFunction.apply(item));
            }

            @Override
            public void sendMany(T... items) {

                for (T item : items) {
                    sendQueue.send(toJsonFunction.apply(item));
                }
            }

            @Override
            public void sendBatch(Collection<T> items) {

                for (T item : items) {
                    sendQueue.send(toJsonFunction.apply(item));
                }
            }

            @Override
            public void sendBatch(Iterable<T> items) {

                for (T item : items) {
                    sendQueue.send(toJsonFunction.apply(item));
                }
            }

            @Override
            public boolean shouldBatch() {
                return sendQueue.shouldBatch();
            }

            @Override
            public void flushSends() {

                sendQueue.flushSends();
            }

            @Override
            public int size() {
                return sendQueue.size();
            }

            @Override
            public String name() {
                return sendQueue.name();
            }

            @Override
            public void stop() {
                sendQueue.stop();
            }
        };
    }


    /**
     * Create a wrapper SendQueue that encoders the objects to JSON
     * before putting them into the queue.
     *
     * @param interval interval
     * @param timeUnit timeUnit
     * @return returns wrapped SendQueue tht does JSON encoding.
     */
    @Override
    public SendQueue<T> sendQueueWithAutoFlush(int interval, TimeUnit timeUnit) {
        final SendQueue<String> sendQueue = queue.sendQueueWithAutoFlush(interval, timeUnit);
        return createJsonSendQueue(sendQueue);
    }


    /**
     * Create a wrapper SendQueue that encoders the objects to JSON
     * before putting them into the queue.
     *
     * @param periodicScheduler periodicScheduler
     * @param interval          interval
     * @param timeUnit          timeUnit
     * @return sendQueue
     */
    @Override
    public SendQueue<T> sendQueueWithAutoFlush(PeriodicScheduler periodicScheduler, int interval, TimeUnit timeUnit) {
        final SendQueue<String> sendQueue = queue.sendQueueWithAutoFlush(periodicScheduler, interval, timeUnit);
        return createJsonSendQueue(sendQueue);
    }

    /**
     * Start a listener.
     *
     * @param listener listener
     */
    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {
        queue.startListener(item -> listener.receive(fromJsonFunction.apply(item)));
    }

    /**
     * size
     *
     * @return size
     */
    @Override
    public int size() {
        return queue.size();
    }

    /**
     * started
     *
     * @return started
     */
    @Override
    public boolean started() {
        return queue.started();
    }

    /**
     * name of queue
     *
     * @return name
     */
    @Override
    public String name() {
        return queue.name();
    }

    /**
     * Stop the queue.
     */
    @Override
    public void stop() {
        queue.stop();
    }
}

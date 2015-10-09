package io.advantageous.qbit.queue;

import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.qbit.concurrent.PeriodicScheduler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import java.util.List;

public class JsonQueue <T> implements Queue<T>{

    private final Class<T> classType;
    private final Queue<String> queue;

    public JsonQueue(Class<T> classType, Queue<String> queue) {
        this.classType = classType;
        this.queue = queue;
    }

    @Override
    public ReceiveQueue<T> receiveQueue() {

        final ReceiveQueue<String> receiveQueue = queue.receiveQueue();
        final JsonParserAndMapper jsonParserAndMapper = new JsonParserFactory()
                .createFastObjectMapperParser();

        return new ReceiveQueue<T>() {
            @Override
            public T pollWait() {
                final String item = receiveQueue.pollWait();
                return getParsedItem(item);
            }
            private T getParsedItem(String item) {
                if (item !=null) {
                    final T parsedItem = jsonParserAndMapper.parse(classType, item);
                    return parsedItem;
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

    @Override
    public SendQueue<T> sendQueue() {

        final SendQueue<String> sendQueue = queue.sendQueue();

        return createJsonSendQueue(sendQueue);
    }

    private SendQueue<T> createJsonSendQueue(final SendQueue<String> sendQueue) {
        final JsonSerializer jsonSerializer = new JsonSerializerFactory()
                .setUseAnnotations(true)
                .create();

        return new SendQueue<T>() {
            @Override
            public boolean send(T item) {

                sendQueue.send(jsonSerializer.serialize(item).toString());
                return false;
            }

            @Override
            public void sendAndFlush(T item) {

                sendQueue.sendAndFlush(jsonSerializer.serialize(item).toString());
            }

            @Override
            public void sendMany(T... items) {

                for (T item : items) {
                    sendQueue.send(jsonSerializer.serialize(item).toString());
                }
            }

            @Override
            public void sendBatch(Collection<T> items) {

                for (T item : items) {
                    sendQueue.send(jsonSerializer.serialize(item).toString());
                }
            }

            @Override
            public void sendBatch(Iterable<T> items) {

                for (T item : items) {
                    sendQueue.send(jsonSerializer.serialize(item).toString());
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

    @Override
    public SendQueue<T> sendQueueWithAutoFlush(int interval, TimeUnit timeUnit) {
        final SendQueue<String> sendQueue = queue.sendQueueWithAutoFlush(interval, timeUnit);
        return createJsonSendQueue(sendQueue);
    }

    @Override
    public SendQueue<T> sendQueueWithAutoFlush(PeriodicScheduler periodicScheduler, int interval, TimeUnit timeUnit) {
        final SendQueue<String> sendQueue = queue.sendQueueWithAutoFlush(periodicScheduler, interval, timeUnit);
        return createJsonSendQueue(sendQueue);
    }

    @Override
    public void startListener(final ReceiveQueueListener<T> listener) {


        final ThreadLocal<JsonParserAndMapper> jsonParserAndMapper = new ThreadLocal<JsonParserAndMapper>(){
            @Override
            protected JsonParserAndMapper initialValue() {
                return new JsonParserFactory()
                                .createFastObjectMapperParser();
            }
        };


        queue.startListener(item -> listener.receive(jsonParserAndMapper.get().parse(classType, item)));

    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean started() {
        return queue.started();
    }

    @Override
    public String name() {
        return queue.name();
    }

    @Override
    public void stop() {
        queue.stop();
    }
}

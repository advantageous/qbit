package io.advantageous.qbit.queue;

import io.advantageous.boon.core.Sets;
import io.advantageous.boon.json.JsonParserAndMapper;
import io.advantageous.boon.json.JsonParserFactory;
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
        final JsonParserAndMapper jsonParserAndMapper = new JsonParserFactory().setIgnoreSet(Sets.set("metaClass")).createFastObjectMapperParser();

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
        };
    }

    @Override
    public SendQueue<T> sendQueue() {
        return new SendQueue<T>() {
            @Override
            public boolean send(T item) {
                return false;
            }

            @Override
            public void sendAndFlush(T item) {

            }

            @Override
            public void sendMany(T... items) {

            }

            @Override
            public void sendBatch(Collection<T> items) {

            }

            @Override
            public void sendBatch(Iterable<T> items) {

            }

            @Override
            public boolean shouldBatch() {
                return false;
            }

            @Override
            public void flushSends() {

            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public String name() {
                return null;
            }
        };
    }

    @Override
    public SendQueue<T> sendQueueWithAutoFlush(int interval, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public SendQueue<T> sendQueueWithAutoFlush(PeriodicScheduler periodicScheduler, int interval, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public void startListener(ReceiveQueueListener<T> listener) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean started() {
        return false;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public void stop() {

    }
}

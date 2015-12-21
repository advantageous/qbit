package io.advantageous.qbit.http.websocket;

import io.advantageous.qbit.queue.*;

import java.util.concurrent.TimeUnit;


public class WebSocketTextQueue implements Queue<String> {

    private final WebSocket webSocket;
    private final Queue<String> stringQueue;

    public WebSocketTextQueue(final WebSocket webSocket) {
        this.webSocket = webSocket;
        this.stringQueue = QueueBuilder.queueBuilder().setBatchSize(1).setLimit(100).setPollWait(1000).build();
        final SendQueue<String> sendQueue = this.stringQueue.sendQueue();
        this.webSocket.setTextMessageConsumer(sendQueue::send);
    }

    public WebSocketTextQueue(final WebSocket webSocket, final int batchSize, final int flushInterval, final TimeUnit timeUnit) {
        this.webSocket = webSocket;
        this.stringQueue = QueueBuilder.queueBuilder().setBatchSize(batchSize).setPollWait(1000).build();
        final SendQueue<String> sendQueue = this.stringQueue.sendQueueWithAutoFlush(flushInterval, timeUnit);
        this.webSocket.setTextMessageConsumer(sendQueue::send);
    }

    @Override
    public ReceiveQueue<String> receiveQueue() {
        return stringQueue.receiveQueue();
    }

    @Override
    public SendQueue<String> sendQueue() {
        return new SendQueue<String>() {
            @Override
            public boolean send(String item) {
                webSocket.sendText(item);
                return !webSocket.isClosed();
            }
        };
    }

    @Override
    public void startListener(ReceiveQueueListener<String> listener) {
        stringQueue.startListener(listener);
    }

    @Override
    public int size() {
        return stringQueue.size();
    }
}

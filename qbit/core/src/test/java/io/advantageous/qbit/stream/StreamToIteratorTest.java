package io.advantageous.qbit.stream;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.queue.Queue;
import io.advantageous.qbit.queue.QueueBuilder;
import io.advantageous.qbit.queue.SendQueue;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static io.advantageous.boon.core.IO.puts;
import static io.advantageous.qbit.stream.StreamToIterator.toIterator;
import static io.advantageous.qbit.stream.StreamToIterator.toIteratorWithRequestsPipelineSize;
import static org.junit.Assert.*;


public class StreamToIteratorTest {

    @Test
    public void testGoCode() {
        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIterator(mockPublisher);

        mockPublisher.send("hello");

        assertTrue(iterator.hasNext());

        final String next = iterator.next();

        assertNotNull(next);

        assertEquals("hello", next);

        mockPublisher.close();


        assertFalse(iterator.hasNext());


        assertFalse(iterator.hasNext());

    }

    @Test
    public void testImmediateClose() {
        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIterator(mockPublisher);

        mockPublisher.send("hello");
        mockPublisher.close();

        assertTrue(iterator.hasNext());

        final String next = iterator.next();

        assertNotNull(next);

        assertEquals("hello", next);


        assertFalse(iterator.hasNext());

    }

    @Test
    public void testTwoHasNextChecks() {
        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIterator(mockPublisher);

        mockPublisher.send("hello");
        mockPublisher.close();

        assertTrue(iterator.hasNext());
        assertTrue(iterator.hasNext());

        final String next = iterator.next();

        assertNotNull(next);

        assertEquals("hello", next);


        assertFalse(iterator.hasNext());

    }

    @Test(expected = StreamToIterator.StreamException.class)
    public void testErrorCondition() {
        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIterator(mockPublisher);

        mockPublisher.send("hello");

        mockPublisher.sendError(new IllegalStateException("Ack we lost control"));

        assertTrue(iterator.hasNext());

        final String next = iterator.next();

        assertNotNull(next);

        assertEquals("hello", next);


        assertTrue(iterator.hasNext());

        iterator.next();


    }

    @Test(expected = NoSuchElementException.class)
    public void testForceNoElementException() {

        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIterator(mockPublisher);
        mockPublisher.close();

        assertFalse(iterator.hasNext());

        iterator.next();

    }

    @Test
    public void test100Elements() {

        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIterator(mockPublisher);

        for (int index = 0; index < 100; index++) {
            mockPublisher.send("" + index);
        }

        mockPublisher.close();

        int i = 0;
        while (iterator.hasNext()) {
            String item = iterator.next();
            assertEquals("" + i, item);
            i++;
        }

        assertEquals(100, i);

    }

    @Test
    public void test100ElementsPipeline10() {

        final MockPublisher mockPublisher = new MockPublisher();
        final Iterator<String> iterator = toIteratorWithRequestsPipelineSize(mockPublisher, 10);

        for (int index = 0; index < 100; index++) {
            mockPublisher.send("" + index);
        }

        mockPublisher.close();

        int i = 0;
        while (iterator.hasNext()) {
            String item = iterator.next();
            assertEquals("" + i, item);
            i++;
        }

        assertEquals(100, i);

    }

    @Test
    public void integrationTest() throws Exception {
        final Queue<String> queue = QueueBuilder.queueBuilder().build();

        final QueueToStreamUnicast<String> stream = new QueueToStreamUnicast<>(queue);

        final Iterator<String> iterator = toIteratorWithRequestsPipelineSize(stream, 10);

        final SendQueue<String> sendQueue = queue.sendQueue();

        for (int index = 0; index < 100; index++) {
            sendQueue.send("" + index);
        }
        sendQueue.flushSends();

        Thread thread = new Thread(() -> {
            Sys.sleep(500);
            queue.stop();
        });
        thread.start();


        int i = 0;
        while (iterator.hasNext()) {
            String item = iterator.next();
            assertEquals("" + i, item);
            puts("" + i, item);
            i++;
        }

        assertEquals(100, i);


    }

    class MockPublisher implements Publisher<String> {

        private Subscriber<? super String> subscriber;
        private int requestedCount;

        @Override
        public void subscribe(Subscriber<? super String> subscriber) {
            this.subscriber = subscriber;

            this.subscriber.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    requestedCount += n;
                }

                @Override
                public void cancel() {

                }
            });


        }

        public void send(String string) {

            if (requestedCount > 0) {
                subscriber.onNext(string);
                requestedCount--;
            }
        }

        public void close() {
            subscriber.onComplete();
        }

        public void sendError(Throwable t) {
            subscriber.onError(t);
        }
    }
}
package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Locale;
import java.util.Random;

/**
 * created by rhightower on 5/22/15.
 */
public class StatsDReplicator implements StatReplicator, QueueCallBackHandler {

    private final int flushRateIntervalMS;
    private final ByteBuffer sendBuffer;
    private final boolean multiMetrics;

    private final Random random = new Random();
    private final Logger logger = LoggerFactory.getLogger(StatsDReplicator.class);
    private final InetSocketAddress address;
    private final DatagramChannel channel;

    private long lastFlush;
    private long time;

    /*
    Sets

StatsD supports counting unique occurences of events between flushes, using a Set to store all occurring events.

uniques:765|s
If the count at flush is 0 then you can opt to send no metric at all for this set, by setting config.deleteSets.
     */

    public StatsDReplicator(String host, int port, boolean multiMetrics, int bufferSize, int flushRateIntervalMS) throws IOException {
        this(InetAddress.getByName(host), port, multiMetrics, bufferSize, flushRateIntervalMS);
    }

    public StatsDReplicator(InetAddress host, int port, boolean multiMetrics, int bufferSize, int flushRateIntervalMS) throws IOException {
        address = new InetSocketAddress(host, port);
        channel = DatagramChannel.open();
        this.multiMetrics = multiMetrics;
        this.flushRateIntervalMS = flushRateIntervalMS;
        sendBuffer = ByteBuffer.allocate(bufferSize);

    }

    protected void finalize() throws Throwable {
        super.finalize();
        flushStatSend();
    }


    @SuppressWarnings("UnusedReturnValue")
    public boolean timing(String key, int value) {
        return timing(key, value, 1.0);
    }

    public boolean timing(String key, int value, double sampleRate) {
        return send(sampleRate, String.format(Locale.ENGLISH, "%s:%d|ms", key, value));
    }

    public boolean decrement(String key) {
        return increment(key, -1, 1.0);
    }

    public boolean decrement(String key, int magnitude) {
        return decrement(key, magnitude, 1.0);
    }

    public boolean decrement(String key, int magnitude, double sampleRate) {
        magnitude = magnitude < 0 ? magnitude : -magnitude;
        return increment(key, magnitude, sampleRate);
    }

    public boolean decrement(String... keys) {
        return increment(-1, 1.0, keys);
    }

    public boolean decrement(int magnitude, String... keys) {
        magnitude = magnitude < 0 ? magnitude : -magnitude;
        return increment(magnitude, 1.0, keys);
    }

    public boolean decrement(int magnitude, double sampleRate, String... keys) {
        magnitude = magnitude < 0 ? magnitude : -magnitude;
        return increment(magnitude, sampleRate, keys);
    }

    public boolean increment(String key) {
        return increment(key, 1, 1.0);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean increment(String key, int magnitude) {
        return increment(key, magnitude, 1.0);
    }

    public boolean increment(String key, int magnitude, double sampleRate) {
        String stat = String.format(Locale.ENGLISH, "%s:%s|c", key, magnitude);
        return send(sampleRate, stat);
    }

    public boolean increment(int magnitude, double sampleRate, String... keys) {
        String[] stats = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            stats[i] = String.format(Locale.ENGLISH, "%s:%s|c", keys[i], magnitude);
        }
        return send(sampleRate, stats);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean gauge(String key, double magnitude) {
        return gauge(key, magnitude, 1.0);
    }

    public boolean gauge(String key, double magnitude, double sampleRate) {
        final String stat = String.format(Locale.ENGLISH, "%s:%s|g", key, magnitude);
        return send(sampleRate, stat);
    }

    private boolean send(double sampleRate, String... stats) {

        boolean sentSomething = false; // didn't send anything
        if (sampleRate < 1.0) {
            for (String stat : stats) {
                if (random.nextDouble() <= sampleRate) {
                    stat = String.format(Locale.ENGLISH, "%s|@%f", stat, sampleRate);
                    if (doSend(stat)) {
                        sentSomething = true;
                    }
                }
            }
        } else {
            for (String stat : stats) {
                if (doSend(stat)) {
                    sentSomething = true;
                }
            }
        }

        return sentSomething;
    }

    private boolean doSend(String stat) {
        try {
            final byte[] data = stat.getBytes("utf-8");

            // If we're going to go past the threshold of the buffer then flush.
            // the +1 is for the potential '\n' in multi_metrics below
            if (sendBuffer.remaining() < (data.length + 1)) {
                flushStatSend();
            }

            if (sendBuffer.position() > 0) {         // multiple metrics are separated by '\n'
                sendBuffer.put((byte) '\n');
            }

            sendBuffer.put(data);   // append the data

            if (!multiMetrics) {
                flushStatSend();
            }

            return true;

        } catch (IOException e) {
            logger.error(
                    String.format("Could not send stat %s to host %s:%d",
                            sendBuffer.toString(), address.getHostName(),
                            address.getPort()), e);
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean flushStatSend() {
        try {
            final int sizeOfBuffer = sendBuffer.position();

            if (sizeOfBuffer <= 0) {
                return false;
            }

            final int sentByteCount = sendBufferOverChannel();

            if (sizeOfBuffer == sentByteCount) {
                return true;
            } else {
                logger.error(String.format(
                        "Could not send all of stat %s to host %s:%d. Only sent %d bytes out of %d bytes", sendBuffer.toString(),
                        address.getHostName(), address.getPort(), sentByteCount, sizeOfBuffer));
                return false;
            }

        } catch (IOException e) {
            logger.error(
                    String.format("Could not send stat %s to host %s:%d", sendBuffer.toString(), address.getHostName(),
                            address.getPort()), e);
            return false;
        }
    }

    private int sendBufferOverChannel() throws IOException {
        sendBuffer.flip();
        final int sentByteCount = channel.send(sendBuffer, address);
        sendBuffer.limit(sendBuffer.capacity());
        sendBuffer.rewind();
        return sentByteCount;
    }

    @Override
    public void replicateCount(final String name, final int count, final long time) {
        increment(name, count);
    }

    @Override
    public void replicateLevel(final String name, final int level, final long time) {

        gauge(name, level);
    }

    @Override
    public void replicateTiming(String name, int timed, long time) {
        timing(name, timed);
    }


    private void flushIfNeeded() {
        long delta = time - lastFlush;
        if (delta > flushRateIntervalMS) {
            flushStatSend();
            lastFlush = time;
        }
    }

    @Override
    public void queueLimit() {

        time = Timer.timer().now();
        flushIfNeeded();
    }

    @Override
    public void queueEmpty() {

        time = Timer.timer().now();
        flushIfNeeded();
    }
}
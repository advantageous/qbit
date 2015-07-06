package io.advantageous.qbit.metrics.support;

import io.advantageous.qbit.metrics.StatReplicator;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.util.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


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
    private final int bufferSize;
    private  DatagramChannel channel;
    private final ConcurrentHashMap<String, Metric> countMap = new ConcurrentHashMap<>();


    private long lastFlush;
    private long time;
    private long lastOpenTime;

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

        this.bufferSize = bufferSize;

        openChannel();

        this.multiMetrics = multiMetrics;
        this.flushRateIntervalMS = flushRateIntervalMS;
        sendBuffer = ByteBuffer.allocate(bufferSize + 100);

    }

    private void openChannel() {


        time = Timer.timer().now();


        try {

            if (channel!=null) {
                try {
                    channel.close();
                }catch (Exception ex) {
                    logger.debug("unable to clean up channel connection", ex);
                }
            }
            channel = DatagramChannel.open();
            lastOpenTime = time;
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_SNDBUF, bufferSize * 2);

        }catch (Exception ex) {
            logger.error("Unable to open channel", ex);
        }
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
                if (!flushStatSend()) {
                    logger.error("Buffer overflow, connection might be down");
                    return false;
                }
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

        try {
            sendBuffer.flip();
            /* Made this async. */
            final int sentByteCount = channel.send(sendBuffer, address);
            sendBuffer.limit(sendBuffer.capacity());
            sendBuffer.rewind();
            return sentByteCount;
        }catch (IOException ex) {

            DatagramChannel oldChannel = channel;
            channel = null;

            /* Added recovery logic. */
            if (oldChannel!=null) {
                oldChannel.close();
            }
            openChannel();
            return 0;
        }
    }

    @Override
    public void replicateCount(final String name, final int count, final long time) {
        if (count == 0) {
            return;
        }


       Metric localCount = countMap.get(name);
       if (localCount == null) {
           localCount = Metric.count(name);
            countMap.put(name, localCount);
        }
        localCount.value += count;

    }

    @Override
    public void replicateLevel(final String name, final int level, final long time) {


        Metric localCount = countMap.get(name);
        if (localCount == null) {

            localCount = Metric.level(name);
            countMap.put(name, localCount);

            /* Set the initial level. */
            localCount.value = level;
            /* Send the gauge. */
            gauge(name, level);
        }

        localCount.value = level;

    }

    @Override
    public void replicateTiming(String name, int timed, long time) {


        /* A 0 timing is not useful. */
        if (timed <= 0) {
            return;
        }

        Metric localCount = countMap.get(name);
        if (localCount == null) {

            localCount = Metric.timing(name);
            countMap.put(name, localCount);

            /* Set the initial timing. */
            localCount.value = timed;
            /* Send the timing. */
            timing(name, timed);
        }


        /** It would be nice to average the time. */
        localCount.value = timed;


    }


    private void flushIfNeeded() {
        long delta = time - lastFlush;
        if (delta > flushRateIntervalMS) {
            countMap.entrySet().forEach(entry -> {

                if (entry.getValue().value != 0) {

                    switch (entry.getValue().type) {
                        case COUNT:
                            increment(entry.getKey(), entry.getValue().value);
                            break;
                        case TIMING:
                            timing(entry.getKey(), entry.getValue().value);
                            break;
                        case LEVEL:
                            gauge(entry.getKey(), entry.getValue().value);
                            break;
                    }
                    entry.getValue().value = 0;
                }
            });
            flushStatSend();
            lastFlush = time;
        }
    }


    @Override
    public void queueProcess() {

        time = Timer.timer().now();
        flushIfNeeded();

        /* Reopen channel every hour so if there is a problem like last time
        we are at least fixing it once an hour.
         */
        if (time - lastOpenTime > (60 * 60 * 1000) || channel == null) {
            openChannel();
        }

    }


    enum MetricType {
        COUNT, LEVEL, TIMING;
    }

    final static class Metric {

        int value;
        final String name;
        final MetricType type;


        public static Metric count(String name) {

            return new Metric(name, MetricType.COUNT);

        }

        public static Metric level(String name) {

            return new Metric(name, MetricType.LEVEL);

        }


        public static Metric timing(String name) {

            return new Metric(name, MetricType.TIMING);

        }

        public Metric(String name, MetricType type) {
            this.name = name;
            this.type = type;
        }
    }
}